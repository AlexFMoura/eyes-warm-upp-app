package br.org.sistemafieg.aquecimentoapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import br.org.sistemafieg.aquecimentoapp.persistence.core.IEntity;

@Entity
public class ParametrosConfExercicio implements IEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private Long id;

    @ColumnInfo(name = "QTD_DISPOSITIVO_CLIENTE")
    private Integer qtdDispositivoCliente;

    @ColumnInfo(name = "SEQUENCIA_NUMERO_EXERCICIO")
    private String sequenciaNumeroExercicio;


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQtdDispositivoCliente() {
        return qtdDispositivoCliente;
    }

    public void setQtdDispositivoCliente(Integer qtdDispositivoCliente) {
        this.qtdDispositivoCliente = qtdDispositivoCliente;
    }

    public String getSequenciaNumeroExercicio() {
        return sequenciaNumeroExercicio;
    }

    public void setSequenciaNumeroExercicio(String sequenciaNumeroExercicio) {
        this.sequenciaNumeroExercicio = sequenciaNumeroExercicio;
    }


}
