package com.capstone.bgJobs.model;

public enum RunbookAction {
    UPDATE_FINDING("update_finding"),
    CREATE_TICKET("create_ticket");
    // Additional actions can be added here.

    private final String key;

    RunbookAction(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}