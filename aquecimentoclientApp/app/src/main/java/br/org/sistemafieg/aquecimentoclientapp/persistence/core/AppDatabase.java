package br.org.sistemafieg.aquecimentoclientapp.persistence.core;




import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import br.org.sistemafieg.aquecimentoclientapp.model.ConfDispositivo;
import br.org.sistemafieg.aquecimentoclientapp.persistence.IConfDispositivoDao;

@Database(entities = {ConfDispositivo.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public final static String DATABASE_NAME = "AQUECIMENTO";

    public abstract IConfDispositivoDao confDispositivoDao();

}
