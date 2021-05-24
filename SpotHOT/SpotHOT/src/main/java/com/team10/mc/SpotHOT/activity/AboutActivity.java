package com.team10.mc.SpotHOT.activity;

import android.app.Activity;

import android.os.Bundle;
import android.widget.TextView;
import java.util.Calendar;


import com.team10.mc.SpotHOT.R;


import java.util.Date;



public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);


        TextView textView = (TextView) findViewById(R.id.versionTextView);
        Date currentTime = Calendar.getInstance().getTime();
        String ct = currentTime.toString();
        textView.append("\n"+ct);
    }

}