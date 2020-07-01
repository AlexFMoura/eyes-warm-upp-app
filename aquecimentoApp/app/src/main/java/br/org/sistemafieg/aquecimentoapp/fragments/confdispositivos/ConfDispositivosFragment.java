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

package br.org.sistemafieg.aquecimentoapp.fragments.confdispositivos;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import br.org.sistemafieg.aquecimentoapp.MainActivity;
import br.org.sistemafieg.aquecimentoapp.R;
import br.org.sistemafieg.aquecimentoapp.controller.DispositivoConfExercicioController;
import br.org.sistemafieg.aquecimentoapp.dto.BluetoothDeviceDispConfDto;
import br.org.sistemafieg.aquecimentoapp.enums.IdentificacaoDispositivoEnum;
import br.org.sistemafieg.aquecimentoapp.model.DispositivoConfExercicio;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class ConfDispositivosFragment extends Fragment {

    // Intent request codes
    private static final int REQUEST_EDIT_CONF_DEVICE = 1;
    private static final int REQUEST_CONNECT_DEVICE = 2;

    /**
     * Member fields
     */
    private BluetoothAdapter mBtAdapter;
    private ListAdaperDevices pairedDevicesArrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conf_dispositivos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Conf. Exercício");

        // Define permissão de acesso bluetooth
        requestPermissionBluetoth();

        // Set result CANCELED in case the user backs out
        getActivity().setResult(Activity.RESULT_CANCELED);

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        pairedDevicesArrayAdapter = new ListAdaperDevices(getContext(), R.layout.item_list_device);
        pairedDevicesArrayAdapter.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ViewDeviceItem mainViewDeviceItem = (ViewDeviceItem) v.getTag();

            }
        });
        pairedDevicesArrayAdapter.setActivityOpenButtonClick(EditConfDeviceActivity.class);


        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) getView().findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!mBtAdapter.isEnabled()){
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
            alertBuilder.setCancelable(true);
            alertBuilder.setMessage(getString(R.string.msg_permissao_habilitar_bluetooth));
            alertBuilder.setNegativeButton("RECUSAR", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().onBackPressed();                }
            });
            alertBuilder.setPositiveButton("PERMITIR", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    mBtAdapter.enable();

                    refreshPairedDevicesPosEnableBluetoth();
                }
            });
            AlertDialog alert = alertBuilder.create();
            alert.show();
        } else {
            refreshPairedDevices();
        }
    }

    private void refreshPairedDevicesPosEnableBluetoth() {
        final Handler handler = new Handler();
        final Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                if(mBtAdapter.isEnabled()) {
                    handler.removeCallbacks(this);
                    refreshPairedDevices();
                    return;
                }
                handler.postDelayed(this, 500);
            }
        };

        handler.post(runnableCode);
    }

    private void refreshPairedDevices() {
        pairedDevicesArrayAdapter.clear();

        class ListDispositivosConfiguradosTask extends AsyncTask<Void, Void, List<DispositivoConfExercicio>> {

            @Override
            protected List<DispositivoConfExercicio> doInBackground(Void... voids) {
                List<DispositivoConfExercicio> ds = DispositivoConfExercicioController.getInstance(getContext()).getAll();

                return ds;
            }

            @Override
            protected void onPostExecute(List<DispositivoConfExercicio> ds) {
                super.onPostExecute(ds);

                // Get a set of currently paired devices
                Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

                // If there are paired devices, add each one to the ArrayAdapter
                if (pairedDevices.size() > 0) {
                    List<BluetoothDeviceDispConfDto> list1 = new ArrayList<BluetoothDeviceDispConfDto>();
                    List<BluetoothDeviceDispConfDto> list2 = new ArrayList<BluetoothDeviceDispConfDto>();

                    getView().findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);

                    for (final BluetoothDevice device : pairedDevices) {
                        BluetoothDeviceDispConfDto bluetoothDeviceDispConfDto = new BluetoothDeviceDispConfDto();
                        for(DispositivoConfExercicio disp : ds) {
                            if(disp.getAddressDevice().equals(device.getAddress())) {
                                bluetoothDeviceDispConfDto.setDispositivoConfExercicio(disp);

                                list1.add(bluetoothDeviceDispConfDto);
                                break;
                            }
                        }

                        bluetoothDeviceDispConfDto.setBluetoothDevice(device);
                        if(!list1.contains(bluetoothDeviceDispConfDto)) {
                            list2.add(bluetoothDeviceDispConfDto);
                        }
                    }

                    Collections.sort(list1, (o1, o2) -> {
                        return o1.getBluetoothDevice().getName().compareTo(o2.getBluetoothDevice().getName());
                    });

                    Collections.sort(list2, (o1, o2) -> {
                        return o1.getBluetoothDevice().getName().compareTo(o2.getBluetoothDevice().getName());
                    });

                    pairedDevicesArrayAdapter.addAll(list1);
                    pairedDevicesArrayAdapter.addAll(list2);
                } else {
                    getView().findViewById(R.id.lb_nenhum_dispositivo_pareado_encontrado).setVisibility(View.VISIBLE);
                }
            }
        }

        ListDispositivosConfiguradosTask newListDispositivosConfiguradosTask = new ListDispositivosConfiguradosTask();
        newListDispositivosConfiguradosTask.execute();

    }

    private void requestPermissionBluetoth() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_conf_dispositivos, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_list_device_scan_dispositivos: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getContext(), ListNewDeviceActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
    public void onDestroy() {
        super.onDestroy();

        Intent returnIntent = new Intent();
        getActivity().setResult(Activity.RESULT_OK,returnIntent);

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
                        Intent it = new Intent(ConfDispositivosFragment.this.getActivity(), activityOpenButtonClick);
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