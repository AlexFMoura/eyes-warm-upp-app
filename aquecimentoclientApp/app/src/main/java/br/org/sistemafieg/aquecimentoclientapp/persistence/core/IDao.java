package br.org.sistemafieg.aquecimentoclientapp.persistence.core;


import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Update;

public interface IDao<E extends IEntity> {

    @Insert
    Long insert(E entity);

    @Delete
    void delete(E entity);

    @Update
    void update(E entity);

}
