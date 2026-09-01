package com.donuru.url_shortener.agentic.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
public class WorkflowTask {
    private String taskId;
    private WorkflowStage stage;
    private StageStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    @Builder.Default
    private Set<String> dependencies = new HashSet<>();
    @Builder.Default
    private int retryCount = 0;
    @Builder.Default
    private int maxRetries = 3;
    private boolean humanApprovalRequired;
    private String output;
    private String failureReason;
}