

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

public class FloatStreamConverter extends ByteStreamConverter {

    @Override
    public void convertReceivedData(byte currentByte) {

        if (!newValues) {
            if (checkForNewData(currentByte)) {
                return;
            }
        } else {

            waitForNewData(currentByte);
        }


        lastReceivedByte = currentByte;
    }
}
