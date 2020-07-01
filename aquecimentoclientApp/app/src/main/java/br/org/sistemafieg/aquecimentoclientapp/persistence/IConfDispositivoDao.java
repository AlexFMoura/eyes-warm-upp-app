package br.org.sistemafieg.aquecimentoclientapp.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import br.org.sistemafieg.aquecimentoclientapp.model.ConfDispositivo;
import br.org.sistemafieg.aquecimentoclientapp.persistence.core.IDao;

@Dao
public interface IConfDispositivoDao extends IDao<ConfDispositivo> {


    @Query("SELECT * FROM ConfDispositivo d")
    ConfDispositivo buscarConf();
}
