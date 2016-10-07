package com.epam.bigdata.spark1.model;

import java.util.List;

public class DateCityKeywords {
    private String date;
    private String city;
    private List<String> keywords;

    public DateCityKeywords(String date, String city, List<String> keywords) {
        this.date = date;
        this.city = city;
        this.keywords = keywords;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public String toString() {
        return "DateCityKeywords{" +
                "date='" + date + '\'' +
                ", city='" + city + '\'' +
                ", keywords=" + keywords +
                '}';
    }
}
