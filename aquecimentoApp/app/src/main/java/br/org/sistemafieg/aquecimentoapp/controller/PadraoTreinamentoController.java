package br.org.sistemafieg.aquecimentoapp.controller;

import android.content.Context;

import java.util.List;

import br.org.sistemafieg.aquecimentoapp.controller.generic.ControllerManager;
import br.org.sistemafieg.aquecimentoapp.model.PadraoTreinamento;
import br.org.sistemafieg.aquecimentoapp.model.PadraoTreinamentoDispItem;

public class PadraoTreinamentoController extends ControllerManager<PadraoTreinamento> {

    private static PadraoTreinamentoController mInstance;

    private PadraoTreinamentoController(Context c) {
        super(c);
        this.dao = getAppDatabase().padraoTreinamentoDao();
    }

    public List<PadraoTreinamento> getAll() {
        return getAppDatabase().padraoTreinamentoDao().getAll();
    }

    public PadraoTreinamento find(Long id) {
        return getAppDatabase().padraoTreinamentoDao().find(id);
    }

    public List<PadraoTreinamentoDispItem> getPadraoTreinamentoDispItemAll(Long idPadraoTreinamento) {
        return getAppDatabase().padraoTreinamentoDao().getPadraoTreinamentoDispItemAll(idPadraoTreinamento);
    }


    @Override
    public PadraoTreinamento insertOrUpdate(PadraoTreinamento entity) {
        getAppDatabase().padraoTreinamentoDao().insertOrUpdateCascade(entity);

        return entity;
    }

    @Override
    public void delete(PadraoTreinamento entity) {
        getAppDatabase().padraoTreinamentoDao().deleteCascade(entity);
    }

    public void deletePadraoTreinamentoDispItemPorDispositivoConfExercicio(Long idDispositivoConfExercicio) {
        getAppDatabase().padraoTreinamentoDao().deletePadraoTreinamentoDispItemPorDispositivoConfExercicio(idDispositivoConfExercicio);
    }

    public static synchronized PadraoTreinamentoController getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new PadraoTreinamentoController(mCtx);
        }
        return mInstance;
    }
}
