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
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class SmartSleeve implements ServiceConnection {

    private static final String TAG = "SmartSleeve";
    private static final String TAG_PERMISSSION_FRAGMENT = "permission_fragment";

    private Activity mActivity;
    private SmartSleeveService mService;
    private SleeveListener mSleeveListener;

    /**
     * Listener to listen for the smartKnee creation
     */
    public interface SleeveSetupListener {
        void onSleeveCreated(SmartSleeve sleeve);
    }

    public interface SleeveListener {
        void onConnected();
        void onDisconnected();
        void onCalibrationOneFinished();
        void onCalibrationTwoFinished();
        void onConnectionError();
        void onNotPairedError();
        void onAngleValueChanged(float angle, float valgus);
    }


    private BroadcastReceiver mSetupReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //unregister
            mActivity.unregisterReceiver(this);

            String action = intent.getAction();
            if (action.equals(SmartSleeveService.BROADCAST_SLEEVE_CONNECTED)) {
                Log.d(TAG, "Paired");
                mSleeveListener.onConnected();

            } else if (action.equals(SmartSleeveService.BROADCAST_CALIBRATION_ONE_END)) {
                Log.d(TAG, "Calibration 1/2 success");
                mSleeveListener.onCalibrationOneFinished();

            } else if (action.equals(SmartSleeveService.BROADCAST_CALIBRATION_TWO_END)) {
                Log.d(TAG, "Calibration 2/2 success");
                mSleeveListener.onCalibrationTwoFinished();

            } else if (action.equals(SmartSleeveService.BROADCAST_SLEEVE_CONNECTION_ERROR)){
                Log.d(TAG, "Connection error");
                mSleeveListener.onConnectionError();
            }else if(action.equals(SmartSleeveService.BROADCAST_SLEEVE_DISCONNECTED))
            {
                mSleeveListener.onDisconnected();
            }
            else if(action.equals(SmartSleeveService.BROADCAST_SLEEVE_NOT_PAIRED_ERROR))
            {
                mSleeveListener.onNotPairedError();
            }
        }
    };

    private BroadcastReceiver mAngleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(SmartSleeveService.BROADCAST_ANGLE)) {
                float angle = intent.getFloatExtra(SmartSleeveService.PARAM_ANGLE, -1);
                mSleeveListener.onAngleValueChanged(angle, 0.f);
            }
        }
    };

    public boolean isCalibrated() {
        return mService != null && mService.isCalibrated();
    }

    public void setAngleListener(SleeveListener listener) {
        mSleeveListener = listener;
    }


    public SmartSleeve(Activity activity, SleeveListener listener) {
        mActivity = activity;
        mSleeveListener = listener;
    }


    /**
     * Connect to the sleeve by using bluetooth. Listener method will be called once this is done.
     */
    public void initialize() {
        //start service in case it has not been started
        mActivity.startService(new Intent(mActivity, SmartSleeveService.class));

        //binds to the service
        mActivity.bindService(new Intent(mActivity, SmartSleeveService.class), this, Context.BIND_ABOVE_CLIENT); //TODO this flag has been set too freely
    }

    /**
     * Disconnects the sleeve, closing the service. This methods release all device resources which means that the service responsible from communication
     * with the sleeve is also destroyed and calibration will be needed again. Call this method id the sleeve feedback is no longer needed.
     */
    public void disconnect() {
        Log.d(TAG, "Disconnecting...");
        try {
            mActivity.unregisterReceiver(mAngleReceiver);

        } catch (IllegalArgumentException e) {
            //receiver might not be set in some cases/
        }



        if (mService != null) {
            mService.killBtConnection();
            try {
                Thread.sleep(1000);
            }
            catch (Exception e)
            {

            }

            mService.disconnect();
            try {
                mActivity.unbindService(this);
            }
            catch (Exception e)
            {

            }

            mSleeveListener.onDisconnected();

        }
    }



    public boolean isSendingData()
    {
        if (mService != null) return mService.isSendingData();
        return false;
    }

    /**
     * Start the phase one of the calibration
     */
    public void startCalibrationOne() {
        Log.d(TAG, "Starting calibration one...");

        IntentFilter filter = new IntentFilter();
        filter.addAction(SmartSleeveService.BROADCAST_CALIBRATION_ONE_END);
        mActivity.registerReceiver(mSetupReceiver, filter);

        mService.calibrateOne();
    }

    /**
     * Start the phase two of the caibration
     */
    public void startCalibrationTwo() {
        Log.d(TAG, "Starting Calibration two...");

        IntentFilter filter = new IntentFilter();
        filter.addAction(SmartSleeveService.BROADCAST_CALIBRATION_TWO_END);
        mActivity.registerReceiver(mSetupReceiver, filter);

        mService.calibrateTwo();
    }


    public String getDeviceName() {return mService.getDeviceName();}

    /**
     * Indicated the Sleeve that we want to receive angle change measures
     */
    public void startReadingAngle() {
        Log.d(TAG, "Start reading angle...");

        IntentFilter filter = new IntentFilter();
        filter.addAction(SmartSleeveService.BROADCAST_ANGLE);
        mActivity.registerReceiver(mAngleReceiver, filter);
        //mActivity.bindService(new Intent(mActivity, SmartSleeveService.class), this, Context.BIND_ABOVE_CLIENT);
        mService.startCalculation();
    }

    public void startVibration() {
        Log.d(TAG, "Start vibration...");

        mService.startVibration();
    }

    public void stopVibration(){
        Log.d(TAG, "Stop vibration...");

        mService.stopVibration();
    }


    /**
     * Stop reading the angle. Service will be still running, so calibration is maintained. After calling pause it is possible to call starReadingAngle again to
     * start receiving feedback again. Also, a new instance of the SmartSleeve can be created and startReadingAngle can be called without a previous
     * calibration.
     */
    public void stopReadingAngle() {
        Log.d(TAG, "Stop reading angle.");

        if(mService!=null)mService.stopSendingData();

        try {
            //receiver might not be set in some cases/
            mActivity.unregisterReceiver(mAngleReceiver);
        } catch (IllegalArgumentException e)
        {

        }
    }



    public void connectWithSleeve()
    {
        //register broadcast receiver for Pairing
        IntentFilter filter = new IntentFilter();
        filter.addAction(SmartSleeveService.BROADCAST_SLEEVE_CONNECTED);
        filter.addAction(SmartSleeveService.BROADCAST_SLEEVE_CONNECTION_ERROR);
        mActivity.registerReceiver(mSetupReceiver, filter);

        mService.connectWithSleeve(mActivity);
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        mService = ((SmartSleeveService.LocalBinder) binder).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        if (mService != null) {
            mActivity.unbindService(this);
        }
    }


    public static class Builder {

        private SleeveSetupListener mSetupListener;
        private SleeveListener mSleeveListener;
        private AppCompatActivity mActivity;

        public Builder(AppCompatActivity activity, SleeveSetupListener setupListener, SleeveListener sleeveListener) {
            this.mSetupListener = setupListener;
            this.mActivity = activity;
            this.mSleeveListener = sleeveListener;
        }

        public void build() {
            //Listen for a permissions granted and retries if not granted
            PermissionFragment.PermissionListener permissionListener = new PermissionFragment.PermissionListener() {
                @Override
                public void onMeetConditions() {

                    //Remove the fragment
                    Fragment fragment = mActivity.getSupportFragmentManager().findFragmentByTag(TAG_PERMISSSION_FRAGMENT);
                    mActivity.getSupportFragmentManager().beginTransaction().remove(fragment).commit();

                    mSetupListener.onSleeveCreated(new SmartSleeve(mActivity, mSleeveListener));

                }

                @Override
                public void onDoentMeetConditions() {
                    //requestPermissions again (stubborn mode)
                    Toast.makeText(mActivity, "bla bla", Toast.LENGTH_LONG).show();
                    ((PermissionFragment) mActivity.getSupportFragmentManager().findFragmentByTag(TAG_PERMISSSION_FRAGMENT)).request();
                }
            };

            Fragment fr = PermissionFragment.newInstance(permissionListener);
            mActivity.getSupportFragmentManager().beginTransaction().add(fr,TAG_PERMISSSION_FRAGMENT).commit();
        }

    }


}