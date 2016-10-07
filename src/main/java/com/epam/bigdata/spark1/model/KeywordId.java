package com.epam.bigdata.spark1.model;

public class KeywordId {
    private String keywordsId;
    private String keywords;

    public KeywordId(String id, String keywords) {
        this.keywordsId = id;
        this.keywords = keywords;
    }

    public String getKeywordsId() {
        return keywordsId;
    }

    public void setKeywordsId(String keywordsId) {
        this.keywordsId = keywordsId;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
}
