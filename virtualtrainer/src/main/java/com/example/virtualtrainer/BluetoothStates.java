
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

import java.util.UUID;

public class BluetoothStates {
    public static final UUID UUID_DEVICE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final short NUMBER_BYTES_PACKAGE = 34;
    public static final short START_BYTES1 = 60;
    public static final short START_BYTES2 = 67;
    public static final short FINISHED_BYTES = 61;
    public static final short STOPPED_BYTES = 62;
    public static final short BYTE_SIZE_FLOAT = 4;
    public static final short BUFFER_BYTES_SIZE = 200;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
}
