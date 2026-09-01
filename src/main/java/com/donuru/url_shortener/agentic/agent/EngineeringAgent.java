package com.donuru.url_shortener.agentic.agent;

import com.donuru.url_shortener.agentic.model.WorkflowContext;
import com.donuru.url_shortener.agentic.model.WorkflowStage;
import com.donuru.url_shortener.agentic.model.WorkflowTask;

public interface EngineeringAgent {

    WorkflowStage supports();

    String execute(
            WorkflowContext context,
            WorkflowTask task
    );
}