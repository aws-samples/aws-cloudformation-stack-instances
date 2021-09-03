/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package software.amazon.cloudformation.stackinstances.util;

import lombok.Builder;
import lombok.Data;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.stackinstances.DeploymentTargets;
import software.amazon.cloudformation.stackinstances.Parameter;
import software.amazon.cloudformation.stackinstances.ResourceModel;
import software.amazon.cloudformation.stackinstances.StackInstances;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class to hold {@link StackInstances} that need to be modified during the update
 */
@Builder
@Data
public class InstancesAnalyzer {

    private ResourceModel previousModel;

    private ResourceModel desiredModel;

    /**
     * Aggregates flat {@link StackInstance} to a group of {@link StackInstances} to call
     * corresponding StackSet APIs
     *
     * @param flatStackInstances {@link StackInstance}
     * @return {@link StackInstances} set
     */
    public static Set<StackInstances> aggregateStackInstances(
            final Set<StackInstance> flatStackInstances) {
        final Set<StackInstances> groupedStacks = groupInstancesByTargets(flatStackInstances);
        return aggregateInstancesByRegions(groupedStacks);
    }


    /**
     * Group regions by {@link DeploymentTargets} and {@link StackInstance#getParameters()}
     *
     * @return {@link StackInstances}
     */
    private static Set<StackInstances> groupInstancesByTargets(
            final Set<StackInstance> flatStackInstances) {

        final Map<List<Object>, StackInstances> groupedStacksMap = new HashMap<>();
        for (final StackInstance stackInstance : flatStackInstances) {
            final String target = stackInstance.getDeploymentTarget();
            final String region = stackInstance.getRegion();
            final Set<Parameter> parameterSet = stackInstance.getParameters();
            final List<Object> compositeKey = Arrays.asList(target, parameterSet);

            if (groupedStacksMap.containsKey(compositeKey)) {
                groupedStacksMap.get(compositeKey).getRegions().add(stackInstance.getRegion());
            } else {
                final DeploymentTargets targets = DeploymentTargets.builder().build();
                targets.setAccounts(new HashSet<>(Arrays.asList(target)));

                final StackInstances stackInstances = StackInstances.builder()
                        .regions(new HashSet<>(Arrays.asList(region)))
                        .deploymentTargets(targets)
                        .parameterOverrides(parameterSet)
                        .build();
                groupedStacksMap.put(compositeKey, stackInstances);
            }
        }
        return new HashSet<>(groupedStacksMap.values());
    }

    /**
     * Aggregates instances with similar {@link StackInstances#getRegions()}
     *
     * @param groupedStacks {@link StackInstances} set
     * @return Aggregated {@link StackInstances} set
     */
    private static Set<StackInstances> aggregateInstancesByRegions(
            final Set<StackInstances> groupedStacks) {

        final Map<List<Object>, StackInstances> groupedStacksMap = new HashMap<>();
        for (final StackInstances stackInstances : groupedStacks) {
            final DeploymentTargets target = stackInstances.getDeploymentTargets();
            final Set<Parameter> parameterSet = stackInstances.getParameterOverrides();
            final List<Object> compositeKey = Arrays.asList(stackInstances.getRegions(), parameterSet);
            if (groupedStacksMap.containsKey(compositeKey)) {
                groupedStacksMap.get(compositeKey).getDeploymentTargets()
                        .getAccounts().addAll(target.getAccounts());
            } else {
                groupedStacksMap.put(compositeKey, stackInstances);
            }
        }
        return new HashSet<>(groupedStacksMap.values());
    }

    /**
     * Compares {@link StackInstance#getParameters()} with previous {@link StackInstance#getParameters()}
     * Gets the StackInstances need to update
     *
     * @param intersection     {@link StackInstance} retaining desired stack instances
     * @param previousStackMap Map contains previous stack instances
     * @return {@link StackInstance} to update
     */
    private static Set<StackInstance> getUpdatingStackInstances(
            final Set<StackInstance> intersection,
            final Map<StackInstance, StackInstance> previousStackMap) {

        return intersection.stream()
                .filter(stackInstance -> !Comparator.equals(
                        previousStackMap.get(stackInstance).getParameters(), stackInstance.getParameters()))
                .collect(Collectors.toSet());
    }

    /**
     * Since Stack instances are defined across accounts and regions with(out) parameters,
     * We are expanding all before we tack actions
     *
     * @param stackInstances {@link ResourceModel#getStackInstances()}
     * @return {@link StackInstance} set
     */
    private static Set<StackInstance> flattenStackInstances(
            final StackInstances stackInstances) {

        final Set<StackInstance> flatStacks = new HashSet<>();

        for (final String region : stackInstances.getRegions()) {

            final Set<String> targets = stackInstances.getDeploymentTargets().getAccounts();

            // Validates expected DeploymentTargets exist in the template
            if (CollectionUtils.isNullOrEmpty(targets)) {
                throw new CfnInvalidRequestException(
                        String.format("%s should be specified in DeploymentTargets in [%s] model",
                                "Accounts",
                                "SELF_MANAGED"));
            }

            for (final String target : targets) {
                final StackInstance stackInstance = StackInstance.builder()
                        .region(region).deploymentTarget(target).parameters(stackInstances.getParameterOverrides())
                        .build();

                // Validates no duplicated stack instance is specified
                if (flatStacks.contains(stackInstance)) {
                    throw new CfnInvalidRequestException(
                            String.format("Stack instance [%s,%s] is duplicated", target, region));
                }

                flatStacks.add(stackInstance);
            }
        }
        return flatStacks;
    }

    /**
     * Analyzes {@link StackInstances} that need to be modified during the update operations
     *
     * @param placeHolder {@link software.amazon.cloudformation.stackinstances.util.StackInstancesPlaceHolder}
     */
    public void analyzeForUpdate(final software.amazon.cloudformation.stackinstances.util.StackInstancesPlaceHolder placeHolder) {
        final boolean isSelfManaged = true;

        final Set<StackInstance> previousStackInstances =
                flattenStackInstances(previousModel.getStackInstances());
        final Set<StackInstance> desiredStackInstances =
                flattenStackInstances(desiredModel.getStackInstances());

        // Calculates all necessary differences that we need to take actions
        final Set<StackInstance> stacksToAdd = new HashSet<>(desiredStackInstances);
        stacksToAdd.removeAll(previousStackInstances);
        final Set<StackInstance> stacksToDelete = new HashSet<>(previousStackInstances);
        stacksToDelete.removeAll(desiredStackInstances);
        final Set<StackInstance> stacksToCompare = new HashSet<>(desiredStackInstances);
        stacksToCompare.retainAll(previousStackInstances);

        // Since StackInstance.parameters is excluded for @EqualsAndHashCode,
        // we needs to construct a key value map to keep track on previous StackInstance objects
        final Set<StackInstance> stacksToUpdate = getUpdatingStackInstances(
                stacksToCompare, previousStackInstances.stream().collect(Collectors.toMap(s -> s, s -> s)));

        // Update the stack lists that need to write of callbackContext holder
        placeHolder.setCreateStackInstances(stacksToAdd);
        placeHolder.setDeleteStackInstances(stacksToDelete);
        placeHolder.setUpdateStackInstances(stacksToUpdate);
    }

    /**
     * Analyzes {@link StackInstances} that need to be modified during create operations
     *
     * @param placeHolder {@link software.amazon.cloudformation.stackinstances.util.StackInstancesPlaceHolder}
     */
    public void analyzeForCreate(final software.amazon.cloudformation.stackinstances.util.StackInstancesPlaceHolder placeHolder) {
        if (desiredModel.getStackInstances() == null) return;
        final Set<StackInstance> desiredStackInstances =
                flattenStackInstances(desiredModel.getStackInstances());

        placeHolder.setCreateStackInstances(desiredStackInstances);
    }

    /**
     * Analyzes {@link StackInstances} that need to be modified during delete operations
     *
     * @param placeHolder {@link software.amazon.cloudformation.stackinstances.util.StackInstancesPlaceHolder}
     */
    public void analyzeForDelete(final software.amazon.cloudformation.stackinstances.util.StackInstancesPlaceHolder placeHolder) {
        if (desiredModel.getStackInstances() == null) return;

        final Set<StackInstance> desiredStackInstances =
                flattenStackInstances(desiredModel.getStackInstances());
        placeHolder.setDeleteStackInstances(desiredStackInstances);
    }
}
