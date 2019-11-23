package com.foxconn.mac1.zebraprinter.Utils;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class PackageUtils {

    /**
     * get application name
     * @param context
     * @return
     */
    public static String getApplicationName(Context context){
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            int labelRes = applicationInfo.labelRes;
            String applicationName = context.getResources().getString(labelRes);
            return applicationName;
        } catch (PackageManager.NameNotFoundException e) {
            return "Error:(getApplicationName) - " + e.getMessage();
        }
    }

    /**
     * get application version name
     * @param context
     * @return
     */
    public static String getVersionName(Context context){
        PackageManager packageManager = context.getPackageManager();

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            String applicationVersionName = packageInfo.versionName;
            return applicationVersionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "Error:(getVersionName) - " + e.getMessage();
        }
    }

    /**
     * get application version code
     * @param context
     * @return
     */
    public static String getVersionCode(Context context){
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo  = packageManager.getPackageInfo(context.getPackageName(), 0);
            String applicationVersionCode = String.valueOf(packageInfo.versionCode);
            return applicationVersionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return "Error:(getVersionCode) - " + e.getMessage();
        }
    }
}
