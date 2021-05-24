package com.team10.mc.SpotHOT;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.SparseIntArray;
import android.widget.TextView;
import android.widget.Toast;


import com.team10.mc.SpotHOT.db.DBManager;
import com.team10.mc.SpotHOT.db.WiFiTethering;

import java.io.BufferedReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;


public class Utils {

    private Utils() {
    }

    public static boolean validateTime(final String time) {
        final String TIME24HOURS_PATTERN = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
        Pattern pattern = Pattern.compile(TIME24HOURS_PATTERN);
        Matcher matcher = pattern.matcher(time);
        return matcher.matches();
    }

    public static int connectedClients() {
        int res = 0;
        BufferedReader br = null;


        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("IP")) {
                    res++;
                }
            }

        } catch (FileNotFoundException e) {
            //MyLog.e("connectedClients", e);
        } catch (IOException e) {
            //MyLog.e("connectedClients", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    //MyLog.e("Close Reader", e);
                }
            }
        }


        return res;
    }

    public static String maskToDays(int mask) {
        String binary = String.format("%7s", Integer.toBinaryString(mask)).replace(' ', '0');
        StringBuilder result = new StringBuilder();
        Map<Integer, String> map = new HashMap<>();
        map.put(6, "Mon");
        map.put(5, "Tue");
        map.put(4, "Wed");
        map.put(3, "Thu");
        map.put(2, "Fri");
        map.put(1, "Sat");
        map.put(0, "Sun");

        for (int i = binary.length() - 1; i >= 0; i--) {
            if ("1".equals(binary.substring(i, i + 1))) {
                result.append(map.get(i)).append(" ");
            }
        }
        result = new StringBuilder(result.toString().trim().replaceAll(" ", ", "));
        return result.toString();
    }

    public static int adapterDayOfWeek(int day) {
        SparseIntArray map = new SparseIntArray();
        map.put(1, 6);
        map.put(2, 0);
        map.put(3, 1);
        map.put(4, 2);
        map.put(5, 3);
        map.put(6, 4);
        map.put(7, 5);
        return map.get(day);
    }

    public static void broadcast(Context context, Intent intent) {
        if (intent != null && context != null) {
            PendingIntent onPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                onPendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                //MyLog.e("Util", e);
            }
        }
    }

    public static int getWidgetId(Intent intent) {
        Bundle extras = intent.getExtras();
        int appWidgetId = INVALID_APPWIDGET_ID;
        if (extras != null) {
            appWidgetId = extras.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID);
        }
        return appWidgetId;
    }

    public static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }



    /**
     * Returns the list of preferred devices ordered by the last time connection
     *
     * @return
     */
    public static List<String> findPreferredDevices(final SharedPreferences prefs) {
        Map<String, ?> map = prefs.getAll();
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (entry.getKey().startsWith("bt.devices.")) {
                list.add(String.valueOf(entry.getValue()));
            }
        }
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String deviceName1, String deviceName2) {
                long lastConnectTime1 = prefs.getLong("bt.last.connect." + deviceName1, 0);
                long lastConnectTime2 = prefs.getLong("bt.last.connect." + deviceName2, 0);
                return (int) (lastConnectTime2 - lastConnectTime1);
            }
        });
        return list;
    }

    /**
     * Returns current system setting whether Data Roaming is enabled
     *
     * @param context
     * @return
     */




    public static void showToast(final Context context, final String text) {
        Handler h = new Handler(context.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                TextView v = toast.getView().findViewById(android.R.id.message);
                if (v != null) {
                    v.setPadding(2, 2, 2, 2);
                }
                toast.show();
            }
        });
    }







    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }



    /**
     * Get the last known location from a specific provider (network/gps)
     */


    public static void resetDataUsageStat(SharedPreferences prefs, long resetValue, long lastValue) {
        long timestamp = System.currentTimeMillis();
        prefs.edit().putLong("data.usage.last.value", lastValue).apply();
        prefs.edit().putLong("data.usage.removeAllData.value", resetValue).apply();
        prefs.edit().putLong("data.usage.removeAllData.timestamp", timestamp).apply();
        prefs.edit().putLong("data.usage.update.timestamp", timestamp).apply();
    }

    public static String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + "B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "kMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f%sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String humanReadableDistance(long bytes) {
        int unit = 1000;
        if (bytes < unit) return bytes + "m";
        if (bytes / 1000d < 9.999) {
            return String.format("%.2fkm", bytes / 1000d);
        }
        return String.format("%.0fkm", bytes / 1000d);
    }

    public static WifiConfiguration getDefaultWifiConfiguration(Context context, SharedPreferences prefs) {
        String ssid = prefs.getString("default.wifi.network", null);
        WifiConfiguration netConfig = null;
        if (ssid != null) {
            List<WiFiTethering> list = DBManager.getInstance(context).readWiFiTethering();
            for (WiFiTethering item : list) {
                if (ssid.equals(item.getSsid())) {
                    netConfig = saveWifiConfiguration(context, item);
                    break;
                }
            }
        }
        return netConfig;
    }

    public static WifiConfiguration saveWifiConfiguration(Context context, WiFiTethering wifiTethering) {
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration netConfig = null;
        if (wifiTethering != null) {
            netConfig = new WifiConfiguration();
            netConfig.SSID = wifiTethering.getSsid();
            netConfig.preSharedKey = wifiTethering.getPassword();
            netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN | WifiConfiguration.AuthAlgorithm.SHARED);
            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN | WifiConfiguration.Protocol.WPA);
            netConfig.allowedKeyManagement.set(wifiTethering.getType().getCode());
            netConfig.hiddenSSID = wifiTethering.isHidden();
        }
        wifi.saveConfiguration();
        return netConfig;
    }

    public static int strToInt(String value) {
        return strToInt(value, 0);
    }

    public static int strToInt(String value, int defaultValue) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }
}
