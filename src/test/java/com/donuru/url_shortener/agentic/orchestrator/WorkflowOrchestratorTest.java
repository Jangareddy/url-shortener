package com.donuru.url_shortener.agentic.orchestrator;

import com.donuru.url_shortener.agentic.agent.EngineeringAgent;
import com.donuru.url_shortener.agentic.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {WorkflowOrchestratorTest.FailingAgentTestConfig.class})
class WorkflowOrchestratorTest {

    @Autowired
    private WorkflowOrchestrator orchestrator;

    @Test
    void shouldStopAtRequirementApprovalGate() {
        WorkflowContext context = orchestrator.startWorkflow(ScenarioType.GREENFIELD, "Build a URL shortener");
        assertEquals(StageStatus.WAITING_FOR_APPROVAL, context.getTasks().get("requirements").getStatus());
        assertEquals(StageStatus.PENDING, context.getTasks().get("architecture").getStatus());
    }

    @Test
    void shouldCompleteWorkflowAfterApprovals() {
        WorkflowContext context = orchestrator.startWorkflow(ScenarioType.GREENFIELD, "Build a URL shortener");
        String workflowId = context.getWorkflowId();
        orchestrator.approveTask(workflowId, "requirements");
        assertEquals(StageStatus.WAITING_FOR_APPROVAL, context.getTasks().get("architecture").getStatus());
        orchestrator.approveTask(workflowId, "architecture");
        /*  * Implementation, testing, review and  * documentation should now progress.  */
        assertEquals(StageStatus.COMPLETED, context.getTasks().get("implementation").getStatus());
        assertEquals(StageStatus.COMPLETED, context.getTasks().get("testing").getStatus());
        assertEquals(StageStatus.COMPLETED, context.getTasks().get("review").getStatus());
        assertEquals(StageStatus.COMPLETED, context.getTasks().get("documentation").getStatus());
        assertEquals(StageStatus.WAITING_FOR_APPROVAL, context.getTasks().get("release-readiness").getStatus());
        orchestrator.approveTask(workflowId, "release-readiness");
        assertNotNull(context.getCompletedAt());
        assertTrue(context.getTasks().values().stream().allMatch(task -> task.getStatus() == StageStatus.COMPLETED));
    }

    @Test
    void shouldReplanDownstreamTasksWhenArchitectureChanges() {
        WorkflowContext context = orchestrator.startWorkflow(ScenarioType.GREENFIELD, "Build a URL shortener");
        String workflowId = context.getWorkflowId();
        orchestrator.approveTask(workflowId, "requirements");
        orchestrator.approveTask(workflowId, "architecture");
        /*  * Workflow should now have progressed  * to release readiness.  */
        assertEquals(StageStatus.WAITING_FOR_APPROVAL, context.getTasks().get("release-readiness").getStatus());
        orchestrator.replanFrom(workflowId, "architecture");
        assertTrue(context.getAuditTrail().stream().anyMatch(entry -> entry.contains("TASK INVALIDATED BY REPLAN")));
        assertTrue(context.getDecisions().get("architecture").contains("downstream tasks replanned"));
    }

    @Test
    void shouldRejectApprovalWhenTaskIsNotWaitingForApproval() {
        WorkflowContext context = orchestrator.startWorkflow(ScenarioType.GREENFIELD, "Build a URL shortener");
        String workflowId = context.getWorkflowId();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> orchestrator.approveTask(workflowId, "architecture"));
        assertTrue(exception.getMessage().contains("not waiting for approval"));
    }

    @Test
    void shouldRollbackCompletedTask() {
        WorkflowContext context = orchestrator.startWorkflow(ScenarioType.GREENFIELD, "Create a URL shortener");
        orchestrator.approveTask(context.getWorkflowId(), "requirements");
        orchestrator.approveTask(context.getWorkflowId(), "architecture");
        WorkflowContext rolledBack = orchestrator.rollbackTask(context.getWorkflowId(), "implementation");
        WorkflowTask task = rolledBack.getTasks().get("implementation");
        assertEquals(StageStatus.ROLLED_BACK, task.getStatus());
        assertNull(task.getOutput());
    }

    @TestConfiguration
    static class FailingAgentTestConfig {
        @Bean
        @Primary
        EngineeringAgent failingImplementationAgent() {
            return new EngineeringAgent() {
                @Override
                public WorkflowStage supports() {
                    return WorkflowStage.IMPLEMENTATION;
                }

                @Override
                public String execute(WorkflowContext context, WorkflowTask task) {
                    throw new IllegalStateException("Simulated implementation failure");
                }
            };
        }
    }
}