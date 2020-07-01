package br.org.sistemafieg.aquecimentoapp.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.org.sistemafieg.aquecimentoapp.model.PadraoTreinamento;
import br.org.sistemafieg.aquecimentoapp.model.PadraoTreinamentoDispItem;
import br.org.sistemafieg.aquecimentoapp.persistence.core.IDao;

@Dao
public abstract class IPadraoTreinamentoDao implements IDao<PadraoTreinamento> {

    public PadraoTreinamento find(Long id) {
        PadraoTreinamento result = getPadraoTreinamento(id);

        List<PadraoTreinamentoDispItem> padraoTreinamentoDispItems = getPadraoTreinamentoDispItemAll(id);
        if(padraoTreinamentoDispItems != null) {
            result.setPadraoTreinamentoDispItems(new HashSet<PadraoTreinamentoDispItem>(padraoTreinamentoDispItems));
        }

        return result;
    }

    @Insert
    public Long insertOrUpdateCascade(PadraoTreinamento entity) {
        Long id = null;
        if(entity.getId() == null) {
            id = insert(entity);
        } else {
            id = entity.getId();
            update(entity);
        }

        entity.setId(id);
        Set<PadraoTreinamentoDispItem> padraoTreinamentoDispItemsInsert = new HashSet<PadraoTreinamentoDispItem>();
        Set<PadraoTreinamentoDispItem> padraoTreinamentoDispItemsUpdate = new HashSet<PadraoTreinamentoDispItem>();

        for(PadraoTreinamentoDispItem item : entity.getPadraoTreinamentoDispItems()) {
            item.setIdPadraoTreinamento(id);
            item.setIdPadraoTreinamento(entity.getId());
            if(item.getId() == null) {
                padraoTreinamentoDispItemsInsert.add(item);
            } else {
                padraoTreinamentoDispItemsUpdate.add(item);
            }
        }

        insertPadraoTreinamentoDispItemAll(padraoTreinamentoDispItemsInsert);
        updatePadraoTreinamentoDispItemAll(padraoTreinamentoDispItemsUpdate);

        return entity.getId();
    }

    @Delete
    public void deleteCascade(PadraoTreinamento entity) {
        deletePadraoTreinamentoDispItems(entity.getId());
        delete(entity);
    }

    @Insert
    abstract void insertPadraoTreinamentoDispItemAll(Set<PadraoTreinamentoDispItem> entitys);

    @Update
    abstract void updatePadraoTreinamentoDispItemAll(Set<PadraoTreinamentoDispItem> entitys);

    @Query("DELETE FROM PadraoTreinamentoDispItem WHERE ID_PADRAO_TREINAMENTO =:idPadraoTreinamento")
    abstract void deletePadraoTreinamentoDispItems(Long idPadraoTreinamento);

    @Query("DELETE FROM PadraoTreinamentoDispItem WHERE ID_DISPOSITIVO_CONF_EXERCICIO =:idDispositivoConfExercicio")
    public abstract void deletePadraoTreinamentoDispItemPorDispositivoConfExercicio(Long idDispositivoConfExercicio);



    @Query("SELECT * FROM PadraoTreinamento")
    public abstract List<PadraoTreinamento> getAll();

    @Query("SELECT * FROM PadraoTreinamento p WHERE p.ID =:id")
    public abstract PadraoTreinamento getPadraoTreinamento(Long id);

    @Query("SELECT * FROM PadraoTreinamentoDispItem p WHERE p.ID_PADRAO_TREINAMENTO =:idPadraoTreinamento")
    public abstract List<PadraoTreinamentoDispItem> getPadraoTreinamentoDispItemAll(Long idPadraoTreinamento);

}
