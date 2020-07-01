package br.org.sistemafieg.aquecimentoclientapp.controller;

import android.content.Context;

import br.org.sistemafieg.aquecimentoclientapp.controller.generic.ControllerManager;
import br.org.sistemafieg.aquecimentoclientapp.model.ConfDispositivo;

public class ConfDispositivoController extends ControllerManager<ConfDispositivo> {

    private static ConfDispositivoController mInstance;

    public ConfDispositivoController(Context c) {
        super(c);
        this.dao = getAppDatabase().confDispositivoDao();
    }

    public ConfDispositivo buscarConf() {
        return getAppDatabase().confDispositivoDao().buscarConf();
    }

    @Override
    public ConfDispositivo insertOrUpdate(ConfDispositivo confDispositivo) {
        ConfDispositivo entity = getAppDatabase().confDispositivoDao().buscarConf();
        if(entity != null) {
            entity.setIdentificacaoDevice(confDispositivo.getIdentificacaoDevice());
            entity.setCorLayout(confDispositivo.getCorLayout());
            return super.insertOrUpdate(entity);
        }

        return super.insertOrUpdate(confDispositivo);
    }

    public static synchronized ConfDispositivoController getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new ConfDispositivoController(mCtx);
        }
        return mInstance;
    }
}
