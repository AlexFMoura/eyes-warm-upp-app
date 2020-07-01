package br.org.sistemafieg.aquecimentoapp.dto;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

import br.org.sistemafieg.aquecimentoapp.model.DispositivoConfExercicio;

public class BluetoothDeviceDispConfDto implements Serializable {

    private DispositivoConfExercicio dispositivoConfExercicio;
    private BluetoothDevice bluetoothDevice;

    public BluetoothDeviceDispConfDto() {

    }

    public DispositivoConfExercicio getDispositivoConfExercicio() {
        return dispositivoConfExercicio;
    }

    public void setDispositivoConfExercicio(DispositivoConfExercicio dispositivoConfExercicio) {
        this.dispositivoConfExercicio = dispositivoConfExercicio;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }


}
