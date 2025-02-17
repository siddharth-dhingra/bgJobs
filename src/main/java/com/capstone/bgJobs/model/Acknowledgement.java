package com.capstone.bgJobs.model;

public interface Acknowledgement<T> {
    String getAcknowledgementId();
    T getPayload();
}