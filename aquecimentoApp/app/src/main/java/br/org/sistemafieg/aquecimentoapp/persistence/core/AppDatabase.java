package br.org.sistemafieg.aquecimentoapp.persistence.core;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import br.org.sistemafieg.aquecimentoapp.model.DispositivoConfExercicio;
import br.org.sistemafieg.aquecimentoapp.model.PadraoTreinamento;
import br.org.sistemafieg.aquecimentoapp.model.PadraoTreinamentoDispItem;
import br.org.sistemafieg.aquecimentoapp.model.ParametrosConfExercicio;
import br.org.sistemafieg.aquecimentoapp.persistence.IDispositivoConfExercicioDao;
import br.org.sistemafieg.aquecimentoapp.persistence.IPadraoTreinamentoDao;
import br.org.sistemafieg.aquecimentoapp.persistence.IParametrosConfExercicioDao;

@Database(entities = {DispositivoConfExercicio.class,
                        ParametrosConfExercicio.class,
                        PadraoTreinamento.class,
                        PadraoTreinamentoDispItem.class
           }, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public final static String DATABASE_NAME = "AQUECIMENTO";

    public abstract IParametrosConfExercicioDao parametrosConfExercicioDao();
    public abstract IDispositivoConfExercicioDao dispositivoConfExercicioDao();
    public abstract IPadraoTreinamentoDao padraoTreinamentoDao();

}
