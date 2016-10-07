package com.epam.bigdata.spark1.model;

public class Attendee {
    private String name;
    private String id;
    private Integer numOccurencies;

    public Attendee(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public Attendee(String name, Integer numOccurencies) {
        this.name = name;
        this.numOccurencies = numOccurencies;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNumOccurencies() {
        return numOccurencies;
    }

    public void setNumOccurencies(Integer numOccurencies) {
        this.numOccurencies = numOccurencies;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return name + '\'' + numOccurencies + '\n';
    }
}
