package com.capstone.bgJobs.model;

public enum RunbookFilter {
    SEVERITY("severity"),
    STATE("state");
    // Additional filters can be added here.

    private final String key;

    RunbookFilter(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}