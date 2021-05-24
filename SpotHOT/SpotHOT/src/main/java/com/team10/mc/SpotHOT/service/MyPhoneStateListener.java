package com.team10.mc.SpotHOT.service;

import android.content.Context;
import android.content.Intent;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;

import static com.team10.mc.SpotHOT.TetherIntents.CHANGE_CELL;



class MyPhoneStateListener extends PhoneStateListener {

    private final Context context;

    public MyPhoneStateListener(Context context) {
        this.context = context;
    }

    @Override
    public void onCellLocationChanged(CellLocation location) {
        super.onCellLocationChanged(location);
        context.sendBroadcast(new Intent(CHANGE_CELL));
    }

}