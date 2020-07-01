package br.org.sistemafieg.aquecimentoapp.controller;

import android.content.Context;

import java.util.List;

import br.org.sistemafieg.aquecimentoapp.R;
import br.org.sistemafieg.aquecimentoapp.controller.generic.ControllerManager;
import br.org.sistemafieg.aquecimentoapp.model.DispositivoConfExercicio;

public class DispositivoConfExercicioController extends ControllerManager<DispositivoConfExercicio> {

    private static DispositivoConfExercicioController mInstance;

    public DispositivoConfExercicioController(Context c) {
        super(c);
        this.dao = getAppDatabase().dispositivoConfExercicioDao();
    }

    public List<DispositivoConfExercicio> getAll() {
        return getAppDatabase().dispositivoConfExercicioDao().getAll();
    }

    public List<DispositivoConfExercicio> listByIdPadraoTreinamento(Long idPadraoTreinamento) {
        return getAppDatabase().dispositivoConfExercicioDao().listByIdPadraoTreinamento(idPadraoTreinamento);
    }

    @Override
    public DispositivoConfExercicio insertOrUpdate(DispositivoConfExercicio entity) {
        DispositivoConfExercicio disp = getAppDatabase().dispositivoConfExercicioDao().buscarPorIdentificacao(entity.getIdentificacaoDevice());
        if(disp != null && !disp.getId().equals(entity.getId())) {
            throw new RuntimeException(mCtx.getResources().getString(R.string.msg_validacao_ja_possui_um_dispositivo_com_esta_identificacao));
        }

        return super.insertOrUpdate(entity);
    }

    public static synchronized DispositivoConfExercicioController getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new DispositivoConfExercicioController(mCtx);
        }
        return mInstance;
    }
}
