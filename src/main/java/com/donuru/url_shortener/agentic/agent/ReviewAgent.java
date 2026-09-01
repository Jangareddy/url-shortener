package com.donuru.url_shortener.agentic.agent;

import com.donuru.url_shortener.agentic.model.WorkflowContext;
import com.donuru.url_shortener.agentic.model.WorkflowStage;
import com.donuru.url_shortener.agentic.model.WorkflowTask;
import org.springframework.stereotype.Component;

@Component
public class ReviewAgent implements EngineeringAgent {

    @Override
    public WorkflowStage supports() {
        return WorkflowStage.REVIEW;
    }

    @Override
    public String execute(
            WorkflowContext context,
            WorkflowTask task) {

        String result = """
                Engineering Review:
                - Validate API contract
                - Review error handling
                - Review concurrency risks
                - Review cache consistency
                - Review database constraints
                - Verify no sensitive configuration is hardcoded
                - Confirm release requires human approval
                """;

        context.putContext(
                "reviewResult",
                result
        );

        return result;
    }
}