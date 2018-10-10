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

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;


public class SmartSleeveService extends Service implements SmartSleeveServiceListener {

    public static final String BROADCAST_ANGLE = "newAngle";
    public static final String BROADCAST_NEW_BALANCE_DATA = "newBalanceData";
    public static final String BROADCAST_CALIBRATION_ONE_END = "calibrationOneFinished";
    public static final String BROADCAST_CALIBRATION_TWO_END = "calibrationTwoFinished";
    public static final String BROADCAST_BATTERY_LEVEL_DATA = "batteryLevelData";
    public static final String BROADCAST_SLEEVE_CONNECTED = "pairingSuccess";
    public static final String BROADCAST_SLEEVE_DISCONNECTED = "disconnected";
    public static final String BROADCAST_SLEEVE_CONNECTION_ERROR = "pairingError";
    public static final String BROADCAST_SLEEVE_NOT_PAIRED_ERROR = "notPairedError";

    public static final String PARAM_ANGLE = "angleValue";

    private final static String TAG = "SmartSleeveService";

    /**
     * Manage all ble communication
     */
    private BluetoothHelper mBluetoothHelper;

    public SmartSleeveService() {


        mBluetoothHelper = new BluetoothHelper(this);
        //TODO mark this service as important so it is not killed by the system (unless extreme situations)
        //startForeground(int, Notification)
    }


    public void connectWithSleeve(Activity context) {
        Log.d(TAG, "Connect to sleeve...");
        mBluetoothHelper.startConnection(context);
    }

    public void calibrateOne() {
        Log.d(TAG, "Starting calibration one...");
        mBluetoothHelper.startCalibration1();
    }

    public void calibrateTwo() {
        Log.d(TAG, "Starting calibration two");
        mBluetoothHelper.startCalibration2();
    }


    public String getDeviceName()
    {
        return mBluetoothHelper.getDeviceName();
    }


    public void startCalculation() {
        mBluetoothHelper.setBluetoothHelperMode(BluetoothHelperMode.ANGLE);
        mBluetoothHelper.startAngleCalculation();

    }

    public void stopSendingData() {
        mBluetoothHelper.setBluetoothHelperMode(BluetoothHelperMode.NONE);
        mBluetoothHelper.stopAncleCalculation();
    }

    public void startVibration(){
        mBluetoothHelper.startVibration();
    }

    public void stopVibration(){
        mBluetoothHelper.stopVibration();
    }


    public boolean isSendingData()
    {
        return mBluetoothHelper.isSendingData();
    }

    public void killBtConnection() {
        mBluetoothHelper.killBtConnection();
    }

    // TODO: delete and use broadcast
    public boolean isCalibrated() {
        return mBluetoothHelper.mIsCalibrated;
    }

    public void disconnect()
    {
        mBluetoothHelper.disconnect();
    }

    @Override
    public void onAngleReceived(float data) {
        //Send over broadcast
        Intent intent = new Intent();
        intent.setAction(BROADCAST_ANGLE);
        intent.putExtra(PARAM_ANGLE, data);
        sendBroadcast(intent);
    }



    @Override
    public void onCalibration1Finished() {
        Log.d(TAG, "Calibration 1 finished.");
        sendBroadcast(new Intent(BROADCAST_CALIBRATION_ONE_END));
    }

    @Override
    public void onCalibration2Finished() {
        Log.d(TAG, "Calibration 2 finished.");
        sendBroadcast(new Intent(BROADCAST_CALIBRATION_TWO_END));
    }

    @Override
    public void onConnected() {
        Handler handler = new Handler(Looper.getMainLooper());
        // the sleeve needs some time to set up after open a new bt connection
        handler.postDelayed(new Runnable() {
            public void run() {
                // Actions to do after 10 seconds
            }
        }, 2000);
        sendBroadcast(new Intent(BROADCAST_SLEEVE_CONNECTED));
    }

    @Override
    public void onConnectionFailed() {
        sendBroadcast(new Intent(BROADCAST_SLEEVE_CONNECTION_ERROR));
}

    @Override
    public void onDisconnected() {
        sendBroadcast(new Intent(BROADCAST_SLEEVE_DISCONNECTED));
    }

    @Override
    public void onNotPairedError() {
        sendBroadcast(new Intent(BROADCAST_SLEEVE_NOT_PAIRED_ERROR));
    }

    public class LocalBinder extends Binder {
        SmartSleeveService getService() {
            return SmartSleeveService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Bind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "unBind");
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * After using a given bluetooth device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        Log.d(TAG, "Ending service...");

        stopSelf();
    }



}

