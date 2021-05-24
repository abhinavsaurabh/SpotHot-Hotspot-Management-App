package com.team10.mc.SpotHOT.activity.helpers;

import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.widget.Toast;

import com.team10.mc.SpotHOT.Utils;
import com.team10.mc.SpotHOT.activity.MainActivity;
import com.team10.mc.SpotHOT.activity.ScheduleActivity;
import com.team10.mc.SpotHOT.db.Cron;
import com.team10.mc.SpotHOT.db.DBManager;
import com.team10.mc.SpotHOT.ui.SchedulePreference;

import java.util.List;
import java.util.Locale;

import static com.team10.mc.SpotHOT.AppProperties.MAX_SCHEDULED_ITEMS;
import static com.team10.mc.SpotHOT.activity.MainActivity.ON_CHANGE_SCHEDULE;


public class RegisterSchedulerListenerHelper extends AbstractRegisterHelper {

    public RegisterSchedulerListenerHelper(MainActivity activity) {
        super(activity);
    }

    @Override
    public void registerUIListeners() {
        getPreferenceScreen("scheduler.add").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                PreferenceCategory cat = getPreferenceCategory("scheduled.shutdown.list");
                if (cat.getPreferenceCount() >= MAX_SCHEDULED_ITEMS) {
                    Toast.makeText(activity, "You cannot add more than " + MAX_SCHEDULED_ITEMS + " schedule items!", Toast.LENGTH_LONG).show();
                    return false;
                }
                activity.startActivityForResult(new Intent(activity, ScheduleActivity.class), ON_CHANGE_SCHEDULE);
                return true;
            }
        });
    }

    @Override
    public void prepare() {
        final PreferenceCategory p = getPreferenceCategory("scheduled.shutdown.list");
        List<Cron> list = DBManager.getInstance(activity).getCrons();

        p.removeAll();
        for (final Cron cron : list) {
            final SchedulePreference ps = new SchedulePreference(p, cron, activity);
            String title;
            if (cron.getHourOff() == -1) {
                title = String.format(Locale.ENGLISH, "ON at %02d:%02d", cron.getHourOn(), cron.getMinOn());
            } else if (cron.getHourOn() == -1) {
                title = String.format(Locale.ENGLISH, "OFF at %02d:%02d", cron.getHourOff(), cron.getMinOff());
            } else {
                title = String.format(Locale.ENGLISH, "%02d:%02d - %02d:%02d", cron.getHourOff(), cron.getMinOff(), cron.getHourOn(), cron.getMinOn());
            }
            ps.setPersistent(false);
            ps.setTitle(title);
            ps.setSummary(Utils.maskToDays(cron.getMask()));
            p.addPreference(ps);
        }
    }

}
