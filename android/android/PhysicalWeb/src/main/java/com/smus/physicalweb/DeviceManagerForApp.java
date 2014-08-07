/*
 * Copyright 2014 Google Inc. All Rights Reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smus.physicalweb;

        import android.app.Activity;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.bluetooth.BluetoothManager;
        import android.content.Context;
        import android.content.Intent;
        import android.os.Handler;
        import android.provider.Settings;
        import android.util.Log;

        import java.util.ArrayList;
        import java.util.Timer;
        import java.util.TimerTask;
        import android.os.PowerManager;
        import android.content.Context;

/**
 * Keeps track of all devices nearby.
 *
 * Posts notifications when a new device is near, or if an old device is no
 * longer nearby.
 *
 * Created by smus on 1/24/14.
 */
public class DeviceManagerForApp extends NearbyDeviceManager {
    private int REQUEST_ENABLE_BT = 0;
    private boolean isAppInForeground;

    private NearbyDeviceAdapter mNearbyDeviceAdapter;//Keep here only

    /**
     * The public interface of this class follows:
     */
    DeviceManagerForApp(MainActivity activity) {
        super(activity);

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        //Intantiating the adapter that will manage UI changes
        mNearbyDeviceAdapter = new NearbyDeviceAdapter(activity);
    }

    /**
     * Set up a listener for new nearby devices coming and going.
     * @param listener
     */
    public void setOnNearbyDeviceChangeListener(OnNearbyDeviceChangeListener listener) {
        mListener = listener;
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

    public NearbyDeviceAdapter getAdapter() {
        return mNearbyDeviceAdapter;
    }

    public void scanDebug() {
        mSearchTask.run();
    }

    public void foundDeviceDebug(NearbyDevice debugDevice) {
        handleDeviceFound(debugDevice);
    }

    //This is called from mSearchTask define in parent class
    @Override
    protected void scanDevices(){
        isDeviceActive = powerManager.isScreenOn();
        if (isDeviceActive) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            boolean result = mBluetoothAdapter.startLeScan(mLeScanCallback);
            if (!result) {
                //Log.e(TAG, "startLeScan failed.");
            }
        }else{
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    @Override
    protected void removeExpiredDevices(){
        if (isAppInForeground) {
            ArrayList<NearbyDevice> removed = mNearbyDeviceAdapter.removeExpiredDevices();
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
            MetadataResolver.getBatchMetadata(mDeviceBatchList, mNearbyDeviceAdapter);
            mDeviceBatchList = new ArrayList<NearbyDevice>(); // Clear out the list
        }
    }


    //Here we handle the found devices: adding to the list, removing from the list...
    //This function will have different behavior in app and background service
    @Override
    protected void handleDeviceFound(NearbyDevice candidateNearbyDevice) {
        NearbyDevice nearbyDevice = mNearbyDeviceAdapter.getExistingDevice(candidateNearbyDevice);

        //We will remove this function
        // Check if this is a new device.
        if (nearbyDevice != null) {
            // For existing devices, update their RSSI.
            nearbyDevice.updateLastSeen(candidateNearbyDevice.getLastRSSI());
            mNearbyDeviceAdapter.updateListUI();
        } else {
                if (candidateNearbyDevice.getUrl() != null){
                    // For new devices, add the device to the adapter.
                    nearbyDevice = candidateNearbyDevice;
                    if (nearbyDevice.isBroadcastingUrl()) {
                        if (!mIsQueuing) {
                            mIsQueuing = true;
                            // We wait QUERY_PERIOD ms to see if any other devices are discovered so we can batch.
                            mQueryHandler.postAtTime(mBatchMetadataRunnable, QUERY_PERIOD);
                        }
                        // Add the device to the queue of devices to look for.
                        mDeviceBatchList.add(nearbyDevice);
                        mNearbyDeviceAdapter.addDevice(nearbyDevice);
                        mListener.onDeviceFound(nearbyDevice);
                    }
            }
        }
    }


    public void setIsAppInForeground(boolean _isAppInForeground){
        isAppInForeground = _isAppInForeground;
    }

    public boolean getIsAppInForeground(){
        return isAppInForeground;
    }

}

