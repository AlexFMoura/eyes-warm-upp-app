package br.org.sistemafieg.aquecimentoclientapp.controller.generic;

import android.arch.persistence.room.Room;
import android.content.Context;

import br.org.sistemafieg.aquecimentoclientapp.persistence.core.AppDatabase;
import br.org.sistemafieg.aquecimentoclientapp.persistence.core.IDao;
import br.org.sistemafieg.aquecimentoclientapp.persistence.core.IEntity;

public class ControllerManager<E extends IEntity> {

    protected Context mCtx;
    private AppDatabase appDatabase;
    protected IDao dao;

    public ControllerManager(Context c) {
        this.mCtx = c;
        this.appDatabase = Room.databaseBuilder(this.mCtx, AppDatabase.class, AppDatabase.DATABASE_NAME).build();
    }

    public AppDatabase getAppDatabase() {
        return this.appDatabase;
    }

    public IDao getDao() {
        return this.dao;
    }

    public E insert(E entity) {
        Long id = getDao().insert(entity);
        entity.setId(id);

        return entity;
    }

    public void update(E entity) {
        getDao().update(entity);
    }

    public E insertOrUpdate(E entity) {
        if(entity.getId() == null) {
            insert(entity);
        } else {
            update(entity);
        }

        return entity;
    }

    public void delete(E entity) {
        getDao().delete(entity);
    }







}