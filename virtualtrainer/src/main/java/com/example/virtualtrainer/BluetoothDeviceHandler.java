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
import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class BluetoothDeviceHandler {

    private static final String FILE_NAME = "btState.pref";
    private static String NAME_TO_SEARCH_FOR = "RNBT";
    public static String name ="";
    public static String adress="";

    public static void saveDevice(Activity activity) throws IOException
    {
        FileOutputStream fileOutputStream = activity.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
        ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
        outputStream.writeUTF(adress);
        outputStream.writeUTF(name);
        outputStream.close();
        fileOutputStream.close();
    }

    public static void loadDevice(Activity activity) throws IOException {
        FileInputStream fileInputStream = activity.openFileInput(FILE_NAME);
        ObjectInputStream inputStream = new ObjectInputStream(fileInputStream);
        adress = inputStream.readUTF();
        name = inputStream.readUTF();
        inputStream.close();
        fileInputStream.close();
    }

    public static boolean searchForDevice(String name, String adress)
    {
        if((name.trim()).contains(NAME_TO_SEARCH_FOR))
        {
            BluetoothDeviceHandler.name = name;
            BluetoothDeviceHandler.adress = adress;
            return true;
        }

        return false;
    }
}
