

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


public interface BluetoothHelperListener {
    void onNewDataReceived(float value);
    void onCalibration1Finished();
    void onCalibration2Finished();
}
