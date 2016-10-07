package com.epam.bigdata.spark1.model;

public class LogsEntry {
    private String date;
    private String tagsId;
    private String cityId;

    public LogsEntry(String date, String cityId, String tagsId) {
        this.date = date;
        this.tagsId = tagsId;
        this.cityId = cityId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTagsId() {
        return tagsId;
    }

    public void setTagsId(String tagsId) {
        this.tagsId = tagsId;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }
}
