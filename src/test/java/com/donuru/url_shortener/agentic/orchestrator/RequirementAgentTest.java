package com.donuru.url_shortener.agentic.orchestrator;

import com.donuru.url_shortener.agentic.agent.RequirementAgent;
import com.donuru.url_shortener.agentic.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RequirementAgentTest {

    private final RequirementAgent agent = new RequirementAgent();

    @Test
    void shouldHandleGreenfieldScenario() {

        WorkflowContext context = new WorkflowContext();
        context.putContext("scenarioType", ScenarioType.GREENFIELD);
        context.putContext("requirement", "Build a URL shortener from scratch");
        String result = agent.execute(context, task());
        assertTrue(result.contains("Greenfield Requirement Analysis"));
        assertTrue(result.contains("Decomposition"));
    }

    @Test
    void shouldHandleBrownfieldScenario() {

        WorkflowContext context = new WorkflowContext();
        context.putContext("scenarioType", ScenarioType.BROWNFIELD);
        context.putContext("requirement", "Add analytics to existing service");
        String result = agent.execute(context, task());
        assertTrue(result.contains("Brownfield Requirement Analysis"));
        assertTrue(result.contains("backward compatibility"));
    }

    @Test
    void shouldIdentifyAmbiguousRequirements() {

        WorkflowContext context = new WorkflowContext();
        context.putContext("scenarioType", ScenarioType.AMBIGUOUS);
        context.putContext("requirement", "Make the system enterprise ready");
        String result = agent.execute(context, task());
        assertTrue(result.contains("Ambiguous Requirement Analysis"));
        assertTrue(result.contains("Ambiguities Identified"));
        assertTrue(result.contains("Assumptions"));
    }

    private WorkflowTask task() {
        return WorkflowTask.builder().taskId("requirements").stage(WorkflowStage.REQUIREMENT).status(StageStatus.PENDING).build();
    }
}