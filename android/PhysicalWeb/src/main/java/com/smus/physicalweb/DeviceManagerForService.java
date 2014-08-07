package com.smus.physicalweb;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Keeps track of all devices nearby.
 *
 * Posts notifications when a new device is near, or if an old device is no
 * longer nearby.
 *
 * Created by smus on 1/24/14.
 */
public class DeviceManagerForService extends NearbyDeviceManager {
    private int REQUEST_ENABLE_BT = 0;
    private boolean isDeviceActive;
    private boolean isAppRunning;
    private boolean isAppInForeground;
    private Service service;
    private Map<String, NearbyDevice> curentDevices;//This will hold the current detected devices
    //private NotificationManager notificationManager;
    private NotificationPoster notificationPoster;



    /**
     * The public interface of this class follows:
     */
    DeviceManagerForService(Service _service) {
        super(_service);
        service = _service;
        curentDevices = new HashMap<String, NearbyDevice>();

        //Initializing the notificationManager
        //notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationPoster = new NotificationPoster(service);

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Todo: decide what to do when bluetooth is off
            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //_service.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    //We might need to call this when the device goes to sleep
    public void stopSearchingForDevices() {
        assert mIsSearching;
        mIsSearching = false;
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        // Stop expired device timer.
        mExpireTimer.cancel();
        mScanTimer.cancel();
    }

    //This is called from mSearchTask define in parent class
    @Override
    protected void scanDevices(){
        isDeviceActive = powerManager.isScreenOn();
        isForeground("com.smus.physicalweb");
        if (!isAppInForeground && isDeviceActive){
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            boolean result = mBluetoothAdapter.startLeScan(mLeScanCallback);
            if (!result) {
                //Log.e(TAG, "startLeScan failed.");
            }
        }else{
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private Runnable mBatchMetadataRunnable = new Runnable () {
        @Override
        public void run() {
            batchFetchMetaData();
            mIsQueuing = false;
        }
    };

    //This is where we call the proxy server to get the data about the discovered devices
    private void batchFetchMetaData() {
        if(mDeviceBatchList.size() > 0) {
            MetadataResolver.getBatchMetadata(mDeviceBatchList, null);
            mDeviceBatchList = new ArrayList<NearbyDevice>(); // Clear out the list
        }
    }

    //Checking which app is in the foreground and comparing it to the physical web app
    //Returns true if physical web app in foreground, return false otherwise
    public void isForeground(String myPackage){
        ActivityManager manager = (ActivityManager) service.getSystemService(Context.ACTIVITY_SERVICE);
        List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        if(componentInfo.getPackageName().equals(myPackage)) isAppInForeground = true;
        else isAppInForeground = false;
    }

    //Here we handle the found devices: adding to the list, removing from the list...
    //This function will have different behavior in app and background service
    @Override
    protected void handleDeviceFound(NearbyDevice candidateNearbyDevice) {
        NearbyDevice nearbyDevice = curentDevices.get(candidateNearbyDevice.getUrl());
        if (nearbyDevice == null) {
            //Device detected but not found in the curentDevices map: add the device to curentDevices
            if(candidateNearbyDevice.getUrl() != null) {
                //Todo: Think if we need a URL validation here before we add the device
                curentDevices.put(candidateNearbyDevice.getUrl(), candidateNearbyDevice);
                updateNotification();
            }
        } else {
            //Update expiry date
            nearbyDevice.updateLastSeen(candidateNearbyDevice.getLastRSSI());
        }
    }

    public void setIsAppInForeground(boolean _isAppInForeground){
        isAppInForeground = _isAppInForeground;
    }

    public boolean getIsAppInForeground(){
        return isAppInForeground;
    }

    @Override
    protected void removeExpiredDevices(){
        isDeviceActive = powerManager.isScreenOn();
        isForeground("com.smus.physicalweb");
        if (!isAppInForeground && isDeviceActive) {

            boolean notificationUpdateNeeded = false;
            Iterator entries = curentDevices.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry thisDevice = (Map.Entry) entries.next();
                Object key = thisDevice.getKey();
                NearbyDevice device = (NearbyDevice) thisDevice.getValue();
                //NearbyDevice device = (NearbyDevice) thisDevice;
                if (device.isLastSeenAfter(DeviceManagerForService.MAX_INACTIVE_TIME)) {
                    entries.remove();
                    notificationUpdateNeeded = true;
                }
            }
            if (notificationUpdateNeeded) {
                updateNotification();
            }
        }else{

        }
    }

    private void updateNotification(){
        if(curentDevices.size() >= 0) {
            notificationPoster.updateNotification(curentDevices);
        }else{
            //notificationPoster.cancelNotification();
        }
    }

}