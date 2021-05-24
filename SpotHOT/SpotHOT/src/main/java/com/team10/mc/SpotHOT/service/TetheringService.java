package com.team10.mc.SpotHOT.service;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.team10.mc.SpotHOT.AppProperties;

import com.team10.mc.SpotHOT.R;
import com.team10.mc.SpotHOT.TetherIntents;
import com.team10.mc.SpotHOT.Utils;
import com.team10.mc.SpotHOT.activity.MainActivity;
import com.team10.mc.SpotHOT.db.Cron;
import com.team10.mc.SpotHOT.db.Cron.STATUS;
import com.team10.mc.SpotHOT.db.DBManager;
import com.team10.mc.SpotHOT.receiver.ChargeBroadcastReceiver;
import com.team10.mc.SpotHOT.receiver.NetworkConnectionReceiver;
import com.team10.mc.SpotHOT.receiver.TetheringStateReceiver;
import com.team10.mc.SpotHOT.receiver.TetheringWidgetProvider;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static android.content.Intent.ACTION_BATTERY_CHANGED;
import static android.content.Intent.ACTION_POWER_CONNECTED;
import static android.content.Intent.ACTION_POWER_DISCONNECTED;
import static android.telephony.PhoneStateListener.LISTEN_NONE;
import static com.team10.mc.SpotHOT.AppProperties.ACTIVATE_3G;
import static com.team10.mc.SpotHOT.AppProperties.ACTIVATE_KEEP_SERVICE;
import static com.team10.mc.SpotHOT.AppProperties.ACTIVATE_ON_ROAMING;
import static com.team10.mc.SpotHOT.AppProperties.ACTIVATE_ON_ROAMING_HC;
import static com.team10.mc.SpotHOT.AppProperties.ACTIVATE_ON_SIMCARD;
import static com.team10.mc.SpotHOT.AppProperties.ACTIVATE_TETHERING;
import static com.team10.mc.SpotHOT.AppProperties.BT_TIMER_INTERVAL;
import static com.team10.mc.SpotHOT.AppProperties.BT_TIMER_START_DELAY;
import static com.team10.mc.SpotHOT.AppProperties.CHANNEL_ID;
import static com.team10.mc.SpotHOT.AppProperties.DATAUSAGE_TIMER_INTERVAL;
import static com.team10.mc.SpotHOT.AppProperties.DATAUSAGE_TIMER_START_DELAY;
import static com.team10.mc.SpotHOT.AppProperties.DEFAULT_IDLE_TETHERING_OFF_TIME;
import static com.team10.mc.SpotHOT.AppProperties.IDLE_3G_OFF;
import static com.team10.mc.SpotHOT.AppProperties.IDLE_3G_OFF_TIME;
import static com.team10.mc.SpotHOT.AppProperties.IDLE_TETHERING_OFF;
import static com.team10.mc.SpotHOT.AppProperties.IDLE_TETHERING_OFF_TIME;
import static com.team10.mc.SpotHOT.AppProperties.RETURN_TO_PREV_STATE;
import static com.team10.mc.SpotHOT.AppProperties.SILENT_CHANNEL_ID;
import static com.team10.mc.SpotHOT.TetherIntents.BT_CONNECTED;
import static com.team10.mc.SpotHOT.TetherIntents.BT_DISCONNECTED;
import static com.team10.mc.SpotHOT.TetherIntents.BT_START_SEARCH;
import static com.team10.mc.SpotHOT.TetherIntents.BT_START_TASKSEARCH;
import static com.team10.mc.SpotHOT.TetherIntents.BT_STOP;
import static com.team10.mc.SpotHOT.TetherIntents.CHANGE_CELL;
import static com.team10.mc.SpotHOT.TetherIntents.CHANGE_NETWORK_STATE;
import static com.team10.mc.SpotHOT.TetherIntents.EVENT_MOBILE_OFF;
import static com.team10.mc.SpotHOT.TetherIntents.EVENT_MOBILE_ON;
import static com.team10.mc.SpotHOT.TetherIntents.EVENT_TETHER_OFF;
import static com.team10.mc.SpotHOT.TetherIntents.EVENT_TETHER_ON;
import static com.team10.mc.SpotHOT.TetherIntents.EVENT_WIFI_OFF;
import static com.team10.mc.SpotHOT.TetherIntents.EVENT_WIFI_ON;
import static com.team10.mc.SpotHOT.TetherIntents.EXIT;
import static com.team10.mc.SpotHOT.TetherIntents.RESUME;
import static com.team10.mc.SpotHOT.TetherIntents.SERVICE_ON;
import static com.team10.mc.SpotHOT.TetherIntents.TEMPERATURE_ABOVE_LIMIT;
import static com.team10.mc.SpotHOT.TetherIntents.TEMPERATURE_BELOW_LIMIT;
import static com.team10.mc.SpotHOT.TetherIntents.TETHERING;
import static com.team10.mc.SpotHOT.TetherIntents.USB_OFF;
import static com.team10.mc.SpotHOT.TetherIntents.USB_ON;
import static com.team10.mc.SpotHOT.TetherIntents.WIDGET;
import static com.team10.mc.SpotHOT.Utils.adapterDayOfWeek;
import static com.team10.mc.SpotHOT.Utils.getDefaultWifiConfiguration;


public class TetheringService extends IntentService {

    private static final String TAG = "TetheringService";
    private static final int CHECK_DELAY = 5;
    private static final int NOTIFICATION_ID = 1234;
    private static final int MINUTE_IN_MS = 60000;

    private boolean forceOff = false, forceOn = false;
    private boolean changeMobileState;
    private boolean initial3GStatus, initialTetheredStatus, initialBluetoothStatus, initialWifiStatus, wifiWasEnabled;
    private boolean tetheringProcessing;
    private boolean blockForceInternet;
    private boolean runFromActivity;
    private boolean flag = true;
    private boolean internetOn;
    private long lastAccess = getTime().getTimeInMillis();
    private BroadcastReceiver receiver;
    private String lastNotificationTickerText;
    private String connectedDeviceName;
    private List<Cron> crons;
    private SharedPreferences prefs;
    private ServiceHelper serviceHelper;
    private Notification notification;
    private Timer bluetoothTimer, dataUsageTimer;
    private TimerTask dataUsageTask, bluetoothTask;
    private MyPhoneStateListener myPhoneStateListener;

    private enum ScheduleResult {
        ON, OFF, NONE
    }

    private enum Status {
        DEFAULT,
        DEACTIVATED_ON_IDLE,
        ACTIVATED_ON_SCHEDULE,
        DEACTIVATED_ON_SCHEDULE,
        USB_ON,
        DATA_USAGE_LIMIT_EXCEED,
        ACTIVATED_ON_CELL, DEACTIVATED_ON_CELL,
        TEMPERATURE_OFF,
        BT
    }

    private Status previousStatus, status = Status.DEFAULT;

    private static final String[] invents = {TETHERING, WIDGET, RESUME, EXIT, USB_ON, USB_OFF,
            BT_STOP, BT_CONNECTED, BT_DISCONNECTED, BT_START_SEARCH, BT_START_TASKSEARCH, TEMPERATURE_ABOVE_LIMIT, TEMPERATURE_BELOW_LIMIT, CHANGE_NETWORK_STATE, TetherIntents.TETHER_ON, TetherIntents.TETHER_OFF, TetherIntents.INTERNET_ON, TetherIntents.INTERNET_OFF,
            EVENT_TETHER_OFF, EVENT_TETHER_ON, EVENT_MOBILE_OFF, EVENT_MOBILE_ON, EVENT_WIFI_OFF, EVENT_WIFI_ON, SERVICE_ON, CHANGE_CELL, ACTION_BATTERY_CHANGED
    };

    public TetheringService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        serviceHelper = new ServiceHelper(getApplicationContext());
        onChangeProperties();
        init();
        registerReceivers();
        registerTimeTask();
        createNotificationChannels();
    }

    private void registerTimeTask() {
        dataUsageTask = new DataUsageTimerTask(getApplicationContext());
       // bluetoothTask = new BluetoothTimerTask(getApplicationContext());
        bluetoothTimer = new Timer();
        dataUsageTimer = new Timer();
        dataUsageTimer.schedule(dataUsageTask, DATAUSAGE_TIMER_START_DELAY, DATAUSAGE_TIMER_INTERVAL);
        if (prefs.getBoolean("bt.incoming.listen", false)) {
            bluetoothTimer.schedule(bluetoothTask, BT_TIMER_START_DELAY, BT_TIMER_INTERVAL);
        }
    }

    /**
     * Since Android 26 (Oreo) some broadcasts can no longer register broadcast receivers for implicit broadcasts in their manifest.
     *
     * @see <a href="https://developer.android.com/guide/components/broadcast-exceptions">Implicit Broadcast Exceptions</a>
     */
    private void registerReceivers() {
        // Own receivers
        receiver = new TetheringServiceReceiver();
        serviceHelper.registerReceiver(receiver, invents);

        // Register receivers declared in Manifest
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            serviceHelper.registerReceiver(new ChargeBroadcastReceiver(), ACTION_POWER_CONNECTED, ACTION_POWER_DISCONNECTED);
            serviceHelper.registerReceiver(new TetheringWidgetProvider(), "android.appwidget.action.APPWIDGET_UPDATE");
            serviceHelper.registerReceiver(new NetworkConnectionReceiver(), "android.net.conn.CONNECTIVITY_CHANGE", "android.net.wifi.WIFI_AP_STATE_CHANGED");
            serviceHelper.registerReceiver(new TetheringStateReceiver(), "android.net.wifi.WIFI_AP_STATE_CHANGED");
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        //MyLog.clean();
        runAsForeground();
        if (prefs.getBoolean("data.limit.startup.reset", false)) {
            //MyLog.i("Datausage", "Reset data usage at startup");
            Utils.resetDataUsageStat(prefs, -ServiceHelper.getDataUsage(), 0);
            Intent onIntent = new Intent(TetherIntents.DATA_USAGE);
            onIntent.putExtra("value", 0);
        }

        myPhoneStateListener = new MyPhoneStateListener(getApplicationContext());
        final TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        int events = PhoneStateListener.LISTEN_CELL_LOCATION;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            telManager.listen(myPhoneStateListener, events);
        }
    }

    private void init() {
        initial3GStatus = serviceHelper.isConnectedToInternetThroughMobile();
        initialTetheredStatus = serviceHelper.isTetheringWiFi();
        initialBluetoothStatus = serviceHelper.isBluetoothActive();
        initialWifiStatus = serviceHelper.isConnectedToInternetThroughWiFi();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //MyLog.i(TAG, "onStartCommand");
        runFromActivity = intent.getBooleanExtra("runFromActivity", false);
        int state = intent.getIntExtra("state", -1);
        if (state == 1) {
            execute(ServiceAction.TETHER_OFF);
        } else if (state == 0) {
            execute(ServiceAction.TETHER_ON);
        }
        if (intent.getBooleanExtra("usb.on", false)) {
            sendBroadcast(new Intent(USB_ON));
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (isServiceActivated()) {
            showNotification(getString(R.string.service_started), getNotificationIcon());
            if (checkState(true)) {
                onService();
            }
        }

        while (flag) {
            try {
                if (!(forceOff || forceOn) && (isServiceActivated() || keepService())) {
                    if (checkState(true)) {
                        boolean idle = checkIdle();
                        ScheduleResult res = scheduler();
                        if (res == ScheduleResult.OFF) {
                            execute(ServiceAction.SCHEDULED_INTERNET_OFF, ServiceAction.SCHEDULED_TETHER_OFF);
                        } else if (res == ScheduleResult.ON) {
                            setStatus(Status.DEFAULT);

                            execute(ServiceAction.SCHEDULED_TETHER_ON);
                        } else if (idle && serviceHelper.isTetheringWiFi()) {
                            if (check3GIdle()) {
                                execute(ServiceAction.INTERNET_IDLE_OFF);
                            }
                            if (checkWifiIdle()) {
                                execute(ServiceAction.TETHER_IDLE_OFF);
                            }
                        } else if (status == Status.DEFAULT) {
                            onService();
                        }
                    }
                } else if (forceOn) {
                    if (!blockForceInternet && !serviceHelper.isConnectedToInternetThroughMobile()) {
                        execute(ServiceAction.INTERNET_ON);
                    }
                    if (!serviceHelper.isTetheringWiFi()) {
                        execute(ServiceAction.TETHER_ON);
                    }
                }
                if (!keepService()) {
                    flag = false;
                }

                long usage = ServiceHelper.getDataUsage() + prefs.getLong("data.usage.removeAllData.value", 0);
                String dataLimitValue = prefs.getString("data.limit.value", "0");
                int dataLimit = Integer.parseInt(dataLimitValue.isEmpty() ? "0" : dataLimitValue);

                if (prefs.getBoolean("data.limit.on", false) && (usage / (1048576f) > dataLimit)) {
                    execute(ServiceAction.DATA_USAGE_EXCEED_LIMIT);
                } else if (status == Status.DATA_USAGE_LIMIT_EXCEED) {
                    setStatus(Status.DEFAULT);
                }

                TimeUnit.SECONDS.sleep(CHECK_DELAY);
            } catch (InterruptedException e) {
                //MyLog.e(TAG, e);
            }
        }
    }





    private boolean batteryAboveLimit() {
        boolean chkBatteryLvl = prefs.getBoolean("usb.off.battery.lvl", false);
        boolean isConnected = serviceHelper.isPluggedToPower();
        int lvlValue = Utils.strToInt(prefs.getString("usb.off.battery.lvl.value", "15"), 15);
        return isConnected || !chkBatteryLvl || 100f * serviceHelper.batteryLevel() >= lvlValue;
    }


    private boolean usbConnection() {
        boolean flag = prefs.getBoolean("usb.only.when.connected", false);
        return (!flag || serviceHelper.isPluggedToPower()) && batteryAboveLimit();
    }

    private boolean keepService() {
        return prefs.getBoolean(ACTIVATE_KEEP_SERVICE, true);
    }


    private boolean tetheringAsyncTask(boolean state) {
        if (serviceHelper.isTetheringWiFi() == state) {
            return false;
        }

        if (state && !checkState(state)) {
            return false;
        }

        if (state && prefs.getBoolean("wifi.connected.block.tethering", false) && serviceHelper.isConnectedToInternetThroughWiFi()) {
            connectedDeviceName = null;
            showNotification("Tethering blocked due to active connection to WiFi Network", getNotificationIcon());
            return false;
        }

        if (tetheringProcessing) {
            return false;
        }

        tetheringProcessing = true;
        if (state && !serviceHelper.isTetheringWiFi()) {
            wifiWasEnabled = serviceHelper.isWifiEnabled();
        }

        new TurnOnTetheringAsyncTask().doInBackground(state);
        return true;
    }


    private boolean internetAsyncTask(boolean state) {
        if (serviceHelper.isConnectedToInternetThroughMobile() == state) {
            return false;
        }
        if (state && !checkState(state)) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }


        return true;
    }

    private boolean checkState(boolean state) {
        boolean res = doCheckState(state);
        if (!res) {
            internetAsyncTask(false);
            tetheringAsyncTask(false);
        }
        return res;
    }

    private boolean doCheckState(boolean state) {




        if (state && Utils.isAirplaneModeOn(getApplicationContext())) {
            showNotification("Tethering blocked due to activated Airplane Mode", getNotificationIcon());
            return false;
        }

        if (!usbConnection()) {
            showNotification("Tethering blocked due to USB disconnection or battery settings", getNotificationIcon());
            return false;
        }

        return true;
    }

    private boolean isServiceActivated() {
        return runFromActivity || prefs.getBoolean(AppProperties.ACTIVATE_ON_STARTUP, false);
    }


    private ScheduleResult scheduler() {
        Calendar now = getTime();
        onChangeProperties();
        boolean state = false, changed = false;
        for (Cron cron : crons) {
            boolean matchedMask = (cron.getMask() & (int) Math.pow(2, adapterDayOfWeek(now.get(Calendar.DAY_OF_WEEK)))) > 0;
            boolean active = cron.getStatus() == STATUS.SCHED_OFF_ENABLED.getValue();
            if (!active || !matchedMask) {
                continue;
            }

            Calendar timeOff = getTime();
            adjustCalendar(timeOff, cron.getHourOff(), cron.getMinOff());
            Calendar timeOn = getTime();
            adjustCalendar(timeOn, cron.getHourOn(), cron.getMinOn());

            if (cron.getHourOff() == -1) {
                long diff = now.getTimeInMillis() - timeOn.getTimeInMillis();
                if (diff > 0 && CHECK_DELAY * 1000 >= diff) {
                    return ScheduleResult.ON;
                }
            } else if (cron.getHourOn() == -1) {
                long diff = now.getTimeInMillis() - timeOff.getTimeInMillis();
                if (diff > 0 && CHECK_DELAY * 1000 >= diff) {
                    return ScheduleResult.OFF;
                }
            } else {
                long diff = now.getTimeInMillis() - timeOn.getTimeInMillis();
                if (diff > 0 && CHECK_DELAY * 1000 >= diff) {
                    changed = true;
                }
                boolean scheduled = timeOff.getTimeInMillis() < now.getTimeInMillis() && now.getTimeInMillis() < timeOn.getTimeInMillis();
                state |= scheduled;
            }
        }

        return state ? ScheduleResult.OFF : changed ? ScheduleResult.ON : ScheduleResult.NONE;
    }

    private void adjustCalendar(Calendar calendar, int hour, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
    }

    private Calendar getTime() {
        return Calendar.getInstance();
    }


    private boolean checkIdle() {
        Intent onIntent = new Intent(TetherIntents.CLIENTS);
        onIntent.putExtra("value", Utils.connectedClients());
        sendBroadcast(onIntent);
        if (prefs.getBoolean(IDLE_3G_OFF, false) || prefs.getBoolean(IDLE_TETHERING_OFF, false)) {
            if (Utils.connectedClients() > 0) {
                updateLastAccess();
                return false;
            }

            return true;
        } else {
            updateLastAccess();
            if (status == Status.DEACTIVATED_ON_IDLE) {
                setStatus(Status.DEFAULT);
            }
        }
        return false;
    }

    private void updateLastAccess() {
        lastAccess = getTime().getTimeInMillis();
    }

    private boolean check3GIdle() {
        if (prefs.getBoolean(IDLE_3G_OFF, false)) {
            int idle3gOffTime = Utils.strToInt(prefs.getString(IDLE_3G_OFF_TIME, "60"), 60);
            return getTime().getTimeInMillis() - lastAccess > idle3gOffTime * MINUTE_IN_MS;
        }

        return false;
    }

    private boolean checkWifiIdle() {
        if (prefs.getBoolean(IDLE_TETHERING_OFF, false)) {
            int idleTetheringOffTime = Utils.strToInt(prefs.getString(IDLE_TETHERING_OFF_TIME, DEFAULT_IDLE_TETHERING_OFF_TIME));
            return getTime().getTimeInMillis() - lastAccess > idleTetheringOffTime * MINUTE_IN_MS;
        }
        return false;
    }

    private void onChangeProperties() {
        crons = DBManager.getInstance(getApplicationContext()).getCrons();
    }

    private boolean isActivatedTethering() {
        return prefs.getBoolean(ACTIVATE_TETHERING, false) && usbConnection();
    }











    private class TurnOnTetheringAsyncTask extends AsyncTask<Boolean, Void, Void> {
        @Override
        protected Void doInBackground(Boolean... params) {
            updateLastAccess();
            serviceHelper.setWifiTethering(params[0], getDefaultWifiConfiguration(TetheringService.this, prefs));
            return null;
        }
    }

    private void runAsForeground() {
        if (notification == null) {
            this.notification = buildNotification(getString(R.string.service_started));
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private Notification buildNotification(String caption) {
        return buildNotification(caption, getNotificationIcon());
    }

    private int getNotificationIcon() {
        boolean tethering = serviceHelper.isTetheringWiFi();
        boolean internet = serviceHelper.isConnectedToInternetThroughMobile();

        if (tethering && internet) {
            return Color.GREEN;
        } else if (tethering || internet) {
            return Color.YELLOW;
        } else {
            return Color.RED;
        }
    }

    private Notification buildNotification(String caption, int color) {
        lastNotificationTickerText = caption;
        Notification notify;
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent exitIntent = new Intent(EXIT);
        PendingIntent exitPendingIntent = PendingIntent.getBroadcast(this, 0, exitIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        boolean showNotification = prefs.getBoolean("show.notification", true);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setTicker(caption)
                .setContentText(caption)
                .setContentTitle(getText(R.string.app_name))
                .setOngoing(true)
                .setColor(Color.DKGRAY)
                .setSmallIcon(R.drawable.app_white)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(caption).setBigContentTitle(getText(R.string.app_name)));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder.setChannelId(showNotification ? CHANNEL_ID : SILENT_CHANNEL_ID);
        }

        builder.addAction(buildAction());
        buildAdditionalAction(exitPendingIntent, builder);

        notify = builder.build();

        return notify;
    }

    private void buildAdditionalAction(PendingIntent exitPendingIntent, NotificationCompat.Builder builder) {
        if (status == Status.DEACTIVATED_ON_IDLE) {
            Intent onResumeIntent = new Intent(RESUME);
            PendingIntent onResumePendingIntent = PendingIntent.getBroadcast(this, 0, onResumeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_resume24, "Resume", onResumePendingIntent);
        } else if (status == Status.DATA_USAGE_LIMIT_EXCEED) {
            Intent onLimitIntent = new Intent(TetherIntents.UNLOCK);
            PendingIntent onLimitPendingIntent = PendingIntent.getBroadcast(this, 0, onLimitIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_unlocked, "Unlock", onLimitPendingIntent);
        }

        builder.addAction(R.drawable.ic_exit24, "Exit", exitPendingIntent);
    }

    @NonNull
    private NotificationCompat.Action buildAction() {
        Intent onIntent = new Intent(TETHERING);
        PendingIntent onPendingIntent = PendingIntent.getBroadcast(this, 0, onIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int drawable = R.drawable.ic_wifi;
        String ticker = "Turn ON";

        if (forceOff && !forceOn) {
            //drawable = R.drawable.ic_service24;
            ticker = "Service ON";
        } else if (forceOn && !forceOff) {
            //drawable = R.drawable.ic_wifi_off;
            ticker = "Turn OFF";
        }

        return new NotificationCompat.Action.Builder(drawable, ticker, onPendingIntent).build();
    }

    private void updateNotification() {
        showNotification(lastNotificationTickerText, Color.DKGRAY);
    }

    private void showNotification(String body, int icon) {
        try {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = buildNotification(body, icon);
            notificationManager.cancelAll();
            notificationManager.notify(NOTIFICATION_ID, notification);
            //MyLog.i(TAG, "Notification: " + body);
        } catch (Exception ex) {
            //MyLog.e(TAG, ex);
        }
    }

    private void createNotificationChannels() {
        NotificationHelper.createChannels(this);
    }

    @Override
    public void onDestroy() {
        //MyLog.i(TAG, "onDestroy");
        flag = false;
        try {
            revertToInitialState();
            stopForeground(true);
            stopSelf();
            dataUsageTimer.cancel();
            bluetoothTimer.cancel();
            dataUsageTask.cancel();
            bluetoothTask.cancel();
            final TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            telManager.listen(myPhoneStateListener, LISTEN_NONE);
            try {
                unregisterReceiver(receiver);
            } catch (IllegalArgumentException iae) {
                //MyLog.e(TAG, iae, false);
            }
        } catch (Exception ex) {
            //MyLog.e(TAG, ex);
        } finally {
            super.onDestroy();
        }
    }

    private void revertToInitialStateAsync() {
        new TurnOnTetheringAsyncTask().doInBackground(false);
    }

    private void revertToInitialState() {
        if (prefs.getBoolean(RETURN_TO_PREV_STATE, false) && prefs.getBoolean(ACTIVATE_KEEP_SERVICE, true)) {
            new TurnOnTetheringAsyncTask().doInBackground(initialTetheredStatus);
        }
        if (initialWifiStatus) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    serviceHelper.enableWifi();
                }
            }, 1000);
        }
    }

    private class TetheringServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                //MyLog.i(TAG, intent.getAction());
            }
            switch (intent.getAction()) {
                case SERVICE_ON:
                    if (!forceOff && !forceOn) {
                        break;
                    }
                    forceOff = false;
                    forceOn = false;
                    onService();
                    updateNotification();
                    break;
                case TETHERING:
                    // Correct order of execution: Turn OFF -> Turn ON -> Service ON -> ...
                    if (forceOn && !forceOff) {
                        // Turn OFF
                        forceOff = true;
                        forceOn = false;
                        blockForceInternet = false;
                        execute(ServiceAction.TETHER_OFF);
                        execute(ServiceAction.INTERNET_OFF);
                    } else if (!forceOff && !forceOn) {
                        // Turn ON
                        forceOn = true;
                        blockForceInternet = false;
                        execute(ServiceAction.INTERNET_ON);
                        execute(ServiceAction.TETHER_ON);
                    } else {
                        // Service ON
                        forceOff = false;
                        forceOn = false;
                        onService();
                        showNotification("Service waits for activation signals", Color.DKGRAY);
                    }
                    updateNotification();
                    break;

                case WIDGET:
                    changeMobileState = intent.getExtras().getBoolean("changeMobileState", false);
                    blockForceInternet = true;

                    if (serviceHelper.isTetheringWiFi()) {
                        forceOff = true;
                        forceOn = false;
                        execute(ServiceAction.TETHER_OFF);
                    } else {
                        forceOn = true;
                        forceOff = false;
                        setStatus(Status.DEFAULT);
                        execute(ServiceAction.TETHER_ON);
                    }


                    break;

                case RESUME:
                    updateLastAccess();
                    connectedDeviceName = null;
                    if (Status.USB_ON.equals(previousStatus)) {
                        setStatus(previousStatus);
                        sendBroadcast(new Intent(USB_ON));
                    } else {
                        setStatus(previousStatus);
                        onService();
                        updateNotification();
                    }
                    break;

                case USB_ON:
                    if (!forceOff) {
                        if (prefs.getBoolean("usb.activate.on.connect", false)) {
                           // execute(ServiceAction.TETHER_ON, R.string.activate_tethering_usb_on);
                        }
                        if (prefs.getBoolean("usb.internet.force.on", false)) {
                          //  execute(ServiceAction.INTERNET_ON, R.string.activate_internet_usb_on);
                        }
                        setStatus(Status.USB_ON);
                    }
                    break;

                case USB_OFF:
                    if (prefs.getBoolean("usb.deactivate.on.disconnect", false) || !usbConnection()) {
                       // execute(ServiceAction.TETHER_OFF, R.string.activate_tethering_usb_off);
                    }
                    if (prefs.getBoolean("usb.internet.force.off", false) || !usbConnection()) {
                       // execute(ServiceAction.INTERNET_OFF, R.string.activate_internet_usb_off);
                    }

                    break;

                case BT_STOP:
                    bluetoothTask.cancel();
                    bluetoothTimer.cancel();
                    if (!initialBluetoothStatus) {
                        BluetoothAdapter.getDefaultAdapter().disable();
                    }
                    connectedDeviceName = null;
                    revertToInitialStateAsync();
                    if (serviceHelper.isTetheringWiFi()) {
                        execute(ServiceAction.BLUETOOTH_INTERNET_TETHER_OFF);
                    }
                    break;

                case BT_CONNECTED:
                    if (!forceOff) {
                        connectedDeviceName = intent.getStringExtra("name");
                        setStatus(Status.BT);
                        execute(ServiceAction.BLUETOOTH_INTERNET_TETHER_ON);
                    }
                    break;

                case BT_DISCONNECTED:
                    String deviceName = intent.getStringExtra("name");
                    if (connectedDeviceName != null && connectedDeviceName.equals(deviceName)) {
                        connectedDeviceName = null;
                    }
                    execute(ServiceAction.BLUETOOTH_INTERNET_TETHER_OFF);
                    break;



                case TEMPERATURE_ABOVE_LIMIT:
                    if (serviceHelper.isTetheringWiFi()) {
                        execute(ServiceAction.TEMP_TETHER_OFF);
                    }
                    break;

                case TEMPERATURE_BELOW_LIMIT:
                    if (status == Status.TEMPERATURE_OFF && !serviceHelper.isTetheringWiFi()) {
                        execute(ServiceAction.TEMP_TETHER_ON);
                    }
                    break;

                case EVENT_TETHER_OFF:
                    onTetheringOff();
                    break;

                case EVENT_TETHER_ON:
                    onTetheringOn();
                    break;

                case EVENT_MOBILE_OFF:
                    onMobileOff();
                    break;

                case EVENT_MOBILE_ON:
                    onMobileOn();
                    break;

                case EVENT_WIFI_OFF:
                    onWifiOff();
                    break;

                case EVENT_WIFI_ON:
                    onWifiOn();
                    updateNotification();
                    break;

                case TetherIntents.TETHER_ON:
                    execute(ServiceAction.TETHER_ON);
                    break;

                case TetherIntents.TETHER_OFF:
                    execute(ServiceAction.TETHER_OFF);
                    break;

                case TetherIntents.INTERNET_ON:
                    execute(ServiceAction.INTERNET_ON);
                    break;

                case TetherIntents.INTERNET_OFF:
                    execute(ServiceAction.INTERNET_OFF);
                    break;



                case ACTION_BATTERY_CHANGED:
                    if (prefs.getBoolean("usb.off.battery.lvl", false)) {
                        int declaredLevel = Integer.parseInt(prefs.getString("usb.off.battery.lvl.value", "15"));
                        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                        float currentLevel = 100 * level / (float) scale;
                        boolean isConnected = serviceHelper.isPluggedToPower();

                        if (!isConnected && declaredLevel > currentLevel) {
                            sendBroadcast(new Intent(USB_OFF));
                        } else if ((status == Status.USB_ON) && declaredLevel <= currentLevel) {
                            onService();
                        }
                    }
                    break;

                case EXIT:
                    stopSelf();
                    break;
            }
        }



    }

    private void onWifiOn() {
    }

    private void onWifiOff() {
    }

    private void onMobileOn() {
    }

    private void onMobileOff() {
    }

    private void onTetheringOn() {
        tetheringProcessing = false;
    }

    private void onTetheringOff() {
        tetheringProcessing = false;
        if (wifiWasEnabled && !serviceHelper.isWifiEnabled()) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if (!serviceHelper.isTetheringWiFi()) {
                        serviceHelper.enableWifi();
                    }
                }
            });
        }
    }

    private void onService() {
        if (checkState(true) && !tetheringProcessing) {
            boolean tethering = serviceHelper.isTetheringWiFi();
            boolean mobileOn = serviceHelper.isConnectedToInternetThroughMobile();



            if (isActivatedTethering() && !tethering) {
                execute(ServiceAction.TETHER_ON);
            } else if (tethering && !isActivatedTethering()) {
                execute(ServiceAction.INTERNET_OFF);
            }
        }
    }

    private void execute(ServiceAction... serviceAction) {
        for (ServiceAction action : serviceAction) {
            execute(action, 0);
        }
    }

    private void execute(ServiceAction serviceAction, int msg) {
        boolean action = serviceAction.isOn();
        boolean showNotify = false;
        if (serviceAction.isInternet()) {
            if (internetAsyncTask(action)) {
                showNotify = true;
            }
        }
        if (serviceAction.isTethering()) {
            if (tetheringAsyncTask(action)) {
                showNotify = true;
            }
        }

        //MyLog.i(TAG, "Execute action: " + serviceAction.toString());
        notify(serviceAction, msg, showNotify);
    }

    private void notify(ServiceAction serviceAction, int msg, boolean showNotify) {
        Status oldStatus = status;
        int id = 0;
        int icon = getNotificationIcon();
        switch (serviceAction) {
            case TETHER_ON:
                updateLastAccess();
                id = R.string.notification_tethering_restored;
                setStatus(Status.DEFAULT);
                break;
            case TETHER_OFF:
                id = R.string.notification_tethering_off;
                break;
            case INTERNET_ON:
                if (!Utils.isAirplaneModeOn(getApplicationContext()) && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    updateLastAccess();
                    setStatus(Status.DEFAULT);
                    //id = R.string.notification_internet_restored;
                }
                break;
            case INTERNET_OFF:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                   // id = R.string.notification_internet_off;
                }
                break;
            case SCHEDULED_TETHER_ON:
                updateLastAccess();
                id = R.string.notification_scheduled_tethering_on;
                setStatus(Status.ACTIVATED_ON_SCHEDULE);
                break;
            case SCHEDULED_TETHER_OFF:
                id = R.string.notification_scheduled_tethering_off;
                setStatus(Status.DEACTIVATED_ON_SCHEDULE);
                break;
            case SCHEDULED_INTERNET_ON:
                updateLastAccess();
                //id = R.string.notification_scheduled_internet_on;
                setStatus(Status.ACTIVATED_ON_SCHEDULE);
                break;
            case SCHEDULED_INTERNET_OFF:
               // id = R.string.notification_scheduled_internet_off;
                setStatus(Status.DEACTIVATED_ON_SCHEDULE);
                break;
            case TETHER_IDLE_OFF:
                id = R.string.notification_idle_tethering_off;
                previousStatus = status;
                setStatus(Status.DEACTIVATED_ON_IDLE);
                break;
            case INTERNET_IDLE_OFF:
                //id = R.string.notification_idle_internet_off;
                previousStatus = status;
                setStatus(Status.DEACTIVATED_ON_IDLE);
                break;
            case DATA_USAGE_EXCEED_LIMIT:
                id = R.string.notification_data_exceed_limit;
                setStatus(Status.DATA_USAGE_LIMIT_EXCEED);
                break;
            case BLUETOOTH_INTERNET_TETHER_ON:
                //id = R.string.bluetooth_on;
                setStatus(Status.BT);
                break;
            case BLUETOOTH_INTERNET_TETHER_OFF:
               // id = R.string.bluetooth_off;
                showNotify = true;
                setStatus(Status.DEFAULT);
                break;
            case CELL_INTERNET_TETHER_ON:
                //id = R.string.cell_on;
                setStatus(Status.ACTIVATED_ON_CELL);
                break;
            case CELL_INTERNET_TETHER_OFF:
                //id = R.string.cell_off;
                setStatus(Status.DEACTIVATED_ON_CELL);
                break;
            case TEMP_TETHER_OFF:
               // id = R.string.temp_off;
                setStatus(Status.TEMPERATURE_OFF);
                break;
            case TEMP_TETHER_ON:
                //id = R.string.temp_on;
                setStatus(Status.DEFAULT);
                break;
            default:
        }

        if (showNotify || !status.equals(oldStatus)) {
            if (msg != 0) {
                id = msg;
            }
            if (id > 0) {
                String text = getString(id);
                if (id == R.string.notification_tethering_restored) {
                    text += ": " + prefs.getString("default.wifi.network", "");
                }
                showNotification(text, icon);
            }
        }
    }

    private void setStatus(Status status) {
        previousStatus = this.status;
        this.status = status;
    }
}
