package com.donuru.url_shortener.agentic.orchestrator;

import com.donuru.url_shortener.agentic.model.WorkflowContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WorkflowStore {

    private final Map<String, WorkflowContext> workflows =
            new ConcurrentHashMap<>();

    public void save(WorkflowContext context) {
        workflows.put(context.getWorkflowId(), context);
    }

    public Optional<WorkflowContext> find(String workflowId) {
        return Optional.ofNullable(workflows.get(workflowId));
    }
}