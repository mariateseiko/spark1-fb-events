package com.epam.bigdata.spark1.model;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class FBEventsAgg {
    private String keyword;
    private String city;
    private String date;
    private Long totalAttending;
    private String[] topTen;

    public FBEventsAgg(String keyword, String city, String date, Long totalAttending, String[] topTen) {
        this.keyword = keyword;
        this.city = city;
        this.date = date;
        this.totalAttending = totalAttending;
        this.topTen = topTen;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getTotalAttending() {
        return totalAttending;
    }

    public void setTotalAttending(Long totalAttending) {
        this.totalAttending = totalAttending;
    }

    public String[] getTopTen() {
        return topTen;
    }

    public void setTopTen(String[] topTen) {
        this.topTen = topTen;
    }

    @Override
    public String toString() {
        return "keyword='" + keyword + '\'' +
                ", city='" + city + '\'' +
                ", date='" + date + '\'' +
                ", totalAttending=" + totalAttending +
                ", topTen=" + Arrays.toString(topTen);
    }
}
