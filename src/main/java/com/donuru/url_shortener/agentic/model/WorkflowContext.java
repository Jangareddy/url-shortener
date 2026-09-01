package com.donuru.url_shortener.agentic.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class WorkflowContext {

    private final String workflowId = UUID.randomUUID().toString();
    private final LocalDateTime startedAt = LocalDateTime.now();
    private LocalDateTime completedAt;

    private final Map<String, WorkflowTask> tasks =
            new LinkedHashMap<>();

    private final Map<String, Object> sharedContext =
            new LinkedHashMap<>();

    private final Map<String, String> decisions =
            new LinkedHashMap<>();

    private final List<String> auditTrail =
            new ArrayList<>();

    public void addTask(WorkflowTask task) {
        tasks.put(task.getTaskId(), task);
    }

    public void putContext(String key, Object value) {
        sharedContext.put(key, value);
    }

    public void recordDecision(String key, String decision) {
        decisions.put(key, decision);
        recordAudit("DECISION " + key + ": " + decision);
    }

    public void recordAudit(String message) {
        auditTrail.add(
                LocalDateTime.now() + " | " + message
        );
    }

    public void markCompleted() {
        completedAt = LocalDateTime.now();
        recordAudit("WORKFLOW COMPLETED");
    }
}