package com.example.navivoice.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Pos {
    @PrimaryKey
    private Integer id;
    private String regional_code;
    private String serial_number;
    private String name;
    private Double longitude;
    private Double latitude;
    private Integer radius;
    private String voice_name;
    private Integer attraction_flag;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRegional_code() {
        return regional_code;
    }

    public void setRegional_code(String regional_code) {
        this.regional_code = regional_code;
    }

    public String getSerial_number() {
        return serial_number;
    }

    public void setSerial_number(String serial_number) {
        this.serial_number = serial_number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Integer getRadius() {
        return radius;
    }

    public void setRadius(Integer radius) {
        this.radius = radius;
    }

    public String getVoice_name() {
        return voice_name;
    }

    public void setVoice_name(String voice_name) {
        this.voice_name = voice_name;
    }

    public Integer getAttraction_flag() {
        return attraction_flag;
    }

    public void setAttraction_flag(Integer attraction_flag) {
        this.attraction_flag = attraction_flag;
    }

    @Override
    public String toString() {
        return "Pos{" +
                "id=" + id +
                ", regional_code='" + regional_code + '\'' +
                ", serial_number='" + serial_number + '\'' +
                ", name='" + name + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", radius=" + radius +
                ", voice_name='" + voice_name + '\'' +
                ", attraction_flag=" + attraction_flag +
                '}';
    }
}
