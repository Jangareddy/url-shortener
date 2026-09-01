package com.donuru.url_shortener.agentic.agent;

import com.donuru.url_shortener.agentic.model.WorkflowContext;
import com.donuru.url_shortener.agentic.model.WorkflowStage;
import com.donuru.url_shortener.agentic.model.WorkflowTask;
import org.springframework.stereotype.Component;

@Component
public class ImplementationAgent implements EngineeringAgent {

    @Override
    public WorkflowStage supports() {
        return WorkflowStage.IMPLEMENTATION;
    }

    @Override
    public String execute(WorkflowContext context, WorkflowTask task) {
        String result = """
                Implementation Plan:
                - Create URL shortening REST API
                - Implement short-code generation
                - Persist mappings in PostgreSQL
                - Cache redirects in Redis
                - Track clickCount and lastAccessedAt
                - Validate URLs and expiration
                - Handle collisions with bounded retries
                """;
        context.putContext("implementationPlan", result);
        return result;
    }
}