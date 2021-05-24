package com.team10.mc.SpotHOT.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.team10.mc.SpotHOT.R;
import com.team10.mc.SpotHOT.activity.MainActivity;
import com.team10.mc.SpotHOT.activity.ScheduleActivity;
import com.team10.mc.SpotHOT.db.Cron;
import com.team10.mc.SpotHOT.db.DBManager;


public class SchedulePreference extends Preference {
    private final DBManager db;
    private final PreferenceCategory parent;
    private final Cron cron;

    public SchedulePreference(PreferenceCategory parent, Cron cron, Context context) {
        super(context);
        this.cron = cron;
        this.parent = parent;
        db = DBManager.getInstance(getContext());
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        final ImageButton btnToogle = (ImageButton) view.findViewById(R.id.btnToggle);
        final ImageButton btnRemove = (ImageButton) view.findViewById(R.id.btnScheduleDelete);
        final LinearLayout middleLayout = (LinearLayout) view.findViewById(R.id.middleLayout);

        if (cron.getStatus() == Cron.STATUS.SCHED_OFF_DISABLED.getValue()) {
            btnToogle.setSelected(false);
        } else if (cron.getStatus() == Cron.STATUS.SCHED_OFF_ENABLED.getValue()) {
            btnToogle.setSelected(true);
        }

        btnToogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnToogle.setSelected(!btnToogle.isSelected());
                cron.toggle();
                db.addOrUpdateCron(cron);
            }
        });

        btnRemove.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (db.removeCron(cron.getId()) > 0) {
                    parent.removePreference(SchedulePreference.this);
                }
            }
        });

        middleLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ScheduleActivity.class);
                intent.putExtra("cronId", cron.getId());
                if (getContext() instanceof Activity) {
                    ((Activity) getContext()).startActivityForResult(intent, MainActivity.ON_CHANGE_SCHEDULE);
                }
            }
        });
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return li.inflate(R.layout.timer_add, parent, false);
    }
}
