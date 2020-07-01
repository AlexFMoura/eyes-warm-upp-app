package br.org.sistemafieg.aquecimentoapp.dto;

import java.io.Serializable;

import br.org.sistemafieg.aquecimentoapp.model.DispositivoConfExercicio;
import br.org.sistemafieg.aquecimentoapp.model.PadraoTreinamentoDispItem;

public class PadraoTreinamentoDispItemConfDto implements Serializable {

    private DispositivoConfExercicio dispositivoConfExercicio;
    private PadraoTreinamentoDispItem padraoTreinamentoDispItem;

    public PadraoTreinamentoDispItemConfDto() {

    }

    public DispositivoConfExercicio getDispositivoConfExercicio() {
        return dispositivoConfExercicio;
    }

    public void setDispositivoConfExercicio(DispositivoConfExercicio dispositivoConfExercicio) {
        this.dispositivoConfExercicio = dispositivoConfExercicio;
    }

    public PadraoTreinamentoDispItem getPadraoTreinamentoDispItem() {
        return padraoTreinamentoDispItem;
    }

    public void setPadraoTreinamentoDispItem(PadraoTreinamentoDispItem padraoTreinamentoDispItem) {
        this.padraoTreinamentoDispItem = padraoTreinamentoDispItem;
    }
}
