package br.org.sistemafieg.aquecimentoapp.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

import br.org.sistemafieg.aquecimentoapp.model.DispositivoConfExercicio;
import br.org.sistemafieg.aquecimentoapp.persistence.core.IDao;

@Dao
public interface IDispositivoConfExercicioDao extends IDao<DispositivoConfExercicio> {

    @Query("SELECT * FROM DispositivoConfExercicio")
    List<DispositivoConfExercicio> getAll();

    @Query("SELECT * FROM DispositivoConfExercicio dce                                                " +
            "INNER JOIN PadraoTreinamentoDispItem ptdi ON ptdi.ID_DISPOSITIVO_CONF_EXERCICIO = dce.ID " +
            "INNER JOIN PadraoTreinamento pt ON pt.ID = ptdi.ID_PADRAO_TREINAMENTO                    " +
            "WHERE pt.ID =:idPadraoTreinamento                                                        " +
            "ORDER BY dce.IDENTIFICACAO_DEVICE                                                        ")
    List<DispositivoConfExercicio> listByIdPadraoTreinamento(Long idPadraoTreinamento);

    @Query("SELECT * FROM DispositivoConfExercicio d WHERE d.IDENTIFICACAO_DEVICE =:identificacao")
    DispositivoConfExercicio buscarPorIdentificacao(String identificacao);
}
