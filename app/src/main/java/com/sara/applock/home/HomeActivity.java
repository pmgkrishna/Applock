package com.sara.applock.home;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.sara.applock.R;
import com.sara.applock.pin.LockScreenActivity;

import java.util.Calendar;
import java.util.List;


public class HomeActivity extends AppCompatActivity  implements ItemClickListener {

    List<App> generalApps,advancedApps;
    RecyclerView recyclerViewAdvanced, recyclerViewGeneral;
    AppsAdapter appsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        recyclerViewAdvanced = (RecyclerView) findViewById(R.id.recyclerViewAdvanced);
        recyclerViewAdvanced.setNestedScrollingEnabled(false);
        recyclerViewGeneral = (RecyclerView) findViewById(R.id.recyclerViewGeneral);
        recyclerViewGeneral.setNestedScrollingEnabled(false);
        setRecyclerViewAdvanced();
        setRecyclerViewGeneral();
    }
    public void setRecyclerViewAdvanced()
    {
        advancedApps = App.getAdvancedApps(getApplicationContext());
        // Create adapter passing in the sample user data
        appsAdapter = new AppsAdapter(getApplicationContext(), advancedApps);
        // Attach the adapter to the recyclerview to populate items
        recyclerViewAdvanced.setAdapter(appsAdapter);
        // Set layout manager to position the items
        recyclerViewAdvanced.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        appsAdapter.setClickListener(this);
        //globalClass.viewPagerAdapter.notifyDataSetChanged();
    }
    public void setRecyclerViewGeneral()
    {
        generalApps = App.getInstalledApps(getApplicationContext());
        // Create adapter passing in the sample user data
        appsAdapter = new AppsAdapter(getApplicationContext(), generalApps);
        // Attach the adapter to the recyclerview to populate items
        recyclerViewGeneral.setAdapter(appsAdapter);
        // Set layout manager to position the items
        recyclerViewGeneral.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        appsAdapter.setClickListener(this);
        //globalClass.viewPagerAdapter.notifyDataSetChanged();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.changePassword:
                SharedPreferences preferences=getSharedPreferences("PINPREFERENCES", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=preferences.edit();
                editor.putBoolean("CHANGE_PASSWORD",true);
                editor.apply();
                startActivity(new Intent(this, LockScreenActivity.class));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view, int position) {
        App app;
        CheckBox ch=(CheckBox) view;
        RecyclerView rv=(RecyclerView)ch.getParent().getParent().getParent().getParent().getParent();
        //Inorder to get which RecyclerView is clicked
        //Checkbox --> LinearLayout --> RelativeLayout --> CardView --> LinearLayout --> RecyclerView (refer apps_row.xml)
        if(rv.getId()==R.id.recyclerViewAdvanced)
        {
           app = advancedApps.get(position);
        }
        else
        {
            app = generalApps.get(position);
        }
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        if(ch.isChecked())
        {
            if (!app.getPackageName().equals(getPackageName()) &&
                    !dbHelper.checkIfExists(app.getPackageName()))
            {
                app.setChecked(true);
                byte[] iconByteArray = Conversion.convertToByteArray(app.getIcon());
                boolean result = dbHelper.insertIntoLockedApps(iconByteArray,app.getAppName(),app.getPackageName());
                dbHelper.close();
                if (result)
                {
                    Toast.makeText(this, app.getAppName() + " Locked", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(this,"Failed to lock", Toast.LENGTH_SHORT).show();
                }
                setRecyclerViewAdvanced();
            }
        }
        else
        {
            app.setChecked(false);
            dbHelper.removeFromLockedApps(app.getAppName());
            dbHelper.close();
            Toast.makeText(this,app.getAppName() + " Unlocked",Toast.LENGTH_SHORT).show();
            setRecyclerViewAdvanced();
        }
        if (Build.VERSION.SDK_INT!=Build.VERSION_CODES.KITKAT && !isAccessGranted()) {
            Toast.makeText(this,"Please give usage access",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //to reflect any changes in the installed generalApps count
        setRecyclerViewAdvanced();
        initializeLockService();
    }
    private boolean isAccessGranted() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    private void initializeLockService()
    {
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            Intent LockServiceIntent = new Intent
                    (this, LockService.class);
            PendingIntent pendingIntent = PendingIntent.getService
                    (this, 0, LockServiceIntent, 0);
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
            Calendar calendar = Calendar.getInstance();
            alarmManager.setRepeating
                    (AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 100, pendingIntent);
        }
        else
        {
            /*Since the interval time value in alarmManager will be forced upto 60000 milliseconds as of Android 5.1,
            cannot use alarm manager to run the service every 100 milliseconds*/
            startService(new Intent(this, LockService.class));
        }
    }
    @Override
    protected void onPause() {
        super.onPause();finish();
    }
}
