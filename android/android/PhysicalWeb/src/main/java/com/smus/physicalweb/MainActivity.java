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
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends Activity implements DeviceManagerForApp.OnNearbyDeviceChangeListener {

  private String TAG = "MainActivity";

  private DeviceManagerForApp mDeviceManager;
  private boolean appInForground;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (savedInstanceState == null) {
      getFragmentManager().beginTransaction()
          .add(R.id.container, new PlaceholderFragment())
          .commit();
    }

    mDeviceManager = new DeviceManagerForApp(this);
    mDeviceManager.setOnNearbyDeviceChangeListener(this);
    mDeviceManager.setIsAppInForeground(true);
    mDeviceManager.startSearchingForDevices();

    //Here we start the service that will take care of notifications
    boolean isMyServiceRunning = isMyServiceRunning(ScanService.class);
    if (!isMyServiceRunning) {
        Intent intent = new Intent(getApplicationContext(), ScanService.class);
        startService(intent);
    }
  }


  public boolean getAppInForground(){
    return appInForground;
  }


   //Checking if the background ScanService is already running
   private boolean isMyServiceRunning(Class<?> serviceClass) {
       ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
       List <ActivityManager.RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);
       for (ActivityManager.RunningServiceInfo service : services) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
   }

  @Override
  protected void onDestroy() {
      super.onDestroy();
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    switch (item.getItemId()) {
      case R.id.action_scan:
        mDeviceManager.scanDebug();
        return true;
      case R.id.action_debug:
        NearbyDevice device = new NearbyDevice("http://z3.ca/1", -60);
        mDeviceManager.foundDeviceDebug(device);
        return true;
      case R.id.action_debug2:
        device = new NearbyDevice("http://z3.ca/2", -80);
        mDeviceManager.foundDeviceDebug(device);
        return true;
      case R.id.action_debug3:
        device = new NearbyDevice("http://jenson.org/iot/1", -80);
        mDeviceManager.foundDeviceDebug(device);
        return true;
      case R.id.action_debug4:
        mDeviceManager.foundDeviceDebug(new NearbyDevice("http://jenson.org/iot/1", -81));
        mDeviceManager.foundDeviceDebug(new NearbyDevice("http://jenson.org/iot/2", -82));
        mDeviceManager.foundDeviceDebug(new NearbyDevice("http://jenson.org/iot/3", -83));
        mDeviceManager.foundDeviceDebug(new NearbyDevice("http://jenson.org/iot/4", -84));
        mDeviceManager.foundDeviceDebug(new NearbyDevice("http://jenson.org/iot/5", -85));
        mDeviceManager.foundDeviceDebug(new NearbyDevice("http://jenson.org/iot/6", -86));
        mDeviceManager.foundDeviceDebug(new NearbyDevice("http://jenson.org/iot/7", -87));
        mDeviceManager.foundDeviceDebug(new NearbyDevice("http://jenson.org/iot/8", -88));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  //Consider removing this function
  @Override
  public void onDeviceFound(NearbyDevice device) {
    //Log.i(TAG, "Found a device: " + device.getName());
  }

  //Consider removing this function
  @Override
  public void onDeviceLost(NearbyDevice device) {
    Log.i(TAG, "Lost a device: " + device.getName());
  }

  /**
   * A placeholder fragment containing a simple view.
   */
  public static class PlaceholderFragment extends Fragment {

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.fragment_main, container, false);
      MainActivity parentActivity = (MainActivity) getActivity();
      ListView list = (ListView) rootView.findViewById(R.id.devices);
      list.setAdapter(parentActivity.mDeviceManager.getAdapter());
      list.setOnItemClickListener(new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
          NearbyDevice device = (NearbyDevice) parent.getAdapter().getItem(position);
          String url = device.getUrl();
          if (url != null) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
          } else {
            Toast.makeText(getActivity(), "No URL found.", Toast.LENGTH_SHORT).show();
          }
        }
      });
      return rootView;
    }
  }

    @Override
    protected void onResume(){
        super.onResume();
        appInForground = true;
    }

    @Override
    protected void onPause() {
        // WHEN THE SCREEN IS ABOUT TO TURN OFF
        /*
        if (ScreenReceiver.wasScreenOn) {
            // THIS IS THE CASE WHEN ONPAUSE() IS CALLED BY THE SYSTEM DUE TO A SCREEN STATE CHANGE
            System.out.println("SCREEN TURNED OFF");
        } else {
            // THIS IS WHEN ONPAUSE() IS CALLED WHEN THE SCREEN STATE HAS NOT CHANGED
        }
        */
        super.onPause();
        appInForground = false;
    }
}
