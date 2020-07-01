package br.org.sistemafieg.aquecimentoapp.persistence;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

import br.org.sistemafieg.aquecimentoapp.model.ParametrosConfExercicio;
import br.org.sistemafieg.aquecimentoapp.persistence.core.IDao;

@Dao
public interface IParametrosConfExercicioDao extends IDao<ParametrosConfExercicio> {

    @Query("SELECT * FROM ParametrosConfExercicio")
    List<ParametrosConfExercicio> getAll();

    @Query("SELECT * FROM ParametrosConfExercicio")
    ParametrosConfExercicio find();


}
