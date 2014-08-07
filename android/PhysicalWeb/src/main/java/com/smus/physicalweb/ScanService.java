package com.smus.physicalweb;

/**
 * Created by abellajdel on 7/16/14.
 */

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import java.util.List;

public class ScanService extends Service {
    private Context context;
    private Intent intent;
    private DeviceManagerForService mDeviceManagerForService;


    @Override
    public void onCreate(){
        super.onCreate();
        context = this.getApplicationContext();
        mDeviceManagerForService = new DeviceManagerForService(this);
        mDeviceManagerForService.setIsAppInForeground(true);
    }


    @Override
    public int onStartCommand(final Intent _intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        intent = _intent;

        //We start a separate thread that will run the scanning tasks
        new Thread(new Runnable() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public void run() {
                theJob();
            }
        }).start();

        return START_STICKY;
    }

    private void theJob(){
        mDeviceManagerForService.startSearchingForDevices();
    }

    //Checking which app is in the foreground and comparing it to the physical web app
    //Returns true if physical web app in foreground, return false otherwise
    public boolean isForeground(String myPackage){
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        if(componentInfo.getPackageName().equals(myPackage)) return true;
        return false;
    }

    @Override
    public void onDestroy(){
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
