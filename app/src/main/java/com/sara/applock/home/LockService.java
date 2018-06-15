package com.sara.applock.home;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.sara.applock.R;
import com.sara.applock.pin.LockScreenActivity;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

/**
 * Created by pmgkrishna on 11-06-2017.
 */

public class LockService extends Service {
    String CURRENT_PACKAGE_NAME;
    SharedPreferences lockSharedPreference;
    SharedPreferences.Editor editor;
    public static LockService instance;
    private NotificationManager notificationManager;
    private final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setupNotifications();
        showNotification();
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            checkRunningApps();
        }
        else {
            scheduleMethod();
        }
        CURRENT_PACKAGE_NAME = getApplicationContext().getPackageName();
        instance = this;
        return START_STICKY;
    }

    private void scheduleMethod() {
        ScheduledExecutorService scheduler = Executors
                .newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                checkRunningApps();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void checkRunningApps() {
        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        final String TopApp = retrieveTopApp();
        lockSharedPreference = getSharedPreferences("Lock_Preference", MODE_PRIVATE);
        if (!TopApp.equals(getApplicationContext().getPackageName()) &&
                databaseHelper.checkIfExists(TopApp))
        {
            if (lockSharedPreference.getString(TopApp, "").equals("")) {
                editor = lockSharedPreference.edit();
                editor.clear();
                editor.putString(TopApp, "Locked");
                editor.commit();
            }
            if (!lockSharedPreference.getString(TopApp, "").equals("Unlocked")) {
                Intent intent = new Intent(this, LockScreenActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                if(!TopApp.equals(getApplicationContext().getPackageName())) {
                    intent.putExtra("Icon", databaseHelper.getIconByteStream(TopApp));
                }
                intent.putExtra("packageName", TopApp);
                startActivity(intent);
            }
        } else if (!TopApp.equals(getApplicationContext().getPackageName()) &&
                !databaseHelper.checkIfExists(TopApp)) {
            editor = lockSharedPreference.edit();
            editor.clear().commit();
        }
        databaseHelper.close();
    }
    private String retrieveTopApp() {
        String currentApp = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> applist = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            if (applist != null && applist.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : applist) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
            Log.i(TAG, "Current App in foreground is: " + currentApp);
            return currentApp;
        } else {
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            currentApp = (manager.getRunningTasks(1).get(0)).topActivity.getPackageName();
            Log.i(TAG, "Current App in foreground is: " + currentApp);
            return currentApp;
        }
    }
    public static void stop() {
        if (instance != null) {
            instance.stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(1);
    }

    private void setupNotifications() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, LockScreenActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                0);
        notificationBuilder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setOngoing(true);
    }
    private void showNotification() {
        notificationBuilder
                .setContentTitle("Lock service running");
        if (notificationManager != null) {
            notificationManager.notify(1, notificationBuilder.build());
        }
    }
}
