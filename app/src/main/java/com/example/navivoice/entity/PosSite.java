package com.example.navivoice.entity;

public class PosSite {
    private Double longitude;
    private Double latitude;
    private Integer radius;
    private String voice_name;

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

    @Override
    public String toString() {
        return "PosSite{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                ", radius=" + radius +
                ", voice_name='" + voice_name + '\'' +
                '}';
    }
}
