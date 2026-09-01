package com.donuru.url_shortener.agentic.model;

public enum StageStatus {
    PENDING,
    RUNNING,
    WAITING_FOR_APPROVAL,
    COMPLETED,
    FAILED,
    RETRYING,
    ROLLED_BACK,
    SAFE_STOPPED
}