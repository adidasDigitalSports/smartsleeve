

/*
 * Copyright (C) 2018 Ottobock SE & Co. KGaA
 *
 *  All Rights Reserved.
 *
 *  This software is provided AS-IS with no warranty,
 * either express or implied.
 *
 */

package com.example.virtualtrainer;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
public class PermissionFragment extends Fragment {

    private static final int REQ_PERMISSIONS = 9274;
    private static final int REQUEST_ENABLE_BT = 2343;
    private static final String TAG = "PermissionsFragment";

    public interface PermissionListener {
        //Called when the fragment has checked that the user meets the conditions to continue (blue tooth enabled and permissions granted)
        void onMeetConditions();

        //The user doen't meet some of the conditions
        void onDoentMeetConditions();
    }

    private PermissionListener mListener;

    public static PermissionFragment newInstance(PermissionListener listener) {
        PermissionFragment fr = new PermissionFragment();
        fr.setPermissionListener(listener);
        return fr;
    }

    public void setPermissionListener(PermissionListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstaceState) {
        super.onCreate(savedInstaceState);

        request();

    }

    public void request(){
        requestPermissions();
    }

    private void requestBTEnabled() {
        Log.d(TAG, "Checking if bluetooth is enabled...");
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);

        BluetoothAdapter btAdapter = bluetoothManager.getAdapter();
        if (btAdapter != null && btAdapter.isEnabled()) {
            Log.d(TAG, "Enabled");
            mListener.onMeetConditions();

        } else {
            Log.d(TAG, "Not enabled, requesting...");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void requestPermissions() {
        Log.d(TAG, "Checking for the required permissions...");
        //check we got the required permissions
        if ( ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED) {
            Log.d(TAG, "Has required permissions");
            requestBTEnabled();

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(getContext(), "Hi, we need this permission to connect to the Smart Sleeve via bluetooth. Please accept it :)", Toast.LENGTH_LONG).show();
            }

            Log.d(TAG, "Asking for required permissions");
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQ_PERMISSIONS) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permissions denied by the user");
                    mListener.onDoentMeetConditions();
                    return;
                }
            }

            Log.d(TAG, "Permissions granted by the user");
            requestBTEnabled();
        }
    }

    @Override
    public void onActivityResult(int resultCode, int resultStatus, Intent data) {
        super.onActivityResult(resultCode, resultStatus, data);
        if (resultCode == REQUEST_ENABLE_BT) {
            if (resultStatus == Activity.RESULT_OK) {
                Log.d(TAG, "Bluetooth enabled by the user");
                mListener.onMeetConditions();
            } else {
                Log.d(TAG, "Bluetooth not enabled by the user");
                mListener.onDoentMeetConditions();
            }
        }
    }
}