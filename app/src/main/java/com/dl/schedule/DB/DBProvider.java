package com.dl.schedule.DB;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class DBProvider extends ContentProvider {
    private static final String DB_NAME = "schedule.db";
    private static final String DB_TABLE = "schedule";
    private static final String DB_TABLEMOTION = "motion";
    private static final int DB_VERSION = 1;

    private SQLiteDatabase db;
    private DBOpenHelper dbOpenHelper;

    private static final UriMatcher uriMatcher;
    private static final String AUTHORITY = "com.dl.schedule.DB.DBProvider";
    public static final String CONTENT_URI_STRING = "content://" + AUTHORITY + "/" + "schedule";
    public static final String CONTENT_URI_STRING_MOTION = "content://" + AUTHORITY + "/" + "motion";
    public static final Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
    public static final Uri CONTENT_URIMOTION = Uri.parse(CONTENT_URI_STRING_MOTION);

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "schedule", 1);
        uriMatcher.addURI(AUTHORITY, "schedule/#", 2);
        uriMatcher.addURI(AUTHORITY, "motion", 3);
        uriMatcher.addURI(AUTHORITY, "motion/#", 4);
    }

    public DBProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case 1:
                count = db.delete(DB_TABLE, selection, selectionArgs);
                break;
            case 2:
                String segment = uri.getPathSegments().get(1);
                db.delete(DB_TABLE, "_id" + "=" + segment, selectionArgs);
                break;
            case 3:
                count = db.delete(DB_TABLEMOTION, selection, selectionArgs);
                break;
            case 4:
                String segmentMotion = uri.getPathSegments().get(1);
                db.delete(DB_TABLEMOTION, "_id" + "=" + segmentMotion, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI:" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case 1:
                return "vnd.android.cursor.dir/vnd.com.dl.schedule.DB.DBProvider.schedule";
            case 2:
                return "vnd.android.cursor.item/vnd.com.dl.schedule.DB.DBProvider.schedule#";
            case 3:
                return "vnd.android.cursor.dir/vnd.com.dl.schedule.DB.DBProvider.motion";
            case 4:
                return "vnd.android.cursor.item/vnd.com.dl.schedule.DB.DBProvider.motion#";
            default:
                throw new IllegalArgumentException("Unkown uri:" + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (uriMatcher.match(uri)) {
            case 1:
                long id = db.insert(DB_TABLE, null, values);
                if (id > 0) {
                    Uri newUri = ContentUris.withAppendedId(CONTENT_URI, id);
                    getContext().getContentResolver().notifyChange(newUri, null);
                    return newUri;
                }
            case 3:
                long idMotion = db.insert(DB_TABLEMOTION, null, values);
                if (idMotion > 0) {
                    Uri newUri = ContentUris.withAppendedId(CONTENT_URIMOTION, idMotion);
                    getContext().getContentResolver().notifyChange(newUri, null);
                    return newUri;
                }
        }
        throw new SQLException("Failed to insert row into " + uri);

    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbOpenHelper = new DBOpenHelper(context, DB_NAME, null, DB_VERSION);
        db = dbOpenHelper.getWritableDatabase();

        if (db == null)
            return false;
        else
            return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case 1:
                qb.setTables(DB_TABLE);
                break;
            case 2:
                qb.setTables(DB_TABLE);
                qb.appendWhere("_id" + "=" + uri.getPathSegments().get(1));
                break;
            case 3:
                qb.setTables(DB_TABLEMOTION);
                break;
            case 4:
                qb.setTables(DB_TABLEMOTION);
                qb.appendWhere("_id" + "=" + uri.getPathSegments().get(1));
                break;
            default:
                break;
        }
        Cursor cursor = qb.query(db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count;
        switch (uriMatcher.match(uri)) {
            case 1:
                count = db.update(DB_TABLE, values, selection, selectionArgs);
                break;
            case 2:
                String segment = uri.getPathSegments().get(1);
                count = db.update(DB_TABLE, values, "_id" + "=" + segment, selectionArgs);
                break;
            case 3:
                count = db.update(DB_TABLEMOTION, values, selection, selectionArgs);
                break;
            case 4:
                String segmentMotion = uri.getPathSegments().get(1);
                count = db.update(DB_TABLEMOTION, values, "_id"+"="+segmentMotion, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknow URI:" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private static class DBOpenHelper extends SQLiteOpenHelper {

        public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        private static final String DB_CREATE = "create table " +
                DB_TABLE + " (" + "_id" + " integer primary key autoincrement, " +
                "localDate" + " text not null, " + "time" + " text not null, " + "event" + " text not null);";
        private static final String DB_CREATEMOTION = "create table " +
                DB_TABLEMOTION + " (" + "_id" + " integer primary key autoincrement, " +
                "localDate"+ " text not null, " + "flagDis"+ " text not null, " +"goal"+" text not null,"+" distance " + " double);";
        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(DB_CREATE);
            _db.execSQL(DB_CREATEMOTION);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
            _db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
            _db.execSQL("DROP TABLE IF EXISTS " + DB_TABLEMOTION);
            onCreate(_db);
        }
    }
}