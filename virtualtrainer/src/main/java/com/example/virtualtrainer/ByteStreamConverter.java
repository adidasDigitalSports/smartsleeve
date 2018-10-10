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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteStreamConverter {

   protected boolean newValues;
   protected boolean isCalibrated;
   protected boolean isCalibration1Finished;
   protected byte lastReceivedByte;
   protected short numberReceivedBytes;
   protected BluetoothHelperListener bluetoothHelperListener;
   protected byte[] floatBuffer = new byte[BluetoothStates.BYTE_SIZE_FLOAT];
   protected ByteBuffer byteBuffer;

   public  void convertReceivedData(byte currentByte) {}

   protected boolean checkForNewData(byte currentByte)
   {
       int i=0;
       if(currentByte == BluetoothStates.START_BYTES2 && lastReceivedByte == BluetoothStates.START_BYTES1)
       {
           // new values.
           newValues = true;
           floatBuffer = new byte[BluetoothStates.BYTE_SIZE_FLOAT];
           return true;
       }
       return false;
   }

   protected void waitForNewData(byte currentByte) {

       floatBuffer[numberReceivedBytes] = currentByte;
       numberReceivedBytes++;

       if (numberReceivedBytes == BluetoothStates.BYTE_SIZE_FLOAT) {
           float currentValue = convertByteBufferToFloat();
           bluetoothHelperListener.onNewDataReceived(currentValue);

           //reset
           newValues = false;
           numberReceivedBytes = 0;
           lastReceivedByte = 0;
           return;
       }
   }

   public void resetBuffer(){
       // conversation
       lastReceivedByte = 0;
       numberReceivedBytes = 0;
       newValues = false;
       floatBuffer = new byte[BluetoothStates.BYTE_SIZE_FLOAT];
   }

   public float convertByteBufferToFloat()
   {
       // all values received
       byteBuffer = ByteBuffer.wrap(floatBuffer);
       int bufferIndex = 0;
       return byteBuffer.order(ByteOrder.nativeOrder()).getFloat(0);
   }

   public void setBluetoothHelperListener(BluetoothHelperListener bluetoothHelperListener)
   {
       this.bluetoothHelperListener = bluetoothHelperListener;
   }

}
