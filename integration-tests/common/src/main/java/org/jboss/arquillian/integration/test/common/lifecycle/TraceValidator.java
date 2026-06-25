/*
 * JBoss, Home of Professional Open Source
 * Copyright 2026 Red Hat Inc. and/or its affiliates and other contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.integration.test.common.lifecycle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Validates recorded lifecycle traces against expected step sequences.
 * Returns error messages rather than throwing directly, so the calling
 * framework can report failures using its own assertion mechanism.
 */
public final class TraceValidator {

    private TraceValidator() {
    }

    /**
     * Validates that the recorded trace matches the expected steps.
     *
     * @param testClassName simple name of the test class, used in error messages
     * @param expectedSteps the expected lifecycle step sequence
     * @param contents      raw trace file contents
     * @return empty if validation passes, or the full error message if it fails
     */
    public static Optional<String> validate(String testClassName, TraceStep[] expectedSteps, String contents) {
        List<String> errors = new ArrayList<>();

        validateOrderValues(expectedSteps, errors);
        List<ActualStep> actualSteps = parseTrace(contents);
        if (errors.isEmpty()) {
            validateAgainstExpected(expectedSteps, actualSteps, errors);
        }

        if (!errors.isEmpty()) {
            String newLine = System.lineSeparator();
            return Optional.of(testClassName + " trace validation failed." + newLine + newLine
                    + formatComparison(expectedSteps, actualSteps) + newLine
                    + String.join(newLine, errors));
        }
        return Optional.empty();
    }

    private static List<ActualStep> parseTrace(String contents) {
        List<ActualStep> steps = new ArrayList<>();
        if (contents == null || contents.isEmpty()) {
            return steps;
        }
        for (String entry : contents.split(",")) {
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            int colonIndex = trimmed.lastIndexOf(':');
            if (colonIndex < 0) {
                throw new IllegalStateException("Invalid trace entry (missing ':location'): " + trimmed);
            }
            String name = trimmed.substring(0, colonIndex);
            RunsWhere runsWhere = RunsWhere.valueOf(trimmed.substring(colonIndex + 1));
            steps.add(new ActualStep(name, runsWhere));
        }
        return steps;
    }

    private static String formatComparison(TraceStep[] expected, List<ActualStep> actual) {
        String newLine = System.lineSeparator();
        StringBuilder output = new StringBuilder();
        int rowCount = Math.max(expected.length, actual.size());

        Set<Integer> permutableOrders = new HashSet<>();
        Set<Integer> seenOrders = new HashSet<>();
        for (TraceStep step : expected) {
            if (!seenOrders.add(step.order())) {
                permutableOrders.add(step.order());
            }
        }

        int expectedColumnWidth = "Expected".length();
        int actualColumnWidth = "Actual".length();
        for (TraceStep step : expected) {
            int length = formatExpectedStep(step, permutableOrders).length();
            expectedColumnWidth = Math.max(expectedColumnWidth, length);
        }
        for (ActualStep step : actual) {
            actualColumnWidth = Math.max(actualColumnWidth,
                    (step.name + ":" + step.runsWhere).length());
        }

        String headerFormat = String.format("  %%3s | %%-%ds | %%s", expectedColumnWidth);
        String separatorFormat = String.format("  %%s-+-%%-%ds-+-%%s", expectedColumnWidth);
        String rowFormat = String.format("  %%3d | %%-%ds | %%s", expectedColumnWidth);

        output.append(String.format(headerFormat, "#", "Expected", "Actual")).append(newLine);
        output.append(String.format(separatorFormat,
                "---", "-".repeat(expectedColumnWidth), "-".repeat(actualColumnWidth))).append(newLine);

        for (int i = 0; i < rowCount; i++) {
            String expectedEntry = i < expected.length ? formatExpectedStep(expected[i], permutableOrders) : "";
            String actualEntry = i < actual.size()
                    ? actual.get(i).name + ":" + actual.get(i).runsWhere
                    : "<missing>";
            output.append(String.format(rowFormat, i + 1, expectedEntry, actualEntry)).append(newLine);
        }

        return output.toString();
    }

    private static String formatExpectedStep(TraceStep step, Set<Integer> permutableOrders) {
        String entry = step.name() + ":" + step.runsWhere();
        if (permutableOrders.contains(step.order())) {
            return entry + "  [order " + step.order() + ", permutable]";
        }
        return entry;
    }

    private static void validateOrderValues(TraceStep[] steps, List<String> errors) {
        if (steps.length == 0) {
            return;
        }
        if (steps[0].order() != 0) {
            errors.add("Order values must start at 0, but first step '" + steps[0].name()
                    + "' has order " + steps[0].order());
        }
        int previousOrder = 0;
        int maxOrder = 0;
        for (int i = 1; i < steps.length; i++) {
            int order = steps[i].order();
            if (order != previousOrder && order != maxOrder + 1) {
                errors.add("Invalid order at step '" + steps[i].name() + "' at index " + i
                        + ": order " + order + " (previous=" + previousOrder + ", max=" + maxOrder + ")");
            }
            maxOrder = Math.max(maxOrder, order);
            previousOrder = order;
        }
    }

    private static void validateAgainstExpected(TraceStep[] expectedSteps, List<ActualStep> actualSteps,
                    List<String> errors) {
        if (expectedSteps.length != actualSteps.size()) {
            errors.add("Expected " + expectedSteps.length + " trace steps but got " + actualSteps.size());
            return;
        }

        int position = 0;
        int stepIndex = 0;
        while (stepIndex < expectedSteps.length) {
            int blockStart = stepIndex;
            int order = expectedSteps[stepIndex].order();
            while (stepIndex < expectedSteps.length && expectedSteps[stepIndex].order() == order) {
                stepIndex++;
            }
            int blockSize = stepIndex - blockStart;

            if (blockSize == 1) {
                validateFixedStep(expectedSteps[blockStart], actualSteps.get(position), position, errors);
            } else {
                validatePermutableBlock(expectedSteps, blockStart, blockSize, actualSteps, position, order, errors);
            }
            position += blockSize;
        }
    }

    private static void validateFixedStep(TraceStep expected, ActualStep actual, int position, List<String> errors) {
        if (!expected.name().equals(actual.name)) {
            errors.add("Step " + (position + 1) + ": expected name '" + expected.name()
                    + "' but got '" + actual.name + "'");
        }
        if (expected.runsWhere() != actual.runsWhere) {
            errors.add("Step " + (position + 1) + " '" + expected.name() + "': expected "
                    + expected.runsWhere() + " but ran on " + actual.runsWhere);
        }
    }

    private static void validatePermutableBlock(TraceStep[] expectedSteps, int blockStart, int blockSize,
            List<ActualStep> actualSteps, int position, int order, List<String> errors) {
        List<ActualStep> unmatched = new ArrayList<>();
        for (int i = blockStart; i < blockStart + blockSize; i++) {
            unmatched.add(new ActualStep(expectedSteps[i].name(), expectedSteps[i].runsWhere()));
        }

        for (int i = position; i < position + blockSize; i++) {
            ActualStep actual = actualSteps.get(i);
            if (!removeFirstMatch(unmatched, actual)) {
                errors.add("Steps " + (position + 1) + "-" + (position + blockSize)
                        + " (order " + order + "): unexpected step '"
                        + actual.name + ":" + actual.runsWhere + "'");
            }
        }

        for (ActualStep missing : unmatched) {
            errors.add("Steps " + (position + 1) + "-" + (position + blockSize)
                    + " (order " + order + "): missing expected step '"
                    + missing.name + ":" + missing.runsWhere + "'");
        }
    }

    private static boolean removeFirstMatch(List<ActualStep> candidates, ActualStep target) {
        for (int i = 0; i < candidates.size(); i++) {
            ActualStep candidate = candidates.get(i);
            if (candidate.name.equals(target.name) && candidate.runsWhere == target.runsWhere) {
                candidates.remove(i);
                return true;
            }
        }
        return false;
    }

    private static class ActualStep {
        final String name;
        final RunsWhere runsWhere;

        ActualStep(String name, RunsWhere runsWhere) {
            this.name = name;
            this.runsWhere = runsWhere;
        }
    }
}
