
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

public class AngleStreamConverter extends ByteStreamConverter {
    @Override
    public void convertReceivedData(byte currentByte) {
        int i = 0;
        if(!newValues)
        {
            if(checkForNewData(currentByte)) {return;}
            else if(!isCalibrated) {
                if (currentByte == BluetoothStates.FINISHED_BYTES && lastReceivedByte == BluetoothStates.FINISHED_BYTES) {

                    // calibration 1 finished
                    if(!isCalibration1Finished)
                    {
                        bluetoothHelperListener.onCalibration1Finished();
                        isCalibration1Finished = true;
                    }
                    // calibration 2 finished
                    else
                    {
                        bluetoothHelperListener.onCalibration2Finished();
                        isCalibrated = true;

                    }
                }
            }
        }
        else
        {

            waitForNewData(currentByte);
        }

        lastReceivedByte = currentByte;
    }

}