package com.donuru.url_shortener.agentic.orchestrator;

import com.donuru.url_shortener.agentic.model.StageStatus;
import com.donuru.url_shortener.agentic.model.WorkflowContext;
import com.donuru.url_shortener.agentic.model.WorkflowStage;
import com.donuru.url_shortener.agentic.model.WorkflowTask;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class WorkflowGraph {

    public WorkflowContext createDefaultWorkflow() {
        WorkflowContext context = new WorkflowContext();
        context.addTask(task("requirements", WorkflowStage.REQUIREMENT, Set.of(), true));
        context.addTask(task("architecture", WorkflowStage.ARCHITECTURE, new HashSet<>(Set.of("requirements")), true));
        context.addTask(task("implementation", WorkflowStage.IMPLEMENTATION, new HashSet<>(Set.of("architecture")), false));
        context.addTask(task("testing", WorkflowStage.TESTING, new HashSet<>(Set.of("implementation")), false));
        context.addTask(task("review", WorkflowStage.REVIEW, new HashSet<>(Set.of("implementation")), false));
        context.addTask(task("documentation", WorkflowStage.DOCUMENTATION, new HashSet<>(Set.of("testing", "review")), false));
        context.addTask(task("release-readiness", WorkflowStage.RELEASE_READINESS, new HashSet<>(Set.of("documentation")), true));
        return context;
    }

    private WorkflowTask task(String id, WorkflowStage stage, Set<String> dependencies, boolean approvalRequired) {
        return WorkflowTask.builder().taskId(id).stage(stage).status(StageStatus.PENDING).dependencies(dependencies).maxRetries(3).humanApprovalRequired(approvalRequired).build();
    }

}