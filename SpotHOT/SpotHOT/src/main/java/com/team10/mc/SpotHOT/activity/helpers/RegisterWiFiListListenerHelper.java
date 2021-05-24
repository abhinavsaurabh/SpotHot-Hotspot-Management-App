package com.team10.mc.SpotHOT.activity.helpers;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.team10.mc.SpotHOT.TetherIntents;
import com.team10.mc.SpotHOT.Utils;
import com.team10.mc.SpotHOT.activity.MainActivity;
import com.team10.mc.SpotHOT.db.WiFiTethering;
import com.team10.mc.SpotHOT.ui.dialog.WiFiTetheringDialog;

import java.util.List;



public class RegisterWiFiListListenerHelper extends AbstractRegisterHelper {

    private static final int CONST_ITEMS = 3; // Skip constant elements on list ADD, MODIFY, REMOVE
    private static final int MAX_WIFI_NETWORKS = 10;

    public RegisterWiFiListListenerHelper(MainActivity activity) {
        super(activity);
    }

    @Override
    public void registerUIListeners() {
        final PreferenceCategory list = getPreferenceCategory("wifi.list");
        final PreferenceScreen remove = getPreferenceScreen("wifi.remove.device");
        final PreferenceScreen modify = getPreferenceScreen("wifi.modify.device");

        List<WiFiTethering> nets = db.readWiFiTethering();
        clean("wifi.list");
        for (WiFiTethering net : nets) {
            createCheckBoxPreference(list, activity, String.valueOf(net.getId()), "SSID: " + net.getSsid(), "Security: " + net.getType().name());
        }

        remove.setEnabled(list.getPreferenceCount() > CONST_ITEMS);
        modify.setEnabled(list.getPreferenceCount() > CONST_ITEMS);

        getPreferenceScreen("wifi.add.device").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (list.getPreferenceCount() >= MAX_WIFI_NETWORKS + CONST_ITEMS) {
                    Toast.makeText(activity, "Exceeded the limit of max. " + MAX_WIFI_NETWORKS + " networks!", Toast.LENGTH_LONG).show();
                    return false;
                }

                final WiFiTetheringDialog dialog = new WiFiTetheringDialog(activity, null);
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dlg) {
                        WiFiTethering entity = dialog.getEntity();
                        if (entity != null) {
                            long id = db.addOrUpdateWiFiTethering(entity);

                            if (id > 0) {
                                createCheckBoxPreference(list, activity, String.valueOf(id), "SSID: " + entity.getSsid(), "Security: " + entity.getType().name());
                                remove.setEnabled(list.getPreferenceCount() > CONST_ITEMS);
                                modify.setEnabled(list.getPreferenceCount() > CONST_ITEMS);

                                if (entity.isDefaultWiFi()) {
                                    Utils.saveWifiConfiguration(activity, entity);
                                    prefs.edit().putString("default.wifi.network", entity.getSsid()).apply();
                                    activity.sendBroadcast(new Intent(TetherIntents.WIFI_DEFAULT_REFRESH));
                                }
                            } else {
                                Toast.makeText(activity, "Add network failed. Please check if SSID name is unique and already on the list", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
                dialog.show();
                return false;
            }
        });

        modify.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Preference pref = null;
                int selected = 0;

                for (int idx = list.getPreferenceCount() - 1; idx >= 0; idx--) {
                    if (list.getPreference(idx) instanceof CheckBoxPreference) {
                        boolean status = ((CheckBoxPreference) list.getPreference(idx)).isChecked();
                        if (status) {
                            selected++;
                            pref = list.getPreference(idx);
                        }
                    }
                }

                if (selected == 0) {
                    Toast.makeText(activity, "Please select any item!", Toast.LENGTH_LONG).show();
                } else if (selected > 1) {
                    Toast.makeText(activity, "Please select only one item!", Toast.LENGTH_LONG).show();
                } else {
                    WiFiTethering entity = db.getWifiTethering(Integer.valueOf(pref.getKey()));
                    entity.setDefaultWiFi(prefs.getString("default.wifi.network", "").equals(entity.getSsid()));
                    final WiFiTetheringDialog dialog = new WiFiTetheringDialog(activity, entity);
                    final Preference finalPref = pref;
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dlg) {
                            WiFiTethering entity = dialog.getEntity();
                            try {
                                db.addOrUpdateWiFiTethering(entity);

                                setPreferenceCheckBox(finalPref, String.valueOf(entity.getId()), "SSID: " + entity.getSsid(), "Security: " + entity.getType().name());

                                if (entity.isDefaultWiFi() && !prefs.getString("default.wifi.network", "").equals(entity.getSsid())) {
                                    Utils.saveWifiConfiguration(activity, entity);
                                    prefs.edit().putString("default.wifi.network", entity.getSsid()).apply();
                                    activity.sendBroadcast(new Intent(TetherIntents.WIFI_DEFAULT_REFRESH));
                                }
                            } catch (SQLiteException e) {
                                Toast.makeText(activity, "Add network failed. Please check if SSID name is unique and already on the list", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    dialog.show();
                    unselectAll();
                }
                return true;
            }

        });

        remove.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean changed = false;

                for (int idx = list.getPreferenceCount() - 1; idx >= 0; idx--) {
                    Preference pref = list.getPreference(idx);
                    if (pref instanceof CheckBoxPreference) {
                        boolean status = ((CheckBoxPreference) pref).isChecked();
                        if (status) {
                            WiFiTethering item = db.getWifiTethering(Integer.valueOf(pref.getKey()));
                            if (db.removeWiFiTethering(Integer.parseInt(pref.getKey())) > 0) {
                                list.removePreference(pref);
                                changed = true;
                                String defaultSsid = prefs.getString("default.wifi.network", "");

                                if (defaultSsid.equals(item.getSsid())) {
                                    prefs.edit().remove("default.wifi.network");
                                    Utils.saveWifiConfiguration(activity, null);
                                    activity.sendBroadcast(new Intent(TetherIntents.WIFI_DEFAULT_REFRESH));
                                }
                            }
                        }
                    }
                }

                remove.setEnabled(list.getPreferenceCount() > CONST_ITEMS);
                modify.setEnabled(list.getPreferenceCount() > CONST_ITEMS);

                if (!changed) {
                    Toast.makeText(activity, "Please select any item", Toast.LENGTH_LONG).show();
                }
                return true;
            }

        });
    }

    private void setPreferenceCheckBox(Preference finalPref, String key, String title, String summary) {
        finalPref.setKey(key);
        finalPref.setTitle(title);
        finalPref.setSummary(summary);
        finalPref.setPersistent(false);
    }

    private void createCheckBoxPreference(PreferenceCategory list, MainActivity activity, String key, String title, String summary) {
        CheckBoxPreference item = new CheckBoxPreference(activity);
        setPreferenceCheckBox(item, key, title, summary);
        list.addPreference(item);
    }

    private void unselectAll() {
        final PreferenceCategory list = getPreferenceCategory("wifi.list");

        for (int idx = list.getPreferenceCount() - 1; idx >= 0; idx--) {
            Preference pref = list.getPreference(idx);
            if (pref instanceof CheckBoxPreference) {
                ((CheckBoxPreference) pref).setChecked(false);
            }
        }
    }
}
