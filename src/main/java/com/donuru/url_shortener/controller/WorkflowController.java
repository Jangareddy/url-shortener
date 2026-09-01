package com.donuru.url_shortener.controller;

import com.donuru.url_shortener.agentic.model.StartWorkflowRequest;
import com.donuru.url_shortener.agentic.model.WorkflowContext;
import com.donuru.url_shortener.agentic.model.WorkflowMetrics;
import com.donuru.url_shortener.agentic.orchestrator.WorkflowOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowOrchestrator orchestrator;

    @PostMapping
    public ResponseEntity<WorkflowContext> startWorkflow(
            @RequestBody StartWorkflowRequest request) {

        return ResponseEntity.ok(
                orchestrator.startWorkflow(
                        request.scenarioType(),
                        request.requirement()
                )
        );
    }

    @GetMapping("/{workflowId}")
    public ResponseEntity<WorkflowContext> getWorkflow(
            @PathVariable String workflowId) {

        return ResponseEntity.ok(
                orchestrator.getWorkflow(workflowId)
        );
    }

    @PostMapping("/{workflowId}/tasks/{taskId}/approve")
    public ResponseEntity<WorkflowContext> approveTask(
            @PathVariable String workflowId,
            @PathVariable String taskId) {

        return ResponseEntity.ok(
                orchestrator.approveTask(
                        workflowId,
                        taskId
                )
        );
    }

    @PostMapping("/{workflowId}/tasks/{taskId}/rollback")
    public ResponseEntity<WorkflowContext> rollbackTask(
            @PathVariable String workflowId,
            @PathVariable String taskId) {

        return ResponseEntity.ok(
                orchestrator.rollbackTask(
                        workflowId,
                        taskId
                )
        );
    }

    @PostMapping("/{workflowId}/tasks/{taskId}/replan")
    public ResponseEntity<WorkflowContext> replanTask(
            @PathVariable String workflowId,
            @PathVariable String taskId) {

        return ResponseEntity.ok(
                orchestrator.replanFrom(
                        workflowId,
                        taskId
                )
        );
    }

    @GetMapping("/{workflowId}/metrics")
    public ResponseEntity<WorkflowMetrics> getMetrics(
            @PathVariable String workflowId) {

        return ResponseEntity.ok(
                orchestrator.getMetrics(workflowId)
        );
    }
}