package com.example.navivoice.room;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;

import com.example.navivoice.entity.PosSite;

import java.util.List;

@Dao
public interface PosDao {
    //获取地标分为两类，一类可以打断非景点语音getAllPos，一类不会打断getAllSmallPos
    @Query("SELECT * FROM POS WHERE attraction_flag = 0 ORDER BY ID")
    List<Pos> getAllPos();
    @Query("SELECT * FROM POS WHERE attraction_flag = 1 ORDER BY ID")
    List<Pos> getAllSmallPos();
    @Query("SELECT longitude,latitude,radius,voice_name FROM POS ORDER BY ID")
    List<PosSite> getAllPosSite();
    @Update
    void updatePos(Pos pos);
}
