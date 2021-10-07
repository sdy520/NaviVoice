package com.example.navivoice.room;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;

import com.example.navivoice.entity.PosSite;

import java.util.List;

@Dao
public interface PosDao {
    @Query("SELECT * FROM POS WHERE attraction_flag = 0 ORDER BY ID")
    List<Pos> getAllPos();
    @Query("SELECT longitude,latitude,radius,voice_name FROM POS ORDER BY ID")
    List<PosSite> getAllPosSite();
    @Update
    void updatePos(Pos pos);
}
