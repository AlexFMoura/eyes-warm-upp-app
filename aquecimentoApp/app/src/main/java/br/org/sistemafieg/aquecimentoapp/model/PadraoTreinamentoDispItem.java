package br.org.sistemafieg.aquecimentoapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

import br.org.sistemafieg.aquecimentoapp.persistence.core.IEntity;

@Entity(foreignKeys = @ForeignKey(entity = PadraoTreinamento.class,
        parentColumns = "ID",
        childColumns = "ID_PADRAO_TREINAMENTO",
        onDelete = ForeignKey.CASCADE))
public class PadraoTreinamentoDispItem implements IEntity, Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private Long id;

    @ColumnInfo(name = "ID_PADRAO_TREINAMENTO")
    private Long idPadraoTreinamento;

    @ColumnInfo(name = "ID_DISPOSITIVO_CONF_EXERCICIO")
    private Long idDispositivoConfExercicio;

    @ColumnInfo(name = "ATIVAR_BEEP")
    private Boolean ativarBeep;

    @ColumnInfo(name = "TEMPO_VISUALIZACAO_NUMERO_EXERCICIO")
    private Double tempoVisualizacaoNumeroExercicio;


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdPadraoTreinamento() {
        return idPadraoTreinamento;
    }

    public void setIdPadraoTreinamento(Long idPadraoTreinamento) {
        this.idPadraoTreinamento = idPadraoTreinamento;
    }

    public Long getIdDispositivoConfExercicio() {
        return idDispositivoConfExercicio;
    }

    public void setIdDispositivoConfExercicio(Long idDispositivoConfExercicio) {
        this.idDispositivoConfExercicio = idDispositivoConfExercicio;
    }

    public Boolean getAtivarBeep() {
        return ativarBeep;
    }

    public void setAtivarBeep(Boolean ativarBeep) {
        this.ativarBeep = ativarBeep;
    }

    public Double getTempoVisualizacaoNumeroExercicio() {
        return tempoVisualizacaoNumeroExercicio;
    }

    public void setTempoVisualizacaoNumeroExercicio(Double tempoVisualizacaoNumeroExercicio) {
        this.tempoVisualizacaoNumeroExercicio = tempoVisualizacaoNumeroExercicio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PadraoTreinamentoDispItem that = (PadraoTreinamentoDispItem) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(idDispositivoConfExercicio, that.idDispositivoConfExercicio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idDispositivoConfExercicio);
    }
}
