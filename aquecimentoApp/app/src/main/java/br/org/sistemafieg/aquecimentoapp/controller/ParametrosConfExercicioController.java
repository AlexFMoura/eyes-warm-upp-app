package br.org.sistemafieg.aquecimentoapp.controller;

import android.content.Context;

import br.org.sistemafieg.aquecimentoapp.controller.generic.ControllerManager;
import br.org.sistemafieg.aquecimentoapp.model.ParametrosConfExercicio;


public class ParametrosConfExercicioController extends ControllerManager<ParametrosConfExercicio> {

    private static ParametrosConfExercicioController mInstance;

    private ParametrosConfExercicioController(Context c) {
        super(c);
        this.dao = getAppDatabase().parametrosConfExercicioDao();
    }


    public ParametrosConfExercicio find() {
        return getAppDatabase().parametrosConfExercicioDao().find();
    }





    public static synchronized ParametrosConfExercicioController getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new ParametrosConfExercicioController(mCtx);
        }
        return mInstance;
    }
}
