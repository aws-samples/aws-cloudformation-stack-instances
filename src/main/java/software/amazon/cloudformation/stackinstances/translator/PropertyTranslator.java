package software.amazon.cloudformation.stackinstances.translator;

import software.amazon.awssdk.services.cloudformation.model.DeploymentTargets;
import software.amazon.awssdk.services.cloudformation.model.Parameter;
import software.amazon.awssdk.services.cloudformation.model.StackInstanceSummary;
import software.amazon.awssdk.services.cloudformation.model.StackSetOperationPreferences;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.cloudformation.stackinstances.OperationPreferences;
import software.amazon.cloudformation.stackinstances.util.StackInstance;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PropertyTranslator {

    /**
     * Converts resource model DeploymentTargets to StackSet SDK DeploymentTargets
     *
     * @param deploymentTargets DeploymentTargets from resource model
     * @return SDK DeploymentTargets
     */
    static DeploymentTargets translateToSdkDeploymentTargets(
            final Set<StackInstance> deploymentTargets) {
        Set<String> accounts = deploymentTargets.stream().map(StackInstance::getDeploymentTarget).collect(Collectors.toSet());
        return DeploymentTargets.builder()
                .accounts(accounts)
                .build();
    }

    static List<String> translateToRegionsList(final Set<StackInstance> deploymentTargets) {
        return deploymentTargets.stream().map(StackInstance::getRegion).distinct().collect(Collectors.toList());
    }

    /**
     * Converts StackSet SDK Parameters to resource model Parameters
     *
     * @param parameters Parameters collection from resource model
     * @return SDK Parameter list
     */
    static List<Parameter> translateToSdkParameters(
            final Collection<software.amazon.cloudformation.stackinstances.Parameter> parameters) {
        // To remove Parameters from a StackSet or StackSetInstance, set it as an empty list
        if (CollectionUtils.isNullOrEmpty(parameters)) return Collections.emptyList();
        return parameters.stream()
                .map(parameter -> Parameter.builder()
                        .parameterKey(parameter.getParameterKey())
                        .parameterValue(parameter.getParameterValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Converts resource model Parameters to StackSet SDK Parameters
     *
     * @param parameters Parameters from SDK
     * @return resource model Parameters
     */
    public static Set<software.amazon.cloudformation.stackinstances.Parameter> translateFromSdkParameters(
            final Collection<Parameter> parameters) {
        if (CollectionUtils.isNullOrEmpty(parameters)) return null;
        return parameters.stream()
                .map(parameter -> software.amazon.cloudformation.stackinstances.Parameter.builder()
                        .parameterKey(parameter.parameterKey())
                        .parameterValue(parameter.parameterValue())
                        .build())
                .collect(Collectors.toSet());
    }

    /**
     * Converts resource model OperationPreferences to StackSet SDK OperationPreferences
     *
     * @param operationPreferences OperationPreferences from resource model
     * @return SDK OperationPreferences
     */
    static StackSetOperationPreferences translateToSdkOperationPreferences(
            final OperationPreferences operationPreferences) {
        if (operationPreferences == null) return null;
        return StackSetOperationPreferences.builder()
                .maxConcurrentCount(operationPreferences.getMaxConcurrentCount())
                .maxConcurrentPercentage(operationPreferences.getMaxConcurrentPercentage())
                .failureToleranceCount(operationPreferences.getFailureToleranceCount())
                .failureTolerancePercentage(operationPreferences.getFailureTolerancePercentage())
                .regionOrder(operationPreferences.getRegionOrder())
                .regionConcurrencyType(operationPreferences.getRegionConcurrencyType())
                .build();
    }

    /**
     * Converts {@link StackInstanceSummary} to {@link StackInstance} utility placeholder
     *
     * @param summary       {@link StackInstanceSummary}
     * @return {@link StackInstance}
     */
    public static StackInstance translateToStackInstance(
            final StackInstanceSummary summary,
            final Collection<Parameter> parameters) {

        return StackInstance.builder()
                .region(summary.region())
                .parameters(translateFromSdkParameters(parameters))
                .deploymentTarget(summary.account())
                .build();
    }
}