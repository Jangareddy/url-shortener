package com.donuru.url_shortener.agentic.agent;

import com.donuru.url_shortener.agentic.model.WorkflowContext;
import com.donuru.url_shortener.agentic.model.WorkflowStage;
import com.donuru.url_shortener.agentic.model.WorkflowTask;
import org.springframework.stereotype.Component;

@Component
public class TestingAgent implements EngineeringAgent {

    @Override
    public WorkflowStage supports() {
        return WorkflowStage.TESTING;
    }

    @Override
    public String execute(
            WorkflowContext context,
            WorkflowTask task) {

        String result = """
                Validation Plan:
                - Test URL creation
                - Test redirect behavior
                - Test invalid URLs
                - Test expired URLs
                - Test Redis cache lookup
                - Test analytics increment
                - Test short-code collision handling
                - Test workflow approval gates
                """;

        context.putContext(
                "testPlan",
                result
        );

        return result;
    }
}