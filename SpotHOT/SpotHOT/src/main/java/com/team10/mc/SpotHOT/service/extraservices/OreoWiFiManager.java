package com.team10.mc.SpotHOT.service.extraservices;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;



import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@RequiresApi(api = Build.VERSION_CODES.O)
public class OreoWiFiManager {


    private static final int TETHERING_WIFI = 0;

    private static final String TAG = OreoWiFiManager.class.getSimpleName();

    private Context mContext;

    public OreoWiFiManager(Context c) {
        mContext = c;
    }

    public void startTethering(OnStartTetheringCallback callback, Handler handler) {

        CallbackMaker cm = new CallbackMaker(mContext, callback);
        Class<?> mSystemCallbackClazz = cm.getCallBackClass();
        Object mSystemCallback = null;
        try {
            Constructor constructor = mSystemCallbackClazz.getDeclaredConstructor(int.class);
            mSystemCallback = constructor.newInstance(0);

        } catch (ReflectiveOperationException e) {
            //MyLog.e(TAG, e);
        }

        ConnectivityManager manager = mContext.getApplicationContext().getSystemService(ConnectivityManager.class);
        Class callbackClass = null;
        try {
            try {
                callbackClass = Class.forName("android.net.ConnectivityManager$OnStartTetheringCallback");
            } catch (ClassNotFoundException e) {
                //MyLog.e(TAG, e);
            }

            Method method = manager.getClass().getDeclaredMethod("startTethering", int.class, boolean.class, callbackClass, Handler.class);

            if (method == null) {
                Log.e(TAG, "ConnectivityManager:startTethering method not found");
            } else {
                method.invoke(manager, TETHERING_WIFI, false, mSystemCallback, handler);
            }
        } catch (ReflectiveOperationException e) {
            //MyLog.e(TAG, e);
        }
    }

    public void stopTethering() {

        ConnectivityManager manager = mContext.getApplicationContext().getSystemService(ConnectivityManager.class);

        try {
            Method method = manager.getClass().getDeclaredMethod("stopTethering", int.class);

            if (method == null) {
                Log.e(TAG, "stopTetheringMethod is null");
            } else {
                method.invoke(manager, TETHERING_WIFI);
            }
        } catch (ReflectiveOperationException e) {
            //MyLog.e(TAG, e);
        }
    }
}
