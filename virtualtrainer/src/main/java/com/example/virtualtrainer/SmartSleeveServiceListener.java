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


public interface SmartSleeveServiceListener {

    void onAngleReceived(float data);

    void onCalibration1Finished();

    void onCalibration2Finished();

    void onConnected();

    void onConnectionFailed();

    void onDisconnected();

    void onNotPairedError();
}