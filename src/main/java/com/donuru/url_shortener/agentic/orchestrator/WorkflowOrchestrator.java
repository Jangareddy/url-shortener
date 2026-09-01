package com.donuru.url_shortener.agentic.orchestrator;

import com.donuru.url_shortener.agentic.agent.EngineeringAgent;
import com.donuru.url_shortener.agentic.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class WorkflowOrchestrator {

    private final WorkflowGraph workflowGraph;
    private final WorkflowStore workflowStore;
    private final List<EngineeringAgent> agents;

    public WorkflowContext startWorkflow() {

        WorkflowContext context =
                workflowGraph.createDefaultWorkflow();

        workflowStore.save(context);

        context.recordAudit("WORKFLOW STARTED");

        executeReadyTasks(context);

        return context;
    }

    public WorkflowContext getWorkflow(String workflowId) {

        return workflowStore.find(workflowId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Workflow not found: " + workflowId
                        )
                );
    }

    public void executeReadyTasks(WorkflowContext context) {

        boolean progressed;

        do {

            progressed = false;

            List<WorkflowTask> readyTasks =
                    context.getTasks()
                            .values()
                            .stream()
                            .filter(task ->
                                    isReady(context, task))
                            .toList();

            if (!readyTasks.isEmpty()) {

                List<CompletableFuture<Void>> futures =
                        readyTasks.stream()
                                .map(task ->
                                        CompletableFuture.runAsync(
                                                () -> executeTask(
                                                        context,
                                                        task
                                                )
                                        )
                                )
                                .toList();

                CompletableFuture.allOf(
                        futures.toArray(
                                new CompletableFuture[0]
                        )
                ).join();

                progressed = true;
            }

        } while (progressed
                && !hasBlockingState(context));

        if (isWorkflowComplete(context)
                && context.getCompletedAt() == null) {

            context.markCompleted();
        }
    }

    private boolean isReady(
            WorkflowContext context,
            WorkflowTask task) {

        if (task.getStatus()
                != StageStatus.PENDING) {

            return false;
        }

        return task.getDependencies()
                .stream()
                .allMatch(dependencyId -> {

                    WorkflowTask dependency =
                            context.getTasks()
                                    .get(dependencyId);

                    return dependency != null
                            && dependency.getStatus()
                            == StageStatus.COMPLETED;
                });
    }

    private void executeTask(
            WorkflowContext context,
            WorkflowTask task) {

        /*
         * High-impact stages stop at a
         * human approval gate.
         */
        if (task.isHumanApprovalRequired()) {

            task.setStatus(
                    StageStatus.WAITING_FOR_APPROVAL
            );

            context.recordDecision(
                    task.getTaskId(),
                    "Human approval required before execution"
            );

            context.recordAudit(
                    "TASK WAITING FOR APPROVAL: "
                            + task.getTaskId()
            );

            return;
        }

        try {

            task.setStatus(StageStatus.RUNNING);
            task.setStartedAt(LocalDateTime.now());

            context.recordAudit(
                    "TASK STARTED: "
                            + task.getTaskId()
            );

            validatePolicy(
                    context,
                    task
            );

            String output =
                    executeStageLogic(
                            context,
                            task
                    );

            task.setOutput(output);
            task.setCompletedAt(
                    LocalDateTime.now()
            );

            task.setFailureReason(null);

            task.setStatus(
                    StageStatus.COMPLETED
            );

            context.recordAudit(
                    "TASK COMPLETED: "
                            + task.getTaskId()
            );

        } catch (Exception exception) {

            handleFailure(
                    context,
                    task,
                    exception
            );
        }
    }

    private String executeStageLogic(
            WorkflowContext context,
            WorkflowTask task) {

        EngineeringAgent agent =
                agents.stream()
                        .filter(candidate ->
                                candidate.supports()
                                        == task.getStage())
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No agent configured for stage: "
                                                + task.getStage()
                                )
                        );

        context.recordAudit(
                "AGENT EXECUTING: "
                        + agent.getClass()
                        .getSimpleName()
                        + " | stage="
                        + task.getStage()
        );

        return agent.execute(
                context,
                task
        );
    }

    private void validatePolicy(
            WorkflowContext context,
            WorkflowTask task) {

        if (task.getStage()
                == WorkflowStage.RELEASE_READINESS
                && !task.isHumanApprovalRequired()) {

            throw new IllegalStateException(
                    "Release readiness requires human approval"
            );
        }

        context.recordAudit(
                "POLICY CHECK PASSED: "
                        + task.getTaskId()
        );
    }

    private void handleFailure(
            WorkflowContext context,
            WorkflowTask task,
            Exception exception) {

        task.setRetryCount(
                task.getRetryCount() + 1
        );

        task.setFailureReason(
                exception.getMessage()
        );

        context.recordAudit(
                "TASK FAILURE: "
                        + task.getTaskId()
                        + " | "
                        + exception.getMessage()
        );

        if (task.getRetryCount()
                <= task.getMaxRetries()) {

            task.setStatus(
                    StageStatus.RETRYING
            );

            context.recordAudit(
                    "TASK RETRY: "
                            + task.getTaskId()
                            + " attempt="
                            + task.getRetryCount()
            );

            /*
             * Reset to PENDING so the
             * orchestrator can retry it.
             */
            task.setStatus(
                    StageStatus.PENDING
            );

        } else {

            task.setStatus(
                    StageStatus.SAFE_STOPPED
            );

            context.recordDecision(
                    task.getTaskId(),
                    "Safe-stop after retry limit"
            );
        }
    }

    public WorkflowContext approveTask(
            String workflowId,
            String taskId) {

        WorkflowContext context =
                getWorkflow(workflowId);

        return approveTask(
                context,
                taskId
        );
    }

    public WorkflowContext approveTask(
            WorkflowContext context,
            String taskId) {

        WorkflowTask task =
                context.getTasks()
                        .get(taskId);

        if (task == null) {

            throw new IllegalArgumentException(
                    "Unknown task: "
                            + taskId
            );
        }

        if (task.getStatus()
                != StageStatus.WAITING_FOR_APPROVAL) {

            throw new IllegalStateException(
                    "Task is not waiting for approval"
            );
        }

        context.recordDecision(
                taskId,
                "Human approval granted"
        );

        try {

            task.setStatus(
                    StageStatus.RUNNING
            );

            task.setStartedAt(
                    LocalDateTime.now()
            );

            context.recordAudit(
                    "APPROVED TASK STARTED: "
                            + taskId
            );

            validatePolicy(
                    context,
                    task
            );

            String output =
                    executeStageLogic(
                            context,
                            task
                    );

            task.setOutput(output);

            task.setCompletedAt(
                    LocalDateTime.now()
            );

            task.setFailureReason(null);

            task.setStatus(
                    StageStatus.COMPLETED
            );

            context.recordAudit(
                    "APPROVED TASK COMPLETED: "
                            + taskId
            );

        } catch (Exception exception) {

            handleFailure(
                    context,
                    task,
                    exception
            );

            return context;
        }

        executeReadyTasks(context);

        return context;
    }

    public WorkflowContext rollbackTask(
            String workflowId,
            String taskId) {

        WorkflowContext context =
                getWorkflow(workflowId);

        WorkflowTask task =
                context.getTasks()
                        .get(taskId);

        if (task == null) {

            throw new IllegalArgumentException(
                    "Unknown task: "
                            + taskId
            );
        }

        task.setStatus(
                StageStatus.ROLLED_BACK
        );

        task.setOutput(null);

        context.recordDecision(
                taskId,
                "Task rolled back by operator"
        );

        context.recordAudit(
                "TASK ROLLED BACK: "
                        + taskId
        );

        return context;
    }

    public WorkflowContext replanFrom(
            String workflowId,
            String changedTaskId) {

        WorkflowContext context =
                getWorkflow(workflowId);
        context.markInProgress();
        WorkflowTask changedTask =
                context.getTasks()
                        .get(changedTaskId);

        if (changedTask == null) {

            throw new IllegalArgumentException(
                    "Unknown task: "
                            + changedTaskId
            );
        }

        context.getTasks()
                .values()
                .stream()
                .filter(task ->
                        !task.getTaskId()
                                .equals(changedTaskId))
                .filter(task ->
                        dependsOn(
                                context,
                                task,
                                changedTaskId
                        ))
                .forEach(task -> {

                    task.setStatus(
                            StageStatus.PENDING
                    );

                    task.setOutput(null);
                    task.setFailureReason(null);
                    task.setRetryCount(0);
                    task.setStartedAt(null);
                    task.setCompletedAt(null);

                    context.recordAudit(
                            "TASK INVALIDATED BY REPLAN: "
                                    + task.getTaskId()
                    );
                });

        context.recordDecision(
                changedTaskId,
                "Upstream output changed; downstream tasks replanned"
        );

        executeReadyTasks(context);

        return context;
    }

    private boolean dependsOn(
            WorkflowContext context,
            WorkflowTask task,
            String dependencyId) {

        if (task.getDependencies()
                .contains(dependencyId)) {

            return true;
        }

        return task.getDependencies()
                .stream()
                .map(id ->
                        context.getTasks()
                                .get(id))
                .filter(Objects::nonNull)
                .anyMatch(parent ->
                        dependsOn(
                                context,
                                parent,
                                dependencyId
                        ));
    }

    public WorkflowMetrics getMetrics(
            String workflowId) {

        WorkflowContext context =
                getWorkflow(workflowId);

        long completedTasks =
                context.getTasks()
                        .values()
                        .stream()
                        .filter(task ->
                                task.getStatus()
                                        == StageStatus.COMPLETED)
                        .count();

        long failedTasks =
                context.getTasks()
                        .values()
                        .stream()
                        .filter(task ->
                                task.getStatus()
                                        == StageStatus.FAILED
                                        ||
                                        task.getStatus()
                                                == StageStatus.SAFE_STOPPED)
                        .count();

        long retryCount =
                context.getTasks()
                        .values()
                        .stream()
                        .mapToLong(
                                WorkflowTask::getRetryCount
                        )
                        .sum();

        long rollbackCount =
                context.getTasks()
                        .values()
                        .stream()
                        .filter(task ->
                                task.getStatus()
                                        == StageStatus.ROLLED_BACK)
                        .count();

        LocalDateTime endTime =
                context.getCompletedAt() != null
                        ? context.getCompletedAt()
                        : LocalDateTime.now();

        long durationMillis =
                Duration.between(
                        context.getStartedAt(),
                        endTime
                ).toMillis();

        return new WorkflowMetrics(
                completedTasks,
                failedTasks,
                retryCount,
                rollbackCount,
                durationMillis
        );
    }

    private boolean hasBlockingState(
            WorkflowContext context) {

        return context.getTasks()
                .values()
                .stream()
                .anyMatch(task ->
                        task.getStatus()
                                == StageStatus.WAITING_FOR_APPROVAL
                                ||
                                task.getStatus()
                                        == StageStatus.SAFE_STOPPED
                );
    }

    private boolean isWorkflowComplete(
            WorkflowContext context) {

        return context.getTasks()
                .values()
                .stream()
                .allMatch(task ->
                        task.getStatus()
                                == StageStatus.COMPLETED
                );
    }

    public WorkflowContext startWorkflow(
            ScenarioType scenarioType,
            String requirement) {

        WorkflowContext context =
                workflowGraph.createDefaultWorkflow();

        context.putContext(
                "scenarioType",
                scenarioType
        );

        context.putContext(
                "requirement",
                requirement
        );

        workflowStore.save(context);

        context.recordAudit(
                "WORKFLOW STARTED | scenario="
                        + scenarioType
        );

        executeReadyTasks(context);

        return context;
    }
}