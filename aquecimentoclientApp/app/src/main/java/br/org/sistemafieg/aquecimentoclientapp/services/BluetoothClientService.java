package br.org.sistemafieg.aquecimentoclientapp.services;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.org.sistemafieg.aquecimentoclientapp.enums.IdentificacaoDispositivoEnum;
import br.org.sistemafieg.aquecimentoclientapp.util.Constants;


/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothClientService {

    public UUID uuid;

    // Debugging
    private static final String TAG = "BluetoothChatService";

    // Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothChat";

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private int mNewState;


    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothClientService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = Constants.STATE_NONE;
        mNewState = mState;
        mHandler = handler;
    }

    /**
     * Update UI title according to the current state of the chat connection
     */
    private synchronized void updateUserInterfaceTitle() {
        mState = getState();
        Log.d(TAG, "updateUserInterfaceTitle() " + mNewState + " -> " + mState);
        mNewState = mState;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start(IdentificacaoDispositivoEnum idDispEnum) {
        UUIDEnum uuidEnum = UUIDEnum.findByDP(idDispEnum.name());
        start(uuidEnum.uuid);
    }

    private synchronized void start(UUID uuid) {
        Log.d(TAG, "start");
        this.uuid = uuid;

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

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread(uuid);
            mAcceptThread.start();
        }
        // Update UI title
        updateUserInterfaceTitle();
    }


    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "connected");

        // @TODO: comenta para manter multiplas coneções
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

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, uuid);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        // Update UI title
        updateUserInterfaceTitle();
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        mState = Constants.STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed(UUID uuid) {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect device "+ UUIDEnum.findByUUID(uuid.toString()).getDescricao());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = Constants.STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();

        // Start the service over to restart listening mode
        BluetoothClientService.this.start(uuid);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost(UUID uuid) {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost "+ UUIDEnum.findByUUID(uuid.toString()).getDescricao());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = Constants.STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();

        // Start the service over to restart listening mode
        BluetoothClientService.this.start(uuid);
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private BluetoothServerSocket mmServerSocket;
        private final UUIDEnum uuidEnum;// = UUIDEnum.DP2;

        public AcceptThread(UUID uuid) {
            uuidEnum = UUIDEnum.findByUUID(uuid.toString());
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, uuidEnum.getUuid());

            } catch (IOException e) {
                Log.e(TAG, "Socket listen() failed", e);
            }
            mmServerSocket = tmp;
            mState = Constants.STATE_LISTEN;
        }

        public void run() {
            Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");

            BluetoothSocket socket = null;

            try {
                // Listen to the server socket if we're not connected
                while (mState != Constants.STATE_CONNECTED) {
                    try {
                        Log.i(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Socket inicio: "+ uuidEnum.getDescricao());
                        // This is a blocking call and will only return on a
                        // successful connection or an exception
                        socket = mmServerSocket.accept();
                        Log.i(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Socket accept: "+ uuidEnum.getDescricao());
                    } catch (IOException e) {
                        Log.e(TAG, "Socket accept() failed", e);
                        break;
                    }

                    // If a connection was accepted
                    if (socket != null) {
                        Log.i(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Socket accept != null: "+ uuidEnum.getDescricao());
                        synchronized (BluetoothClientService.this) {
                            switch (mState) {
                                case Constants.STATE_LISTEN:
                                case Constants.STATE_CONNECTING:
                                    // Situation normal. Start the connected thread.
                                    connected(socket, socket.getRemoteDevice(), uuidEnum.getUuid());
                                    Log.i(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Socket connect: "+ uuidEnum.getDescricao());
                                    break;
                                case Constants.STATE_NONE:
                                case Constants.STATE_CONNECTED:
                                    // Either not ready or already connected. Terminate new socket.
                                    try {
                                        socket.close();
                                    } catch (IOException e) {
                                        Log.e(TAG, "Could not close unwanted socket", e);
                                    }
                                    break;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Log.i(TAG, "END mAcceptThread");

        }

        public void cancel() {
            Log.d(TAG, "Socket cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final UUID mmUuid;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            mmDevice = device;
            mmUuid = uuid;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(mmUuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket create() failed", e);
            }
            mmSocket = tmp;
            mState = Constants.STATE_CONNECTING;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread Socket");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                connectionFailed(mmUuid);
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothClientService.this) {
                mConnectThread = null;
            }

            Log.i(TAG, "!!!!!!!connected "+ UUIDEnum.findByUUID(mmUuid.toString()).getDescricao());
            // Start the connected thread
            connected(mmSocket, mmDevice, mmUuid);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final UUID mmUuid;

        public ConnectedThread(BluetoothSocket socket, UUID uuid) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            mmUuid = uuid;
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
            mState = Constants.STATE_CONNECTED;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (mState == Constants.STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    String readMessage = new String(buffer, 0, bytes);
                    if(readMessage.contains("conf_initial")) {
                        mHandler.obtainMessage(Constants.MESSAGE_CONF_INITIAL, bytes, -1, buffer).sendToTarget();
                    } else {
                        // Send the obtained bytes to the UI Activity
                        mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost(mmUuid);
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

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }



    enum UUIDEnum {

        DP1(UUID.fromString("b7746a40-c758-4868-aa19-7ac6b3475dfc"), "Dispositivo 1"),
        DP2(UUID.fromString("2d64189d-5a2c-4511-a074-77f199fd0834"), "Dispositivo 2"),
        DP3(UUID.fromString("e442e09a-51f3-4a7b-91cb-f638491d1412"), "Dispositivo 3"),
        DP4(UUID.fromString("a81d6504-4536-49ee-a475-7d96d09439e4"), "Dispositivo 4"),
        DP5(UUID.fromString("aa91eab1-d8ad-448e-abdb-95ebba4a9b55"), "Dispositivo 5"),
        DP6(UUID.fromString("4d34da73-d0a4-4f40-ac38-917e0a9dee97"), "Dispositivo 6"),
        DP7(UUID.fromString("5e14d4df-9c8a-4db7-81e4-c937564c86e0"), "Dispositivo 7");

        private final UUID uuid;
        private final String descricao;

        private UUIDEnum(UUID uuid, String descricao) {
            this.uuid = uuid;
            this.descricao = descricao;
        }

        public static UUIDEnum findByDP(String nameDp) {
            return Enum.valueOf(UUIDEnum.class, nameDp);
        }

        public static UUIDEnum findByUUID(String uuid) {
            for(UUIDEnum item : values()) {
                if(item.getUuid().toString().equals(uuid)) {
                    return item;
                }
            }

            return null;
        }


        public UUID getUuid() {
            return uuid;
        }

        public String getDescricao() {
            return descricao;
        }
    }
}
