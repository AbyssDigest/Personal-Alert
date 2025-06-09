package com.AbyssDigest.personalalert.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY `order` ASC")
    List<Alert> getAll();

    @Query("SELECT * FROM alerts WHERE id = :id")
    Alert getById(int id);

    @Insert
    void insert(Alert alert);

    @Update
    void update(Alert alert);

    @Delete
    void delete(Alert alert);
}
