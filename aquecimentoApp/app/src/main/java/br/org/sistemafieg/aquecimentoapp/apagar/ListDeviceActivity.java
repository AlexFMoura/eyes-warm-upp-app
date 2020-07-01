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

package br.org.sistemafieg.aquecimentoapp.apagar;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import java.util.Set;

import br.org.sistemafieg.aquecimentoapp.R;
import br.org.sistemafieg.aquecimentoapp.controller.DispositivoConfExercicioController;
import br.org.sistemafieg.aquecimentoapp.dto.BluetoothDeviceDispConfDto;
import br.org.sistemafieg.aquecimentoapp.enums.IdentificacaoDispositivoEnum;
import br.org.sistemafieg.aquecimentoapp.fragments.confdispositivos.EditConfDeviceActivity;
import br.org.sistemafieg.aquecimentoapp.fragments.confdispositivos.ListNewDeviceActivity;
import br.org.sistemafieg.aquecimentoapp.model.DispositivoConfExercicio;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class ListDeviceActivity extends AppCompatActivity {

    // Intent request codes
    private static final int REQUEST_EDIT_CONF_DEVICE = 1;
    private static final int REQUEST_CONNECT_DEVICE = 2;

    /**
     * Member fields
     */
    private BluetoothAdapter mBtAdapter;
    private ListAdaperDevices pairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conf_dispositivos);

        // Define permissão de acesso bluetooth
        requestPermissionBluetoth();

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        pairedDevicesArrayAdapter = new ListAdaperDevices(this, R.layout.item_list_device);
        pairedDevicesArrayAdapter.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ViewDeviceItem mainViewDeviceItem = (ViewDeviceItem) v.getTag();

            }
        });
        pairedDevicesArrayAdapter.setActivityOpenButtonClick(EditConfDeviceActivity.class);


        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        refreshPairedDevices();
    }

    private void refreshPairedDevices() {
        pairedDevicesArrayAdapter.clear();

        class ListDispositivosConfiguradosTask extends AsyncTask<Void, Void, List<DispositivoConfExercicio>> {

            @Override
            protected List<DispositivoConfExercicio> doInBackground(Void... voids) {
                List<DispositivoConfExercicio> ds = DispositivoConfExercicioController.getInstance(getApplication()).getAll();

                return ds;
            }

            @Override
            protected void onPostExecute(List<DispositivoConfExercicio> ds) {
                super.onPostExecute(ds);

                // Get a set of currently paired devices
                Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

                // If there are paired devices, add each one to the ArrayAdapter
                if (pairedDevices.size() > 0) {
                    findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);

                    for (final BluetoothDevice device : pairedDevices) {
                        BluetoothDeviceDispConfDto bluetoothDeviceDispConfDto = new BluetoothDeviceDispConfDto();
                        for(DispositivoConfExercicio disp : ds) {
                            if(disp.getAddressDevice().equals(device.getAddress())) {
                                bluetoothDeviceDispConfDto.setDispositivoConfExercicio(disp);
                                break;
                            }
                        }

                        bluetoothDeviceDispConfDto.setBluetoothDevice(device);
                        pairedDevicesArrayAdapter.add(bluetoothDeviceDispConfDto);
                    }
                } else {
                    findViewById(R.id.lb_nenhum_dispositivo_pareado_encontrado).setVisibility(View.VISIBLE);
                }
            }
        }

        ListDispositivosConfiguradosTask newListDispositivosConfiguradosTask = new ListDispositivosConfiguradosTask();
        newListDispositivosConfiguradosTask.execute();

    }

    private void requestPermissionBluetoth() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conf_dispositivos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.menu_list_device_scan_dispositivos) {
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, ListNewDeviceActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_EDIT_CONF_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                   refreshPairedDevices();
                }
                break;
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    refreshPairedDevices();
                }
                break;
        }

    }

    @Override
    public void finish() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK,returnIntent);
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

    }

    private class ListAdaperDevices extends ArrayAdapter<BluetoothDeviceDispConfDto> {

        private int layout;
        private View.OnClickListener eventClickListener;
        private Class<? extends AppCompatActivity> activityOpenButtonClick;
        private ListAdaperDevices(Context context, int resource) {
            super(context, resource);
            this.layout = resource;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final BluetoothDeviceDispConfDto bluetoothDeviceDispConfDto = getItem(position);
            final BluetoothDevice device = bluetoothDeviceDispConfDto.getBluetoothDevice();

            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                ViewDeviceItem viewDeviceItem = new ViewDeviceItem();
                viewDeviceItem.icon = (TextView) convertView.findViewById(R.id.textViewItemListConfDispositivosIcon);
                viewDeviceItem.nameDevice = (TextView) convertView.findViewById(R.id.textViewItemListConfDispositivosNameDevice);
                viewDeviceItem.button = (Button) convertView.findViewById(R.id.imageButtonItemListConfDispositivosAction);
                viewDeviceItem.bluetoothDeviceDispConfDto = bluetoothDeviceDispConfDto;
                convertView.setTag(viewDeviceItem);
            }

            final ViewDeviceItem mainViewDeviceItem = (ViewDeviceItem) convertView.getTag();
            mainViewDeviceItem.bluetoothDeviceDispConfDto = bluetoothDeviceDispConfDto;
            mainViewDeviceItem.nameDevice.setText(device.getName() + "\n" + device.getAddress());
            if(mainViewDeviceItem.bluetoothDeviceDispConfDto.getDispositivoConfExercicio() != null) {
                String nameDevice = mainViewDeviceItem.nameDevice.getText().toString();
                mainViewDeviceItem.nameDevice.setText(nameDevice + "\n"
                        + IdentificacaoDispositivoEnum.findByName(
                                mainViewDeviceItem.bluetoothDeviceDispConfDto.getDispositivoConfExercicio().getIdentificacaoDevice()).getLabel());
                mainViewDeviceItem.nameDevice.setTextColor(Color.BLACK);
            } else {
                mainViewDeviceItem.nameDevice.setTextColor(Color.parseColor("#2196F3"));
            }

            if(activityOpenButtonClick != null) {
                mainViewDeviceItem.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent it = new Intent(ListDeviceActivity.this, activityOpenButtonClick);
                        it.putExtra("dataSource", bluetoothDeviceDispConfDto.getDispositivoConfExercicio());
                        it.putExtra("addressDevice", bluetoothDeviceDispConfDto.getBluetoothDevice().getAddress());
                        startActivityForResult(it, REQUEST_EDIT_CONF_DEVICE);
                    }
                });
            } else {
                mainViewDeviceItem.button.setVisibility(View.GONE);
            }

            if(eventClickListener != null) {
                convertView.setOnClickListener(eventClickListener);
            }

            return convertView;
        }

        public void setOnItemClickListener(View.OnClickListener event) {
            this.eventClickListener = event;
        }

        public void setActivityOpenButtonClick(Class<? extends AppCompatActivity> activityOpenButtonClick) {
            this.activityOpenButtonClick = activityOpenButtonClick;
        }

    }

    private class ViewDeviceItem {

        TextView icon;
        TextView nameDevice;
        Button button;
        BluetoothDeviceDispConfDto bluetoothDeviceDispConfDto;

    }


}