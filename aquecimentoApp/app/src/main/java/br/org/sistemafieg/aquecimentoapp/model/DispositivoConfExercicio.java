package br.org.sistemafieg.aquecimentoapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

import br.org.sistemafieg.aquecimentoapp.persistence.core.IEntity;

@Entity
public class DispositivoConfExercicio implements IEntity, Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private Long id;

    @ColumnInfo(name = "NAME_DEVICE")
    private String nameDevice;

    @ColumnInfo(name = "ADDRESS_DEVICE")
    private String addressDevice;

    @ColumnInfo(name = "IDENTIFICACAO_DEVICE")
    private String identificacaoDevice;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNameDevice() {
        return nameDevice;
    }

    public void setNameDevice(String nameDevice) {
        this.nameDevice = nameDevice;
    }

    public String getAddressDevice() {
        return addressDevice;
    }

    public void setAddressDevice(String addressDevice) {
        this.addressDevice = addressDevice;
    }

    public String getIdentificacaoDevice() {
        return identificacaoDevice;
    }

    public void setIdentificacaoDevice(String identificacaoDevice) {
        this.identificacaoDevice = identificacaoDevice;
    }

}
