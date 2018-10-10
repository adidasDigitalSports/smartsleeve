
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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Set;

public class BluetoothHelper implements Serializable, BluetoothHelperListener{

    private static final String TAG = "BluetoothHelper";
    private final boolean SECURE_CONNECTION = true;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mConnectionState;
    public boolean mDisconnecting;
    public boolean mIsCalibrated;
    private BluetoothAdapter mBluetoothAdapter;
    private Activity activity;

    private SmartSleeveServiceListener mSmartSleeveServiceListener;
    private BluetoothHelperMode bluetoothHelperMode = BluetoothHelperMode.ANGLE;

    private ConverterHandler converterHandler;

    public BluetoothHelper(SmartSleeveServiceListener smartSleeveServiceListener) {
        mDisconnecting = false;
        mSmartSleeveServiceListener = smartSleeveServiceListener;
        converterHandler = new ConverterHandler(this);
    }

    public synchronized void startConnection(Activity activity)
    {
        this.activity = activity;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        setConnectionState(BluetoothStates.STATE_NONE);

        try
        {
            BluetoothDeviceHandler.loadDevice(activity);
            if(BluetoothDeviceHandler.name.isEmpty())
            {
                getDeviceByName();
                BluetoothDeviceHandler.saveDevice(activity);
            }

            connectDevice(SECURE_CONNECTION);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mConnectionState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDeviceHandler to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mConnectionState == BluetoothStates.STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        Log.d(TAG, "THREAD START" + Thread.currentThread().getName());
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDeviceHandler that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        mSmartSleeveServiceListener.onConnected();


    }

    /**
     * Stop all threads
     */
    public synchronized void disconnect() {
        Log.d(TAG, "disconnect");

        mDisconnecting = true;

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        mBluetoothAdapter = null;
        mConnectThread = null;
        mConnectedThread = null;

        setConnectionState(BluetoothStates.STATE_NONE);

        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {

        }

        mDisconnecting = false;

        //mSmartSleeveServiceListener.onDisconnected();

    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mConnectionState != BluetoothStates.STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        mSmartSleeveServiceListener.onConnectionFailed();

        // Start the service over to restart listening mode
        BluetoothHelper.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        mSmartSleeveServiceListener.onDisconnected();

        setConnectionState(BluetoothStates.STATE_NONE);

        // Start the service over to restart listening mode
        BluetoothHelper.this.start();
    }

    private float lastValue;
    private boolean firstValue = true;

    @Override
    public void onNewDataReceived(float value) {
        switch (bluetoothHelperMode)
        {
            case ANGLE:
                mSmartSleeveServiceListener.onAngleReceived(value);
                break;
            default:
                break;

        }
    }

    @Override
    public void onCalibration1Finished() {
        mSmartSleeveServiceListener.onCalibration1Finished();
    }

    @Override
    public void onCalibration2Finished() {
        mSmartSleeveServiceListener.onCalibration2Finished();
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDeviceHandler
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            BluetoothStates.UUID_DEVICE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            BluetoothStates.UUID_DEVICE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
            setConnectionState(BluetoothStates.STATE_CONNECTING);
        }

        public void run() {
            Log.i(TAG, "Begin mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    Log.d(TAG,"Thread Close" + getName());
                    if(mmSocket!=null) mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothHelper.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            Log.d(TAG, "ConnectThread cancelled.");
            try {
                mmSocket.close();
                mmSocket = null;
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

            setConnectionState(BluetoothStates.STATE_CONNECTED);
        }

        public void run() {
            // Keep listening to the InputStream while connected
            while (mConnectionState == BluetoothStates.STATE_CONNECTED) {
                try {

                    byte receivedByte;

                    if(!mDisconnecting)
                    {
                       int receivedValue = mmInStream.read();

                        if(receivedValue != -1) {
                            receivedByte = (byte) receivedValue;
                            converterHandler.convertReceivedData(receivedByte, bluetoothHelperMode);
                        }
                        else
                        {
                            Log.d(TAG, "End of stream.");
                        }
                    }

                } catch (IOException e) {
                    mSmartSleeveServiceListener.onDisconnected();
/*                    if(!mDisconnecting)
                    {
                        Log.e(TAG, "disconnected", e);
                    }*/
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            Log.d(TAG, "ConnectedThread cancelled.");
            if (mmInStream != null)
            {
                try {mmInStream.close();} catch (Exception e) { Log.e(TAG, "close() of inputstream failed", e); }
                mmInStream = null;
            }

            if (mmOutStream != null)
            {
                try {mmOutStream.close();} catch (Exception e) { Log.e(TAG, "close() of outputstream failed", e); }
                mmOutStream = null;
            }

            if (mmSocket != null)
            {
                Log.d(TAG,"THREAD CLOSE" + getName());
                try {mmSocket.close();} catch (Exception e) { Log.e(TAG, "close() of connect socket failed", e); }
            }
        }
    }


    private void connectDevice(boolean secure)
    {
        BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(BluetoothDeviceHandler.adress);
        connect(bluetoothDevice, secure);
    }


    private boolean sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mConnectionState != BluetoothStates.STATE_CONNECTED) {
            // Error.
            return false;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            write(send);
            return true;
        }

        // Error.
        return false;
    }


    public void setBluetoothHelperMode(BluetoothHelperMode bluetoothHelperMode)
    {
        this.bluetoothHelperMode = bluetoothHelperMode;
    }

    public boolean isSendingData()
    {
        if(bluetoothHelperMode != BluetoothHelperMode.NONE) return true;
        return false;
    }

    public boolean startCalibration1()
    {
        converterHandler.resetHard(bluetoothHelperMode);
        return sendMessage(BluetoothCommands.CALIB1);
    }

    public boolean startCalibration2()
    {
        converterHandler.resetSoft(bluetoothHelperMode);
        return sendMessage(BluetoothCommands.CALIB2);
    }

    public boolean startAngleCalculation()
    {
        return sendMessage(BluetoothCommands.START_CALC);
    }

    public boolean startVibration()
    {
        return sendMessage(BluetoothCommands.START_VIBR);
    }

    public boolean stopVibration()
    {
        return sendMessage(BluetoothCommands.STOP_VIBR);
    }

    public boolean stopAncleCalculation() {return sendMessage(BluetoothCommands.STOP_CALC);}

    public boolean killBtConnection() {
        return  sendMessage(BluetoothCommands.KILL_CONNECTION);
    }

    private void getDeviceByName() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!bluetoothAdapter.isEnabled())
            bluetoothAdapter.enable();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for(BluetoothDevice device : pairedDevices)
        {
            if(BluetoothDeviceHandler.searchForDevice(device.getName(), device.getAddress()))
            {
                setConnectionState(mConnectionState);
                return;
            }
            
        }

        mSmartSleeveServiceListener.onNotPairedError();
    }

    public void setConnectionState(int pConnectionState) {
        mConnectionState = pConnectionState;
        try
        {
            BluetoothDeviceHandler.saveDevice(activity);
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getMessage());
        }

    }

    public String getDeviceName() {return BluetoothDeviceHandler.name;}


}
