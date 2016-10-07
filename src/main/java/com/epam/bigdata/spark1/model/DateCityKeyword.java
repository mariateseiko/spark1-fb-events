package com.epam.bigdata.spark1.model;

public class DateCityKeyword {
    private String date;
    private String cityName;
    private String keyword;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public DateCityKeyword(String date, String cityName, String keyword) {
        this.date = date;
        this.cityName = cityName;
        this.keyword = keyword;
    }
}
