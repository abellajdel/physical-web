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
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Handler;
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
public class NearbyDeviceManager {

    protected BluetoothAdapter mBluetoothAdapter;
    protected Timer mExpireTimer;
    protected Timer mScanTimer;
    protected boolean mIsSearching = false;
    protected Context context;
    protected PowerManager powerManager;
    protected boolean isDeviceActive;
    protected ArrayList<NearbyDevice> mDeviceBatchList;
    protected boolean mIsQueuing = false;
    protected Handler mQueryHandler;
    protected OnNearbyDeviceChangeListener mListener;

    // How often we should batch requests for metadata.
    protected int QUERY_PERIOD = 500;
    // How often to search for new devices (ms).
    protected int SCAN_PERIOD = 5000;
    // How often to check for expired devices.
    protected int EXPIRE_PERIOD = 3000;
    // How much time has to pass with a nearby device not being discovered before
    // we declare it gone.
    protected static int MAX_INACTIVE_TIME = 10000;

    NearbyDeviceManager(){

    }

    //We use contextWrapper here because it's the commun class between Activity and Service
    //The Goal is to use the same constructor from Activity and Service
    NearbyDeviceManager(ContextWrapper contextWrapper){
        context = contextWrapper;
        powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) contextWrapper.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        //Initializing few important variables
        mDeviceBatchList = new ArrayList<NearbyDevice>();
        mQueryHandler = new Handler();
        mScanTimer = new Timer();
        mExpireTimer = new Timer();

    }


    public void startSearchingForDevices() {
        assert !mIsSearching;
        mIsSearching = true;

        //We might need to encapsulate this in the screen on control to do the scanning only
        //When the screen is on
        // Start a timer to do scans.
        mScanTimer.scheduleAtFixedRate(mSearchTask, 0, SCAN_PERIOD);
        // Start a timer to check for expired devices.
        //mExpireTimer.scheduleAtFixedRate(mExpireTask, 0, EXPIRE_PERIOD);
    }


    /**
     * Private methods follow:
     */
    protected TimerTask mSearchTask = new TimerTask() {
        @Override
        public void run() {
            scanDevices();
        }
    };

    //The behavior of this function will defer between the app and the background service
    protected void scanDevices(){}

    //The behavior of this function will defer between the app and the background service
    protected void removeExpiredDevices(){}

    // NearbyDevice scan callback.
    protected BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int RSSI, byte[] scanRecord) {
            assert mListener != null;
            NearbyDevice candidateNearbyDevice = new NearbyDevice(device, RSSI);

            //Handle device found need to have separate behavior depending if it's in the app or in
            //the notification center
            handleDeviceFound(candidateNearbyDevice);
            removeExpiredDevices();
        }
    };

    protected void handleDeviceFound(NearbyDevice candidateNearbyDevice) {

    }

    public interface OnNearbyDeviceChangeListener {
        public void onDeviceFound(NearbyDevice device);
        public void onDeviceLost(NearbyDevice device);
    }

}
