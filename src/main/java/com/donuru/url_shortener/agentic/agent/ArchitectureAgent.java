package com.donuru.url_shortener.agentic.agent;

import com.donuru.url_shortener.agentic.model.WorkflowContext;
import com.donuru.url_shortener.agentic.model.WorkflowStage;
import com.donuru.url_shortener.agentic.model.WorkflowTask;
import org.springframework.stereotype.Component;

@Component
public class ArchitectureAgent implements EngineeringAgent {

    @Override
    public WorkflowStage supports() {
        return WorkflowStage.ARCHITECTURE;
    }

    @Override
    public String execute(
            WorkflowContext context,
            WorkflowTask task) {

        Object requirementAnalysis =
                context.getSharedContext()
                        .get("requirementAnalysis");

        String result = """
                Architecture Decision:
                - Spring Boot REST service
                - PostgreSQL as source of truth
                - Redis for redirect caching
                - REST API for URL creation and analytics
                - Indexed shortCode column for fast lookup
                - Atomic analytics update
                - Explicit agentic workflow DAG
                - Human approval gates for high-impact stages
                """;

        context.putContext(
                "architecture",
                result
        );

        context.recordDecision(
                "architecture-design",
                "Spring Boot + PostgreSQL + Redis architecture selected"
        );

        return result;
    }
}