package com.team10.mc.SpotHOT.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class DBManager extends SQLiteOpenHelper {

    private static final String DB_NAME = "autowifi.db";
    private static final int DB_VERSION = 8;
    //TODO to remove because of Lint performance warning: StaticFieldLeak: Static Field Leaks
    private final Context context;

    private static DBManager instance;

    public static synchronized DBManager getInstance(Context context) {
        if (instance == null) {
            instance = new DBManager(context);
        }
        return instance;
    }

    private DBManager(Context context, String name) {
        super(context, name, null, DB_VERSION);
        this.context = context;
    }

    private DBManager(Context context) {
        this(context, DB_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // CREATE TABLE

        db.execSQL("create table CRON(id INTEGER PRIMARY KEY, hourOff INTEGER, minOff INTEGER, hourOn INTEGER, minOn INTEGER, mask INTEGER, status INTEGER)");


        db.execSQL("create table WIFI_TETHERING(id INTEGER PRIMARY KEY, ssid VARCHAR(32), type INTEGER, password VARCHAR(63), channel INTEGER, hidden INTEGER, status INTEGER)");
        // CREATE INDEX

        db.execSQL("create unique index CRON_UNIQUE_IDX on cron(hourOff ,minOff , hourOn, minOn, mask)");


        db.execSQL("create unique index WIFI_TETHERING_UNIQUE_IDX on wifi_tethering(ssid)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 4) {
            // DROP TABLE
            db.execSQL("drop table IF EXISTS CRON");
            // CREATE TABLE
            db.execSQL("create table CRON(id INTEGER PRIMARY KEY, hourOff INTEGER, minOff INTEGER, hourOn INTEGER, minOn INTEGER, mask INTEGER, status INTEGER)");
            // CREATE INDEX
            db.execSQL("create unique index CRON_UNIQUE_IDX on cron(hourOff ,minOff , hourOn, minOn, mask)");
        }



        if (oldVersion < 8) {
            // CREATE TABLE
            db.execSQL("create table WIFI_TETHERING(id INTEGER PRIMARY KEY, ssid VARCHAR(32), type INTEGER, password VARCHAR(63), channel INTEGER, hidden INTEGER, status INTEGER)");
            // CREATE INDEX
            db.execSQL("create unique index WIFI_TETHERING_UNIQUE_IDX on wifi_tethering(ssid)");
        }
        //MyLog.i("DBManager", "DB upgraded from version " + oldVersion + " to " + newVersion);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.execSQL("PRAGMA foreign_keys=ON");
    }





    public boolean isOnWhiteList(final String ssn) {
        boolean res;
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery("SELECT 1 FROM SIMCARD where ssn = '" + ssn + "'", null);
            res = cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return res;
    }



    public List<Cron> getCrons() {
        List<Cron> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().query(Cron.NAME, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    Cron cron = new Cron(cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5), cursor.getInt(6));
                    cron.setId(cursor.getInt(0));
                    list.add(cron);
                }
                while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        Collections.sort(list, new Comparator<Cron>() {
            @Override
            public int compare(Cron lhs, Cron rhs) {
                int diffOff = 60 * (lhs.getHourOff() - rhs.getHourOff()) + (lhs.getMinOff() - rhs.getMinOff());
                int diffOn = 60 * (lhs.getHourOn() - rhs.getHourOn()) + (lhs.getMinOn() - rhs.getMinOn());
                return diffOff > 0 ? diffOff : diffOn;
            }
        });
        return list;
    }

    public Cron getCron(int id) {
        Cursor cursor = null;
        Cron cron = null;
        try {
            cursor = getReadableDatabase().query(Cron.NAME, null, "id=" + id, null, null, null, null);
            if (cursor.moveToFirst()) {
                cron = new Cron(cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5), cursor.getInt(6));
                cron.setId(cursor.getInt(0));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return cron;
    }

    public int removeCron(final int id) {
        return getWritableDatabase().delete(Cron.NAME, "id=" + id, null);
    }

    public long addOrUpdateCron(Cron cron) {
        ContentValues content = new ContentValues();
        content.put("hourOff", cron.getHourOff());
        content.put("minOff", cron.getMinOff());
        content.put("hourOn", cron.getHourOn());
        content.put("minOn", cron.getMinOn());
        content.put("mask", cron.getMask());
        content.put("status", cron.getStatus());
        return addOrUpdate(cron.getId(), Cron.NAME, content);
    }

    public void removeAllData() {

        getWritableDatabase().delete(Cron.NAME, null, null);

        getWritableDatabase().delete(WiFiTethering.NAME, null, null);
    }













    private long addOrUpdate(int id, String name, ContentValues content) {
        if (id > 0) {
            return getWritableDatabase().update(name, content, "id=" + id, null);
        } else {
            return getWritableDatabase().insert(name, null, content);
        }
    }













    public List<WiFiTethering> readWiFiTethering() {
        List<WiFiTethering> list;
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery("SELECT id, ssid, type, password, channel, hidden, status FROM WIFI_TETHERING order by status desc, ssid", null);
            list = new ArrayList<>(cursor.getCount());
            if (cursor.moveToFirst()) {
                do {
                    WiFiTethering p = new WiFiTethering(cursor.getString(1), WiFiTethering.SECURITY_TYPE.valueOf(cursor.getInt(2)), cursor.getString(3), cursor.getInt(4), cursor.getInt(5) == 1, cursor.getInt(6));
                    p.setId(cursor.getInt(0));
                    list.add(p);
                }
                while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    public WiFiTethering getWifiTethering(int id) {
        Cursor cursor = null;
        WiFiTethering wiFiTethering = null;
        try {
            cursor = getReadableDatabase().query(WiFiTethering.NAME, null, "id=" + id, null, null, null, null);
            if (cursor.moveToFirst()) {
                wiFiTethering = new WiFiTethering(cursor.getString(1), WiFiTethering.SECURITY_TYPE.valueOf(cursor.getInt(2)), cursor.getString(3), cursor.getInt(4), cursor.getInt(5) == 1, cursor.getInt(6));
                wiFiTethering.setId(cursor.getInt(0));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return wiFiTethering;
    }

    public int removeWiFiTethering(int id) {
        return getWritableDatabase().delete(WiFiTethering.NAME, "id=" + id, null);
    }

    public long addOrUpdateWiFiTethering(WiFiTethering wiFiTethering) {
        ContentValues content = new ContentValues();
        content.put("ssid", wiFiTethering.getSsid());
        content.put("type", wiFiTethering.getType().getCode());
        content.put("password", wiFiTethering.getPassword());
        content.put("channel", wiFiTethering.getChannel());
        content.put("status", wiFiTethering.getStatus());
        content.put("hidden", wiFiTethering.isHidden());
        return addOrUpdate(wiFiTethering.getId(), WiFiTethering.NAME, content);
    }
}
