package com.donuru.url_shortener.agentic.orchestrator;

import com.donuru.url_shortener.agentic.model.WorkflowContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowGraphTest {

    private final WorkflowGraph workflowGraph =
            new WorkflowGraph();

    @Test
    void shouldCreateExpectedDependencyGraph() {

        WorkflowContext context =
                workflowGraph.createDefaultWorkflow();

        assertEquals(
                7,
                context.getTasks().size()
        );

        assertTrue(
                context.getTasks()
                        .get("requirements")
                        .getDependencies()
                        .isEmpty()
        );

        assertTrue(
                context.getTasks()
                        .get("architecture")
                        .getDependencies()
                        .contains("requirements")
        );

        assertTrue(
                context.getTasks()
                        .get("implementation")
                        .getDependencies()
                        .contains("architecture")
        );

        assertTrue(
                context.getTasks()
                        .get("testing")
                        .getDependencies()
                        .contains("implementation")
        );

        assertTrue(
                context.getTasks()
                        .get("review")
                        .getDependencies()
                        .contains("implementation")
        );

        assertEquals(
                2,
                context.getTasks()
                        .get("documentation")
                        .getDependencies()
                        .size()
        );

        assertTrue(
                context.getTasks()
                        .get("documentation")
                        .getDependencies()
                        .contains("testing")
        );

        assertTrue(
                context.getTasks()
                        .get("documentation")
                        .getDependencies()
                        .contains("review")
        );
    }
}