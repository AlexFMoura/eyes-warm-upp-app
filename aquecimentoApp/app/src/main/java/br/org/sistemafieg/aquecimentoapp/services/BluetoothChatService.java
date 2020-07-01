package br.org.sistemafieg.aquecimentoapp.services;

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import br.org.sistemafieg.aquecimentoapp.controller.DispositivoConfExercicioController;
import br.org.sistemafieg.aquecimentoapp.controller.PadraoTreinamentoController;
import br.org.sistemafieg.aquecimentoapp.controller.ParametrosConfExercicioController;
import br.org.sistemafieg.aquecimentoapp.enums.IdentificacaoDispositivoEnum;
import br.org.sistemafieg.aquecimentoapp.model.DispositivoConfExercicio;
import br.org.sistemafieg.aquecimentoapp.model.PadraoTreinamento;
import br.org.sistemafieg.aquecimentoapp.model.PadraoTreinamentoDispItem;
import br.org.sistemafieg.aquecimentoapp.model.ParametrosConfExercicio;
import br.org.sistemafieg.aquecimentoapp.util.Constants;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 *
 *
 */

public class BluetoothChatService {

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    private List<BluetoothChatServiceConnection> bluetoothConnects = new ArrayList<BluetoothChatServiceConnection>();
    private final Context mContext;
    private final Handler mHandler;
    private ParametrosConfExercicio mParametrosConfExercicio;
    private List<DispositivoConfExercicio> mDispositivoConfExercicios;

    public BluetoothChatService(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;

        findConfigurations();
        findDispositivoConfExercicios();
    }

    public synchronized void connect(IdentificacaoDispositivoEnum dispositivoEnum, BluetoothDevice device) {
       /* BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            BluetoothSocket mmSocket = device.createRfcommSocketToServiceRecord(uuid);
            mAdapter.cancelDiscovery();
            mmSocket.connect();

            BluetoothChatServiceConnection mConnectedThread = new BluetoothChatServiceConnection(mContext, mHandler);
            mConnectedThread.connected(mmSocket, device);

        } catch (Exception e) {
            e.printStackTrace();
        }
    */
        BluetoothChatServiceConnection bluetoothConnect = null;
        for(BluetoothChatServiceConnection bc : bluetoothConnects) {
            if(bc.dispositivoEnum.equals(dispositivoEnum)) {
                bluetoothConnect = bc;
                break;
            }
        }

        if(bluetoothConnect == null) {
            bluetoothConnect = new BluetoothChatServiceConnection(mContext, mHandler);
            bluetoothConnect.dispositivoEnum = dispositivoEnum;
            bluetoothConnects.add(bluetoothConnect);
        }

        BluetoothChatServiceConnection.UUIDEnum uuid = BluetoothChatServiceConnection.UUIDEnum.findByDP(dispositivoEnum.name());
        bluetoothConnect.connect(device,uuid.getUuid());


    }

    private List<Integer> getSequenciaExecucaoTreinamento(PadraoTreinamento padraoTreinamento) {
        long tempoMilisegundosExecucao = padraoTreinamento.getTempoRepeticaoExercicioMillis().longValue();
        List<Integer> result = new ArrayList<Integer>();

        if(padraoTreinamento.getTempoTrocaEntreDispositivosMillis().equals(0.0)) {
            Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.TOAST, "Defina o tempo de troca entre os dispositivos no padrão de treinamento!");
            msg.setData(bundle);
            mHandler.sendMessage(msg);
            throw new RuntimeException("Defina o tempo de troca entre os dispositivos no padrão de treinamento!");
        }

        double executionAmount = (tempoMilisegundosExecucao / padraoTreinamento.getTempoTrocaEntreDispositivosMillis()) + 50;

        Random r = new Random();
        int numberMaxDevice = bluetoothConnects.size();
        if(numberMaxDevice > 0) {
            int next = r.nextInt(bluetoothConnects.size());
            result.add(next);
            int previos = next;

            while (result.size() < Double.valueOf(executionAmount).longValue()) {
                next = r.nextInt(bluetoothConnects.size());

                if(next == previos && numberMaxDevice > 1) {
                    continue;
                }

                result.add(next);
                previos = next;
            }

        }

        return result;
    }

    private List<String> getSequenciaCaracteres() {
        List<String> result = new ArrayList<String>();

        int i = 0;
        while(i < mParametrosConfExercicio.getSequenciaNumeroExercicio().length()) {
            result.add(mParametrosConfExercicio.getSequenciaNumeroExercicio().substring(i, i+1));
            i++;
        }

        return result;
    }

    public void startAquecimento(PadraoTreinamento padraoTreinamento) {
        try {
            long tempoMilisegundosExecucao = padraoTreinamento.getTempoRepeticaoExercicioMillis().longValue();
            LinkedList<Integer> listSequExec = new LinkedList<Integer>();
            listSequExec.addAll(getSequenciaExecucaoTreinamento(padraoTreinamento));

            Random rCaracter = new Random();
            List<String> listCaracteres = getSequenciaCaracteres();
            rCaracter.nextInt(listCaracteres.size());

            Long timeInicioAquecimento = System.currentTimeMillis();
            Long interval = Double.valueOf(padraoTreinamento.getTempoTrocaEntreDispositivosMillis()).longValue();

            Timer t = new Timer();
            TimerTask tt = new TimerTask() {
                @Override
                public void run() {
                    int index = listSequExec.poll();
                    int indexCaracter = rCaracter.nextInt(listCaracteres.size());

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss.SS");
                    //Log.d("start", bluetoothConnects.get(index).dispositivoEnum.getLabel()+" Executado as: "+ sdf.format(new Date(System.currentTimeMillis())));
                    bluetoothConnects.get(index).write(listCaracteres.get(indexCaracter).getBytes());

                    if (System.currentTimeMillis() > (timeInicioAquecimento + tempoMilisegundosExecucao)) {
                        cancel();
                        t.cancel();
                    }
                }
            };

            t.scheduleAtFixedRate(tt, interval, interval);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void startAquecimentoOffline(PadraoTreinamento padraoTreinamento) {
        try {
            long tempoMilisegundosExecucao = padraoTreinamento.getTempoRepeticaoExercicioMillis().longValue();
            LinkedList<Integer> listSequExec = new LinkedList<Integer>();
            listSequExec.addAll(getSequenciaExecucaoTreinamento(padraoTreinamento));

            Random rCaracter = new Random();
            List<String> listCaracteres = getSequenciaCaracteres();
            rCaracter.nextInt(listCaracteres.size());

            Long interval = Double.valueOf(padraoTreinamento.getTempoTrocaEntreDispositivosMillis()).longValue();
            int ordemExecucao = 0;
            Map<Integer, JSONObject> mapConnection = new HashMap<Integer, JSONObject>();
            JSONObject json;
            Iterator<Integer> it = listSequExec.iterator();
            while(it.hasNext()) {
                int index = it.next();
                int indexCaracter = rCaracter.nextInt(listCaracteres.size());

                json = mapConnection.get(index);
                try{
                    json.put("ordemExecucao", json.get("ordemExecucao")+","+ordemExecucao);
                    json.put("sequenciaCaracter", json.get("sequenciaCaracter")+","+listCaracteres.get(indexCaracter));
                    ordemExecucao++;
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    try{
                        json = new JSONObject();
                        json.put("tempoMilisegundosExecucao", tempoMilisegundosExecucao);
                        json.put("intervaloMilissegundos", interval);
                        json.put("ordemExecucao", ordemExecucao);
                        json.put("sequenciaCaracter", listCaracteres.get(indexCaracter));

                        mapConnection.put(index, json);
                        ordemExecucao++;
                    } catch (JSONException jsonEx) {
                        jsonEx.printStackTrace();
                    }
                }

            }

            final LinkedList<Integer> sequenciaDevice = new LinkedList<Integer>(mapConnection.keySet());
            final Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Integer index = sequenciaDevice.poll();
                    JSONObject jsonExTreinamento = mapConnection.get(index);
                    try{
                        jsonExTreinamento.put("ordemConexaoDevice", index);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    bluetoothConnects.get(index).write(jsonExTreinamento.toString().getBytes());

                    if(!sequenciaDevice.isEmpty()) {
                        handler.postDelayed(this, 500);
                    }
                }
            };
            handler.postDelayed(runnable, 500);


            /*
            for(Integer index : mapConnection.keySet()) {
                JSONObject jsonExTreinamento = mapConnection.get(index);
                bluetoothConnects.get(index).write(jsonExTreinamento.toString().getBytes());
            } */

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

    }

    public void writeConf(Long idPadraoTreinamento) {
        class GetConfigurationPadraoTreinamentoItemTask extends AsyncTask<Void, Void, List<PadraoTreinamentoDispItem>> {

            @Override
            protected List<PadraoTreinamentoDispItem> doInBackground(Void... voids) {
                List<PadraoTreinamentoDispItem> ds = PadraoTreinamentoController.getInstance(mContext).getPadraoTreinamentoDispItemAll(idPadraoTreinamento);
                return ds;
            }

            @Override
            protected void onPostExecute(List<PadraoTreinamentoDispItem> ds) {
                super.onPostExecute(ds);

                for(BluetoothChatServiceConnection bc : bluetoothConnects) {
                    JSONObject json = new JSONObject();
                    try{
                        PadraoTreinamentoDispItem ptdi = getPadraoTreinamentoDispItem(bc.dispositivoEnum, ds);
                        json.put("conf_initial", true);
                        json.put("tempoVisualizacaoNumeroExercicio", ptdi.getTempoVisualizacaoNumeroExercicio().toString());
                        json.put("ativarBeep",ptdi.getAtivarBeep().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(bc.getmConnectedThread() != null) {
                        bc.sendMsgCong(json.toString());
                    }
                }


            }

            private PadraoTreinamentoDispItem getPadraoTreinamentoDispItem(IdentificacaoDispositivoEnum dispositivoEnum, List<PadraoTreinamentoDispItem> ds) {
                for(DispositivoConfExercicio dispConfExer : mDispositivoConfExercicios) {
                    if(dispConfExer.getIdentificacaoDevice().equals(dispositivoEnum.name())) {
                        for (PadraoTreinamentoDispItem item : ds) {
                            if (item.getIdDispositivoConfExercicio().equals(dispConfExer.getId())) {
                                return item;
                            }
                        }
                    }
                }

                return null;
            }

        }

        GetConfigurationPadraoTreinamentoItemTask newGetConfigurationPadraoTreinamentoItemTask = new GetConfigurationPadraoTreinamentoItemTask();
        newGetConfigurationPadraoTreinamentoItemTask.execute();

    }

    public void stop() {
        for(BluetoothChatServiceConnection bc : bluetoothConnects) {
            bc.stop();
        }
    }

    public boolean isCompletedConnection() {
        for(BluetoothChatServiceConnection bc : bluetoothConnects) {
            if(!bc.isConnectionRequestCompleted()){
                return false;
            }
        }

        return true;
    }

    public boolean isConnected(IdentificacaoDispositivoEnum dispositivoEnum) {
        BluetoothChatServiceConnection bluetoothConnect = null;
        for(BluetoothChatServiceConnection bc : bluetoothConnects) {
            if(bc.dispositivoEnum.equals(dispositivoEnum)) {
                bluetoothConnect = bc;
                break;
            }
        }

        return bluetoothConnect.getmConnectedThread() != null;
    }

    public Integer qtdConnected() {
        int result = 0;
        for(BluetoothChatServiceConnection bc : bluetoothConnects) {
            if(bc.getmConnectedThread() != null) {
                result++;
            }
        }

        return result;
    }

    private void findConfigurations() {
        class GetConfigurationsTask extends AsyncTask<Void, Void, ParametrosConfExercicio> {

            @Override
            protected ParametrosConfExercicio doInBackground(Void... voids) {
                ParametrosConfExercicio ds = ParametrosConfExercicioController.getInstance(mContext).find();
                return ds;
            }

            @Override
            protected void onPostExecute(ParametrosConfExercicio ds) {
                super.onPostExecute(ds);
                mParametrosConfExercicio = ds;
            }
        }

        GetConfigurationsTask newGetConfigurationsTask = new GetConfigurationsTask();
        newGetConfigurationsTask.execute();
    }

    private void findDispositivoConfExercicios() {
        class GetDispositivoConfExercicioTask extends AsyncTask<Void, Void, List<DispositivoConfExercicio>> {

            @Override
            protected List<DispositivoConfExercicio> doInBackground(Void... voids) {
                List<DispositivoConfExercicio> ds = DispositivoConfExercicioController.getInstance(mContext).getAll();
                return ds;
            }

            @Override
            protected void onPostExecute(List<DispositivoConfExercicio> ds) {
                super.onPostExecute(ds);
                mDispositivoConfExercicios = ds;
            }
        }

        GetDispositivoConfExercicioTask newGetDispositivoConfExercicioTask = new GetDispositivoConfExercicioTask();
        newGetDispositivoConfExercicioTask.execute();
    }


}






class BluetoothChatServiceConnection {
    // Debugging
    private static final String TAG = "BluetoothChatService";

    public IdentificacaoDispositivoEnum dispositivoEnum = null;

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private int mNewState;
    private boolean connectionRequestCompleted = false;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothChatServiceConnection(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
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

    public boolean isConnectionRequestCompleted() {
        return connectionRequestCompleted;
    }

    public ConnectedThread getmConnectedThread() {
        return mConnectedThread;
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
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
        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();

        // Update UI title
        updateUserInterfaceTitle();
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected, Socket");
    /*
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
        } */

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        bundle.putString(Constants.DEVICE_ADDRESS, device.getAddress());
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

        mState = STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();
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
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    public void sendMsgCong(String msfConf) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.writeConf(msfConf.getBytes());

    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed(BluetoothDevice device) {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_CONNECTION_FALIED);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();

    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost(BluetoothDevice device) {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_CONNECTION_LOST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        bundle.putString(Constants.DEVICE_ADDRESS, device.getAddress());
        bundle.putString(Constants.TOAST, "Conexão perdida com o dispoditivo "+dispositivoEnum.getLabel());

        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = STATE_NONE;
        connectionRequestCompleted = false;

        // Update UI title
        updateUserInterfaceTitle();

    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private UUID mUUuid;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mUUuid = uuid;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(mUUuid);
            } catch (IOException e) {
                Log.e(TAG, "Socket : " +uuid.toString(), e);
            }
            mmSocket = tmp;
            mState = STATE_CONNECTING;
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
                    synchronized (BluetoothChatServiceConnection.this) {
                        connectionRequestCompleted = true;
                    }
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }

                connectionFailed(mmDevice);
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothChatServiceConnection.this) {
                mConnectThread = null;
                connectionRequestCompleted = true;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
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
    class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
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
            mState = STATE_CONNECTED;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost(mmSocket.getRemoteDevice());
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
                //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss.SS");
                //Log.d("write", mmSocket.getRemoteDevice().getName() +" Executado as: "+ sdf.format(new Date(System.currentTimeMillis())));
                mmOutStream.write(buffer);
                mmOutStream.flush();
                //Log.d("write", mmSocket.getRemoteDevice().getName() +" Executado as: "+ sdf.format(new Date(System.currentTimeMillis())));

                // Share the sent message back to the UI Activity
                //mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void writeConf(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(Constants.MESSAGE_CONF_INITIAL, -1, -1, buffer).sendToTarget();
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

        public OutputStream getOutStream() {
            return mmOutStream;
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