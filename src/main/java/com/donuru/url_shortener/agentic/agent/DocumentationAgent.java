package com.donuru.url_shortener.agentic.agent;

import com.donuru.url_shortener.agentic.model.WorkflowContext;
import com.donuru.url_shortener.agentic.model.WorkflowStage;
import com.donuru.url_shortener.agentic.model.WorkflowTask;
import org.springframework.stereotype.Component;

@Component
public class DocumentationAgent implements EngineeringAgent {

    @Override
    public WorkflowStage supports() {
        return WorkflowStage.DOCUMENTATION;
    }

    @Override
    public String execute(
            WorkflowContext context,
            WorkflowTask task) {

        String result = """
                Documentation Artifacts:
                - Architecture overview
                - API usage
                - Local setup instructions
                - Testing approach
                - Agentic workflow explanation
                - Assumptions
                - Limitations and trade-offs
                """;

        context.putContext(
                "documentationPlan",
                result
        );

        return result;
    }
}