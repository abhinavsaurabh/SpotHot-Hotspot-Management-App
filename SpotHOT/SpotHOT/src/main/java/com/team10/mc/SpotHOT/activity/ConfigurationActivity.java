package com.team10.mc.SpotHOT.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.preference.PreferenceManager;

import android.widget.Toast;


import com.team10.mc.SpotHOT.Utils;
import com.team10.mc.SpotHOT.receiver.TetheringWidgetProvider;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;


public class ConfigurationActivity extends Activity {

    private int mAppWidgetId;
    private boolean editMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_configuration);
        setResult(RESULT_CANCELED);
        init();
    }

    private void init() {
        editMode = getIntent().getExtras() != null && getIntent().getExtras().getBoolean("editMode", false);
        mAppWidgetId = Utils.getWidgetId(getIntent());
        if (mAppWidgetId == INVALID_APPWIDGET_ID) {
            //MyLog.e("WidgetAdd", "Cannot continue. Widget ID incorrect");
        }


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

    }

    private void handleOkButton() {
        saveWidget();
    }

    private void saveWidget() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (!editMode) {
            Toast.makeText(this, "Double tap on widget to modify settings", Toast.LENGTH_LONG).show();
        }

        Intent serviceIntent = new Intent(ConfigurationActivity.this, TetheringWidgetProvider.class);
        serviceIntent.putExtra(EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, serviceIntent);
        startService(serviceIntent);
        finish();
    }

    private String key(String key) {
        return "widget." + mAppWidgetId + "." + key;
    }
}
