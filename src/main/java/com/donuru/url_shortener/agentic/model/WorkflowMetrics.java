package com.donuru.url_shortener.agentic.model;

public record WorkflowMetrics(
        long completedTasks,
        long failedTasks,
        long retryCount,
        long rollbackCount,
        long durationMillis
) {
}