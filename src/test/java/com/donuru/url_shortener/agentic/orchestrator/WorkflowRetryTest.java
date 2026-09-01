package com.donuru.url_shortener.agentic.orchestrator;

import com.donuru.url_shortener.agentic.agent.EngineeringAgent;
import com.donuru.url_shortener.agentic.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowRetryTest {

    @Test
    void shouldSafeStopAfterRetryLimitExceeded() {
        EngineeringAgent requirementAgent = successfulAgent(WorkflowStage.REQUIREMENT);
        EngineeringAgent architectureAgent = successfulAgent(WorkflowStage.ARCHITECTURE);
        EngineeringAgent failingImplementationAgent = new EngineeringAgent() {
            @Override
            public WorkflowStage supports() {
                return WorkflowStage.IMPLEMENTATION;
            }

            @Override
            public String execute(WorkflowContext context, WorkflowTask task) {
                throw new IllegalStateException("Simulated implementation failure");
            }
        };
        WorkflowOrchestrator orchestrator = new WorkflowOrchestrator(new WorkflowGraph(), new WorkflowStore(), List.of(requirementAgent, architectureAgent, failingImplementationAgent));
        WorkflowContext context = orchestrator.startWorkflow(ScenarioType.GREENFIELD, "Build a URL shortener");
        orchestrator.approveTask(context.getWorkflowId(), "requirements");
        WorkflowContext result = orchestrator.approveTask(context.getWorkflowId(), "architecture");
        WorkflowTask implementation = result.getTasks().get("implementation");
        assertEquals(StageStatus.SAFE_STOPPED, implementation.getStatus());
        assertEquals(implementation.getMaxRetries() + 1, implementation.getRetryCount());
        assertTrue(result.getAuditTrail().stream().anyMatch(entry -> entry.contains("FALLBACK ACTIVATED")));
    }

    private EngineeringAgent successfulAgent(WorkflowStage stage) {
        return new EngineeringAgent() {
            @Override
            public WorkflowStage supports() {
                return stage;
            }

            @Override
            public String execute(WorkflowContext context, WorkflowTask task) {
                return "Test output";
            }
        };
    }
}