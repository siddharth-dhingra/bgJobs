package com.capstone.bgJobs.model;

public interface Event<T> {
    String getEventId();
    EventTypes getType();
    T getPayload();
}