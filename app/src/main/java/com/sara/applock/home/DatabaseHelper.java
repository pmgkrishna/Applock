package com.sara.applock.home;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by Hariharan on 11-06-2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper mInstance = null;
    private static final String databaseName = "LockedAppsDatabase";
    private static final String lockedAppsTable = "LockedAppsTable";
    SQLiteDatabase database;
    Cursor result;
    public static DatabaseHelper getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }
    public DatabaseHelper(Context context) {
        super(context, databaseName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + lockedAppsTable +"(icon BLOB,appName varchar(50),packageName varchar(50))");
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}
    public boolean insertIntoLockedApps(byte[] icon,String appName,String packageName)
    {
        database=getReadableDatabase();
        ContentValues values=new ContentValues();
        values.put("icon",icon);
        values.put("appName",appName);
        values.put("packageName",packageName);
        long result=database.insert(lockedAppsTable,null,values);
        if(result==-1)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    public boolean checkIfExists(String packageName)
    {
        boolean exists=false;
        try {
            database = getReadableDatabase();
            result = database.rawQuery("SELECT * FROM " + lockedAppsTable + " WHERE package" +
                    "Name = '" + packageName + "'", null);
            exists=result.moveToNext();
        }
        catch (Exception e)
        {
            Log.d(TAG,e.getMessage());
        }
        finally {
            result.close();
        }
        return exists;
    }
    public Cursor getAllLockedApps()
    {
        database=getReadableDatabase();
        return database.rawQuery("SELECT * FROM " + lockedAppsTable,null);
    }
    public void removeFromLockedApps(String appName)
    {
        database=getReadableDatabase();
        database.execSQL("DELETE FROM " + lockedAppsTable + " WHERE appName = '" + appName + "'");
    }
    public void removeAllFromLockedApps()
    {
        database=getReadableDatabase();
        database.execSQL("DELETE FROM " + lockedAppsTable);
    }
    public byte[] getIconByteStream(String packageName)
    {
        database=getReadableDatabase();
        result=database.rawQuery("SELECT icon FROM " + lockedAppsTable +
                " where packageName='" + packageName + "'",null);
        if(result.moveToNext())
        {
            return result.getBlob(0);
        }
        return null;
    }
}
