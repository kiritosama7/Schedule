package com.dl.schedule.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAdapter {
    private static final String DB_NAME = "schedule.db";
    private static final int DB_VERSION = 1;

    private static final String DB_TABLE = "schedule";

    public static final String KEY_ID = "_id";
    public static final String KEY_LOCALDATE = "localDate";
    public static final String KEY_TIME = "time";
    public static final String KEY_EVENT = "event";

    private static final String DB_TABLEMOTION = "motion";

    public static final String KEY_IDMOTION = "_id";
    public static final String KEY_LOCALDATEMOTION = "localDate";
    public static final String KEY_DISTANCEMOTION  = "distance";
    public static final String KEY_FLAGDIS  = "flagDis";
    public static final String  KEY_GOAL = "goal";


    private SQLiteDatabase db;
    private final Context context;
    private DBOpenHelper dbOpenHelper;

    public DBAdapter(Context context) {
        this.context = context;
    }

    public void open() throws SQLiteException {
        dbOpenHelper = new DBOpenHelper(context, DB_NAME, null, DB_VERSION);
        try {
            db = dbOpenHelper.getWritableDatabase();
        }catch (SQLiteException ex) {
            db = dbOpenHelper.getReadableDatabase();
        }
    }

    public void close() {
        if (db != null){
            db.close();
            db = null;
        }
    }

    private static class DBOpenHelper extends SQLiteOpenHelper {

        public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
            super(context, name, factory, version);
        }
        private static final String DB_CREATE = "create table " +
                DB_TABLE + " (" + KEY_ID + " integer primary key autoincrement, " +
                KEY_LOCALDATE+ " text not null, "  + KEY_TIME+ " text not null," + KEY_EVENT + " text not null);";
        private static final String DB_CREATEMOTION = "create table " +
                DB_TABLEMOTION + " (" + KEY_IDMOTION + " integer primary key autoincrement, " +
                KEY_LOCALDATEMOTION+ " text not null, " + KEY_FLAGDIS+ " text not null," +  KEY_GOAL+ " text not null,"+ KEY_DISTANCEMOTION+ " double);";
        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DB_CREATEMOTION);
            db.execSQL(DB_CREATE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLEMOTION);
            onCreate(db);
        }
    }

    public long insert(Schedule schedule) {
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_LOCALDATE, schedule.getLocalDate());
        newValues.put(KEY_TIME, schedule.getTime());
        newValues.put(KEY_EVENT, schedule.getEvent());

        return db.insert(DB_TABLE, null, newValues);
    }
    public long insertMotion(Motion motion) {
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_LOCALDATEMOTION, motion.getLocalDate());
        newValues.put(KEY_DISTANCEMOTION, motion.getDistance());
        newValues.put(KEY_FLAGDIS, motion.getFlagDis());
        newValues.put(KEY_GOAL, motion.getGoal());

        return db.insert(DB_TABLEMOTION, null, newValues);
    }

    public long deleteAllData() {
        return db.delete(DB_TABLE, null, null);
    }
    public long deleteAllDataMotion() {
        return db.delete(DB_TABLEMOTION, null, null);
    }

    public long deleteOneData(long id) {
        return db.delete(DB_TABLE,  KEY_ID + "=" + id, null);
    }
    public long deleteOneDataMotion(long id) {
        return db.delete(DB_TABLEMOTION,  KEY_IDMOTION + "=" + id, null);
    }

    public long updateOneData(long id , Schedule schedule){
        ContentValues updateValues = new ContentValues();
        updateValues.put(KEY_LOCALDATE, schedule.getLocalDate());
        updateValues.put(KEY_TIME, schedule.getTime());
        updateValues.put(KEY_EVENT, schedule.getEvent());


        return db.update(DB_TABLE, updateValues,  KEY_ID +"="+ id, null);
    }
    public long updateOneDataMotion(long id , Motion motion){
        ContentValues updateValues = new ContentValues();
        updateValues.put(KEY_LOCALDATEMOTION, motion.getLocalDate());
        updateValues.put(KEY_DISTANCEMOTION, motion.getDistance());
        updateValues.put(KEY_FLAGDIS, motion.getFlagDis());
        updateValues.put(KEY_GOAL, motion.getGoal());

        return db.update(DB_TABLEMOTION, updateValues,  KEY_IDMOTION +"="+ id, null);
    }

    private Schedule[] ConvertToPeople(Cursor cursor){
        int resultCounts = cursor.getCount();
        if (resultCounts == 0 || !cursor.moveToFirst()){
            return null;
        }
        Schedule[] schedules = new Schedule[resultCounts];
        for (int i = 0 ; i<resultCounts; i++){
            schedules[i] = new Schedule();
            schedules[i].setID(cursor.getInt(0));
            schedules[i].setLocalDate(cursor.getString(cursor.getColumnIndex(KEY_LOCALDATE)));
            schedules[i].setTime(cursor.getString(cursor.getColumnIndex(KEY_TIME)));
            schedules[i].setEvent(cursor.getString(cursor.getColumnIndex(KEY_EVENT)));

            cursor.moveToNext();
        }
        return schedules;
    }
    private Motion[] ConvertToMotion(Cursor cursor){
        int resultCounts = cursor.getCount();
        if (resultCounts == 0 || !cursor.moveToFirst()){
            return null;
        }
        Motion[] motions = new Motion[resultCounts];
        for (int i = 0 ; i<resultCounts; i++){
            motions[i] = new Motion();
            motions[i].setID(cursor.getInt(0));
            motions[i].setLocalDate(cursor.getString(cursor.getColumnIndex(KEY_LOCALDATEMOTION)));
            motions[i].setDistance(cursor.getDouble(cursor.getColumnIndex(KEY_DISTANCEMOTION)));
            motions[i].setFlagDis(cursor.getString(cursor.getColumnIndex(KEY_FLAGDIS)));
            motions[i].setGoal(cursor.getString(cursor.getColumnIndex(KEY_GOAL)));

            cursor.moveToNext();
        }
        return motions;
    }
    public Schedule[] queryOneData(long id) {
        Cursor results =  db.query(DB_TABLE, new String[] { KEY_ID, KEY_LOCALDATE, KEY_TIME,KEY_EVENT},
                KEY_ID + "=" + id, null, null, null, null);
        return ConvertToPeople(results);
    }
    public Motion[] queryOneDataMotion(long id) {
        Cursor results =  db.query(DB_TABLEMOTION, new String[] { KEY_IDMOTION, KEY_LOCALDATEMOTION, KEY_DISTANCEMOTION,KEY_FLAGDIS,KEY_GOAL},
                KEY_IDMOTION + "=" + id, null, null, null, null);
        return ConvertToMotion(results);
    }

    public Schedule[] queryAllData() {
        Cursor results = db.query(DB_TABLE, new String[] { KEY_ID, KEY_LOCALDATE, KEY_TIME,KEY_EVENT,},
                null, null, null, null, null);
        return ConvertToPeople(results);
    }
    public Motion[] queryAllDataMotion() {

        Cursor results = db.query(DB_TABLEMOTION, new String[] { KEY_IDMOTION, KEY_LOCALDATEMOTION, KEY_DISTANCEMOTION,KEY_FLAGDIS,KEY_GOAL},
                null, null, null, null, null);
        return ConvertToMotion(results);
    }

    public Schedule[] queryDateData(String date) {
        Cursor results =  db.query(DB_TABLE, new String[] { KEY_ID, KEY_LOCALDATE, KEY_TIME,KEY_EVENT},
                KEY_LOCALDATE+ "=" + "'"+date+"'", null, null, null, KEY_TIME);
        return ConvertToPeople(results);
    }
    public Motion[] queryDateDataMotion(String date) {

        Cursor results =  db.query(DB_TABLEMOTION, new String[] { KEY_IDMOTION, KEY_LOCALDATEMOTION, KEY_DISTANCEMOTION,KEY_FLAGDIS,KEY_GOAL},
                KEY_LOCALDATEMOTION+ "=" + "'"+date+"'", null, null, null,null);
        return ConvertToMotion(results);
    }
    public Motion[] queryFlagDataMotion(String FlagDis) {
        Cursor results =  db.query(DB_TABLEMOTION, new String[] { KEY_IDMOTION, KEY_LOCALDATEMOTION, KEY_DISTANCEMOTION,KEY_FLAGDIS,KEY_GOAL},
                KEY_FLAGDIS+ "=" + "'"+FlagDis+"'", null, null, null,null);
        return ConvertToMotion(results);
    }
}
