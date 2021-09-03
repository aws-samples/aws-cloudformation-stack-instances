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

package software.amazon.cloudformation.stackinstances.translator;

import software.amazon.awssdk.services.cloudformation.model.*;
import software.amazon.cloudformation.stackinstances.ResourceModel;
import software.amazon.cloudformation.stackinstances.util.StackInstance;

import java.util.Set;

import static software.amazon.cloudformation.stackinstances.translator.PropertyTranslator.*;

public class RequestTranslator {

    private static final int LIST_MAX_ITEMS = 100;
    private static final CallAs CALL_AS = CallAs.SELF;

    /**
     * Request to create a resource
     * @param model resource model
     * @param stackInstances
     * @return awsRequest the aws service request to create a resource
     */
    public static CreateStackInstancesRequest createStackInstancesRequest(final ResourceModel model, final Set<StackInstance> stackInstances) {
        return CreateStackInstancesRequest.builder()
                .stackSetName(model.getStackSetName())
                .regions(translateToRegionsList(stackInstances))
                .deploymentTargets(translateToSdkDeploymentTargets(stackInstances))
                .operationPreferences(translateToSdkOperationPreferences(model.getOperationPreferences()))
                .parameterOverrides(translateToSdkParameters(model.getParameters()))
                .callAs(CALL_AS)
                .build();
    }

    public static UpdateStackInstancesRequest updateStackInstancesRequest(final ResourceModel model, final Set<StackInstance> stackInstances) {
        return UpdateStackInstancesRequest.builder()
                .stackSetName(model.getStackSetName())
                .regions(translateToRegionsList(stackInstances))
                .deploymentTargets(translateToSdkDeploymentTargets(stackInstances))
                .operationPreferences(translateToSdkOperationPreferences(model.getOperationPreferences()))
                .parameterOverrides(translateToSdkParameters(model.getParameters()))
                .callAs(CALL_AS)
                .build();
    }

    public static DeleteStackInstancesRequest deleteStackInstancesRequest(final ResourceModel model, final Set<StackInstance> stackInstances) {
        return DeleteStackInstancesRequest.builder()
                .stackSetName(model.getStackSetName())
                .regions(translateToRegionsList(stackInstances))
                .deploymentTargets(translateToSdkDeploymentTargets(stackInstances))
                .operationPreferences(translateToSdkOperationPreferences(model.getOperationPreferences()))
                .callAs(CALL_AS)
                .build();
    }

    public static ListStackInstancesRequest listStackInstancesRequest(
            final String nextToken,
            final String stackSetName) {
        return ListStackInstancesRequest.builder()
                .maxResults(LIST_MAX_ITEMS)
                .nextToken(nextToken)
                .stackSetName(stackSetName)
                .callAs(CALL_AS)
                .build();
    }

    public static DescribeStackInstanceRequest describeStackInstanceRequest(
            final String account,
            final String region,
            final String stackSetId) {
        return DescribeStackInstanceRequest.builder()
                .stackInstanceAccount(account)
                .stackInstanceRegion(region)
                .stackSetName(stackSetId)
                .callAs(CALL_AS)
                .build();
    }

    public static DescribeStackSetOperationRequest describeStackSetOperationRequest(
            final String stackSetName,
            final String operationId) {
        return DescribeStackSetOperationRequest.builder()
                .stackSetName(stackSetName)
                .operationId(operationId)
                .callAs(CALL_AS)
                .build();
    }

    public static DescribeStackSetRequest describeStackSetRequest(
            final String stackSetId) {
        return DescribeStackSetRequest.builder()
                .stackSetName(stackSetId)
                .callAs(CALL_AS)
                .build();
    }
}
