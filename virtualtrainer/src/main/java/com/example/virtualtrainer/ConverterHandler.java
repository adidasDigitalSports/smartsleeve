

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

import android.util.Log;

public class ConverterHandler {
    private final String TAG = "ConverterHandler";
    private com.example.virtualtrainer.FloatStreamConverter floatStreamConverter = new com.example.virtualtrainer.FloatStreamConverter();
    private AngleStreamConverter angleStreamConverter = new AngleStreamConverter();

    private BluetoothHelperListener bluetoothHelperListener;

    public ConverterHandler(BluetoothHelperListener bluetoothHelperListener){
        this.bluetoothHelperListener = bluetoothHelperListener;
        floatStreamConverter.setBluetoothHelperListener(bluetoothHelperListener);
        angleStreamConverter.setBluetoothHelperListener(bluetoothHelperListener);
    }

    public void convertReceivedData(byte currentByte, BluetoothHelperMode bluetoothHelperMode){
        switch (bluetoothHelperMode)
        {
            case BALANCE:
                floatStreamConverter.convertReceivedData(currentByte);
                break;
            case ANGLE:
                angleStreamConverter.convertReceivedData(currentByte);
                break;
            case BATTERY_LEVEL:
                floatStreamConverter.convertReceivedData(currentByte);
                break;
            case BRIDGE:
                Log.e(TAG, "BluetoothHelperMode BRIDGE not implemeneted yet.");
                break;
            case NONE:
                Log.e(TAG, "Can not convert bytes in BluetoothHelperMode NONE.");
                break;
            default:
                Log.e(TAG, "BluetoothHelperMode not defined.");
                break;
        }
    }

    public void resetSoft(BluetoothHelperMode bluetoothHelperMode)
    {
        switch (bluetoothHelperMode)
        {
            case BALANCE:
                floatStreamConverter.resetBuffer();
                break;
            case ANGLE:
                angleStreamConverter.resetBuffer();
                break;
            case BATTERY_LEVEL:
                floatStreamConverter.resetBuffer();
                break;
            case BRIDGE:
                Log.e(TAG, "BluetoothHelperMode BRIDGE not implemeneted yet.");
                break;
            case NONE:
                Log.e(TAG, "Can not convert bytes in BluetoothHelperMode NONE.");
                break;
            default:
                Log.e(TAG, "BluetoothHelperMode not defined.");
                break;
        }
    }

    public void resetHard(BluetoothHelperMode bluetoothHelperMode){
        switch (bluetoothHelperMode)
        {
            case BALANCE:
                floatStreamConverter = new FloatStreamConverter();
                floatStreamConverter.setBluetoothHelperListener(bluetoothHelperListener);
                break;
            case ANGLE:
                angleStreamConverter = new AngleStreamConverter();
                angleStreamConverter.setBluetoothHelperListener(bluetoothHelperListener);
                break;
            case BATTERY_LEVEL:
                floatStreamConverter = new FloatStreamConverter();
                floatStreamConverter.setBluetoothHelperListener(bluetoothHelperListener);
                break;
            case BRIDGE:
                Log.e(TAG, "BluetoothHelperMode BRIDGE not implemeneted yet.");
                break;
            case NONE:
                Log.e(TAG, "Can not convert bytes in BluetoothHelperMode NONE.");
                break;
            default:
                Log.e(TAG, "BluetoothHelperMode not defined.");
                break;
        }
    }
}
