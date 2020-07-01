package br.org.sistemafieg.aquecimentoclientapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

import br.org.sistemafieg.aquecimentoclientapp.persistence.core.IEntity;

@Entity
public class ConfDispositivo implements IEntity, Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private Long id;

    @ColumnInfo(name = "IDENTIFICACAO_DEVICE")
    private String identificacaoDevice;

    @ColumnInfo(name = "COR_LAYOUT")
    private String corLayout;


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentificacaoDevice() {
        return identificacaoDevice;
    }

    public void setIdentificacaoDevice(String identificacaoDevice) {
        this.identificacaoDevice = identificacaoDevice;
    }

    public String getCorLayout() {
        return corLayout;
    }

    public void setCorLayout(String corLayout) {
        this.corLayout = corLayout;
    }
}
