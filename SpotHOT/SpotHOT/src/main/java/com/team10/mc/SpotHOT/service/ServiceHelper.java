package com.team10.mc.SpotHOT.service;

import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.Toast;


import com.team10.mc.SpotHOT.service.extraservices.OnStartTetheringCallback;
import com.team10.mc.SpotHOT.service.extraservices.OreoWiFiManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static android.os.BatteryManager.BATTERY_PLUGGED_USB;
import static android.os.SystemClock.currentThreadTimeMillis;


public class ServiceHelper {

    private final Context context;
    private final WifiManager wifiManager;
    private static final String TAG = "ServiceHelper";
    private static final int TIMEOUT = 5000;

    public ServiceHelper(Context context) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public boolean isTetheringWiFi() {
        try {
            final Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);
        } catch (IllegalAccessException ex) {
            //MyLog.e(TAG, ex);
        } catch (InvocationTargetException ex) {
            //MyLog.e(TAG, ex);
        } catch (NoSuchMethodException ex) {
            //MyLog.e(TAG, ex);
        }

        return false;
    }


    public boolean isPluggedToPower() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int chargePlug = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) : 0;
        return chargePlug == BATTERY_PLUGGED_USB || chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
    }

    public float batteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : 0;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : 0;
        return scale != 0 ? level / (float) scale : 0;
    }


    public String getTetheringSSID() {
        WifiConfiguration cfg = getWifiApConfiguration();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("default.wifi.network", cfg != null ? cfg.SSID : "");
    }


    public boolean isConnectedToInternetThroughMobile() {
        return isConnectedToInternetThroughNetworkType(ConnectivityManager.TYPE_MOBILE);
    }

    public boolean isConnectedToInternetThroughWiFi() {
        return isConnectedToInternetThroughNetworkType(ConnectivityManager.TYPE_WIFI);
    }

    private boolean isConnectedToInternetThroughNetworkType(int networkType) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm != null ? cm.getNetworkInfo(networkType) : null;
        return info != null && info.isConnected();
    }

    public boolean isConnectedtToInternet() {
        return isConnectedToInternetThroughMobile() || isConnectedToInternetThroughWiFi();
    }




    public void setWifiTethering(boolean enable, WifiConfiguration netConfig) {
        if (enable) {
            wifiManager.setWifiEnabled(false);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Method[] methods = wifiManager.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals("setWifiApEnabled")) {
                    try {
                        //MyLog.i(TAG, "setWifiTethering to " + enable);
                        method.invoke(wifiManager, netConfig, enable);
                    } catch (Exception ex) {
                        //MyLog.e(TAG, "Switch on tethering", ex);
                        Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    break;
                }
            }
        } else {
            OreoWiFiManager oreoWifiManager = new OreoWiFiManager(context.getApplicationContext());
            OnStartTetheringCallback callback = new OnStartTetheringCallback() {
                @Override
                public void onTetheringStarted() {
                    Toast.makeText(context, "tethering On", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onTetheringFailed() {
                    Toast.makeText(context, "tethering failed", Toast.LENGTH_LONG).show();
                }
            };
            if (enable) {
                oreoWifiManager.startTethering(callback, null);
            } else {
                oreoWifiManager.stopTethering();
            }
        }
    }


    public void setMobileDataEnabled(boolean enabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //MyLog.e(TAG, "Unimplemented setMobileDataEnabled on Android 5.0!");
            return;
        }
        final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            final Class conmanClass = Class.forName(conman.getClass().getName());
            final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = iConnectivityManagerField.get(conman);
            final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
            final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);

            setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
        } catch (Exception e) {
            //MyLog.e(TAG, "Changing mobile connection state", e);
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private WifiConfiguration getWifiApConfiguration() {
        final Method method = getWifiManagerMethod("getWifiApConfiguration", wifiManager);
        if (method != null) {
            try {
                return (WifiConfiguration) method.invoke(wifiManager);
            } catch (Exception e) {
                //MyLog.e(TAG, e, false);
            }
        }
        return null;
    }

    private Method getWifiManagerMethod(final String methodName, final WifiManager wifiManager) {
        final Method[] methods = wifiManager.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    @Deprecated
    public void usbTethering(boolean value) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        String[] available = null;
        int code = -1;
        Method[] wmMethods = cm.getClass().getDeclaredMethods();

        for (Method method : wmMethods) {
            if (method.getName().equals("getTetherableIfaces")) {

                try {
                    available = (String[]) method.invoke(cm);
                } catch (IllegalAccessException e) {
                    return;
                } catch (InvocationTargetException e) {
                    return;
                }
                break;
            }
        }

        for (Method method : wmMethods) {
            if (method.getName().equals("tether")) {
                try {
                    code = (Integer) method.invoke(cm, available != null ? available[0] : null);
                } catch (IllegalAccessException e) {
                    return;
                } catch (InvocationTargetException e) {
                    return;
                }
                break;
            }
        }

    }

    public static long getDataUsage() {
        long rx = TrafficStats.getMobileRxBytes() != TrafficStats.UNSUPPORTED ? TrafficStats.getMobileRxBytes() : 0;
        long tx = TrafficStats.getMobileTxBytes() != TrafficStats.UNSUPPORTED ? TrafficStats.getMobileTxBytes() : 0;
        return rx + tx;
    }


    public boolean isServiceRunning(Class<? extends Service> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isBluetoothActive() {
        return BluetoothAdapter.getDefaultAdapter() != null && BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public void setBluetoothStatus(boolean bluetoothStatus) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (bluetoothStatus && !mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            } else if (!bluetoothStatus && mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
            }
        }
    }






    public void enableWifi() {
        wifiManager.setWifiEnabled(true);
    }

    public boolean isWifiEnabled() {
        return wifiManager.isWifiEnabled();
    }

    public void registerReceiver(BroadcastReceiver receiver, String... actions) {
        IntentFilter filter = new IntentFilter();
        for (String action : actions) {
            filter.addAction(action);
        }
        context.registerReceiver(receiver, filter);
    }
}
