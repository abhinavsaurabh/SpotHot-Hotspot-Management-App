package com.team10.mc.SpotHOT.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.team10.mc.SpotHOT.R;
import com.team10.mc.SpotHOT.db.Cron;
import com.team10.mc.SpotHOT.db.DBManager;

import java.util.Calendar;


public class ScheduleActivity extends Activity {
    private final int[] buttons = {R.id.btnMonday, R.id.btnTuesday, R.id.btnWednesday, R.id.btnThursday, R.id.btnFriday, R.id.btnSaturday, R.id.btnSunday};
    private DBManager db;
    private TimePicker timeOff, timeOn;
    private CheckBox chkOff, chkOn;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer);
        db = DBManager.getInstance(getApplicationContext());
        timeOff = (TimePicker) findViewById(R.id.scheduleTimeOff);
        timeOn = (TimePicker) findViewById(R.id.scheduleTimeOn);
        chkOff = (CheckBox) findViewById(R.id.chkScheduleOff);
        chkOn = (CheckBox) findViewById(R.id.chkScheduleOn);

        chkOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked && !chkOn.isChecked()) {
                    chkOff.setChecked(true);
                } else {
                    timeOff.setEnabled(isChecked);
                }
            }
        });
        chkOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked && !chkOff.isChecked()) {
                    chkOn.setChecked(true);
                } else {
                    timeOn.setEnabled(isChecked);
                }
            }
        });

        timeOff.setIs24HourView(DateFormat.is24HourFormat(this));
        timeOn.setIs24HourView(DateFormat.is24HourFormat(this));
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        timeOff.setCurrentHour(hour);
        timeOn.setCurrentHour(hour);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            timeOff.setSaveFromParentEnabled(false);
            timeOn.setSaveFromParentEnabled(false);
            timeOff.setSaveEnabled(true);
            timeOn.setSaveEnabled(true);
        }
        readData();

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        Button btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (insertSchedule()) {
                    setResult(RESULT_OK, new Intent());
                    finish();
                }
            }
        });
    }

    private void readData() {
        Intent intent = getIntent();
        if (intent.getIntExtra("cronId", 0) > 0) {
            Cron cron = db.getCron(intent.getIntExtra("cronId", 0));
            id = cron.getId();

            chkOff.setChecked(cron.getHourOff() != -1);
            chkOn.setChecked(cron.getHourOn() != -1);
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            timeOff.setCurrentHour(cron.getHourOff() != -1 ? cron.getHourOff() : hour);
            timeOn.setCurrentHour(cron.getHourOn() != -1 ? cron.getHourOn() : hour);
            timeOff.setCurrentMinute(cron.getMinOff());
            timeOn.setCurrentMinute(cron.getMinOn());
            String binary = String.format("%7s", Integer.toBinaryString(cron.getMask())).replace(' ', '0');
            for (int day = 0; day < buttons.length; day++) {
                ToggleButton button = (ToggleButton) findViewById(buttons[6 - day]);
                button.setChecked(binary.substring(day, day + 1).equals("1"));
            }
        }
    }

    private boolean insertSchedule() {
        timeOff.clearFocus();
        timeOn.clearFocus();
        ToggleButton[] daysOfWeek = new ToggleButton[7];
        int mask = 0;
        for (int day = 0; day < 7; day++) {
            daysOfWeek[day] = (ToggleButton) findViewById(buttons[day]);
            if (daysOfWeek[day].isChecked()) {
                mask += Math.pow(2, day);
            }
        }

        if (mask == 0) {
            Toast.makeText(getApplicationContext(), "You need to select at least one day!", Toast.LENGTH_LONG).show();
            return false;
        }

        Cron cron = new Cron(
                timeOff.isEnabled() ? timeOff.getCurrentHour() : -1,
                timeOff.isEnabled() ? timeOff.getCurrentMinute() : 0,
                timeOn.isEnabled() ? timeOn.getCurrentHour() : -1,
                timeOn.isEnabled() ? timeOn.getCurrentMinute() : 0,
                mask, Cron.STATUS.SCHED_OFF_ENABLED.getValue());
        cron.setId(id);
        if (db.addOrUpdateCron(cron) <= 0) {
            Toast.makeText(getApplicationContext(), "Cannot add the same schedule items more than once!", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
}
