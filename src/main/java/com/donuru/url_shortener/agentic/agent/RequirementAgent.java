package com.donuru.url_shortener.agentic.agent;

import com.donuru.url_shortener.agentic.model.ScenarioType;
import com.donuru.url_shortener.agentic.model.WorkflowContext;
import com.donuru.url_shortener.agentic.model.WorkflowStage;
import com.donuru.url_shortener.agentic.model.WorkflowTask;
import org.springframework.stereotype.Component;

@Component
public class RequirementAgent implements EngineeringAgent {

    @Override
    public WorkflowStage supports() {
        return WorkflowStage.REQUIREMENT;
    }

    @Override
    public String execute(
            WorkflowContext context,
            WorkflowTask task) {

        ScenarioType scenario =
                (ScenarioType) context
                        .getSharedContext()
                        .get("scenarioType");

        String requirement =
                (String) context
                        .getSharedContext()
                        .get("requirement");

        if (scenario == null) {
            scenario = ScenarioType.GREENFIELD;
        }

        String result =
                switch (scenario) {

                    case GREENFIELD -> """
                            Greenfield Requirement Analysis:
                            Requirement: %s
                            
                            Decomposition:
                            - Define URL shortening API
                            - Design persistence model
                            - Implement short-code generation
                            - Implement redirects
                            - Add Redis caching
                            - Add analytics
                            - Add validation and expiration
                            - Add tests and documentation
                            """.formatted(requirement);

                    case BROWNFIELD -> """
                            Brownfield Requirement Analysis:
                            Requirement: %s
                            
                            Existing System Considerations:
                            - Preserve current URL API behavior
                            - Review existing entity and repository
                            - Identify minimal-impact changes
                            - Reuse PostgreSQL persistence
                            - Reuse Redis caching
                            - Add changes without breaking existing clients
                            - Validate backward compatibility
                            - Add regression tests
                            """.formatted(requirement);

                    case AMBIGUOUS -> """
                            Ambiguous Requirement Analysis:
                            Requirement: %s
                            
                            Ambiguities Identified:
                            - Expected short URL lifetime is unspecified
                            - Analytics retention is unspecified
                            - Authentication requirement is unspecified
                            - Expected traffic volume is unspecified
                            
                            Assumptions:
                            - Public URL creation is allowed for prototype
                            - Expiration is optional
                            - Existing analytics fields are sufficient
                            - Current single-service architecture is acceptable
                            
                            Human clarification is required before
                            high-impact assumptions are finalized.
                            """.formatted(requirement);
                };

        context.putContext(
                "requirementAnalysis",
                result
        );

        context.recordDecision(
                "requirement-analysis",
                "Scenario analyzed: " + scenario
        );

        return result;
    }
}