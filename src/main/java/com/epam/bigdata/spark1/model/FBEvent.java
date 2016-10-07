package com.epam.bigdata.spark1.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FBEvent {
    private String id;
    private String date;
    private String name;
    private String description;
    private String keyword;
    private String city;
    private Integer attendingCount;
    private DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    public FBEvent(String id, Date date, String name, String description, String keyword, String city, Integer attendingCount) {
        this.id = id;
        this.date = df.format(date);
        this.name = name;
        this.description = description;
        this.keyword = keyword;
        this.city = city;
        this.attendingCount = attendingCount;
    }

    public FBEvent() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String id) {
        this.date = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Integer getAttendingCount() {
        return attendingCount;
    }

    public void setAttendingCount(Integer attendingCount) {
        this.attendingCount = attendingCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
