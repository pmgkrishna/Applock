package com.sara.applock.home;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Hariharan on 09-06-2017.
 */

public class App{
    private Drawable icon;
    private String appName;
    private String packageName;
    private boolean checked;
    private static int AppsCount;
    private static int LockedAppsCount;
    static List <String> advancedApps=new ArrayList<>();

    //Initialising list of Apps to be displayed in advanced section
    {
        advancedApps.add("com.android.settings");//Settings
        advancedApps.add("com.android.dialer");//Dialer
        advancedApps.add("com.android.contacts");//Contacts
        advancedApps.add("com.android.vending");//Play store
    }

    public App(Drawable icon,String appName,String packageName)
    {
        this.icon=icon;
        this.appName=appName;
        this.packageName=packageName;
    }
    public App(Drawable icon,String appName,String packageName,boolean isChecked)
    {
        this.icon=icon;
        this.appName=appName;
        this.packageName=packageName;
        this.checked=isChecked;
    }
    public static int getAppsCount() {
        return AppsCount;
    }
    public static int getLockedAppsCount() {
        return LockedAppsCount;
    }
    public Drawable getIcon() {
        return icon;
    }
    public String getAppName() {
        return appName;
    }
    public String getPackageName() {
        return packageName;
    }

    public static  List<App> getAdvancedApps(Context context)
    {
        boolean isChecked;
        String appName;
        String packageName;
        Drawable icon;
        DatabaseHelper databaseHelper=DatabaseHelper.getInstance(context);
        List<App> res = new ArrayList<>();
        List<PackageInfo> packageList = context.getPackageManager().getInstalledPackages(0);
        for (int packageCount = 0; packageCount < packageList.size(); packageCount++)
        {
            PackageInfo pack = packageList.get(packageCount);
            packageName = pack.applicationInfo.packageName;
            if (isLaunchablePackage(context,packageName) && !packageName.equals(context.getPackageName()) && advancedApps.contains(packageName))
            {
                appName = pack.applicationInfo.loadLabel(context.getPackageManager()).toString();
                icon = pack.applicationInfo.loadIcon(context.getPackageManager());
                if(databaseHelper.checkIfExists(packageName))
                {
                    isChecked=true;
                }
                else
                {
                    isChecked=false;
                }
                res.add(new App(icon,appName,packageName,isChecked));
            }
        }
        Collections.sort(res,AppComparator);
        return res;
    }

    public static List<App> getInstalledApps(Context context) {
        boolean isChecked;
        String appName;
        String packageName;
        Drawable icon;
        DatabaseHelper databaseHelper=DatabaseHelper.getInstance(context);
        List<App> res = new ArrayList<>();
        List<PackageInfo> packageList = context.getPackageManager().getInstalledPackages(0);
        for (int packageCount = 0; packageCount < packageList.size(); packageCount++)
        {
            PackageInfo pack = packageList.get(packageCount);
            packageName = pack.applicationInfo.packageName;
            if (isLaunchablePackage(context,packageName) && !packageName.equals(context.getPackageName()) && !advancedApps.contains(packageName))
            {
                appName = pack.applicationInfo.loadLabel(context.getPackageManager()).toString();
                icon = pack.applicationInfo.loadIcon(context.getPackageManager());
                if(databaseHelper.checkIfExists(packageName))
                {
                    isChecked=true;
                }
                else
                {
                    isChecked=false;
                }
                res.add(new App(icon,appName,packageName,isChecked));
            }
        }
        Collections.sort(res,AppComparator);
        return res;
    }
    private static boolean isLaunchablePackage(Context context,String packageName) {
         Intent intentOfStartActivity = context.getPackageManager().getLaunchIntentForPackage(packageName);
         return intentOfStartActivity != null;
    }

    public static  List<App> getLockedApps(Context context)
    {
        List <App> res=new ArrayList<>();
        DatabaseHelper dbHelper=DatabaseHelper.getInstance(context);
        Cursor result = dbHelper.getAllLockedApps();
        if(result.getCount() > 0)
        {
            while(result.moveToNext())
            {
                String appName = result.getString(1);
                byte[] iconByteArray = result.getBlob(0);
                Drawable icon = Conversion.convertToDrawable(context,iconByteArray);
                String packageName= result.getString(2);
                res.add(new App(icon, appName, packageName));
            }
        }
        LockedAppsCount =result.getCount();
        result.close();
        return res;
    }

    public boolean isChecked() {
        return checked;
    }
    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    //To sort the generalApps based on appName
    public static Comparator<App> AppComparator
            = new Comparator<App>() {
        @Override
        public int compare(App app1, App app2) {
            String appName1 = app1.getAppName().toUpperCase();
            String appName2 = app2.getAppName().toUpperCase();
            return appName1.compareTo(appName2);
        }
    };
}


