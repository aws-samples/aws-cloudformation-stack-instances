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

package software.amazon.cloudformation.stackinstances;

import com.google.common.annotations.VisibleForTesting;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.*;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.TerminalException;
import software.amazon.cloudformation.proxy.*;
import software.amazon.cloudformation.proxy.delay.MultipleOf;
import software.amazon.cloudformation.stackinstances.util.ClientBuilder;
import software.amazon.cloudformation.stackinstances.util.InstancesAnalyzer;
import software.amazon.cloudformation.stackinstances.util.StackInstance;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static software.amazon.cloudformation.stackinstances.translator.RequestTranslator.*;

/**
 * Placeholder for the functionality that could be shared across Create/Read/Update/Delete/List Handlers
 */
public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

    protected static final MultipleOf MULTIPLE_OF = MultipleOf.multipleOf()
            .multiple(2)
            .timeout(Duration.ofHours(24L))
            .delay(Duration.ofSeconds(2L))
            .build();

    /**
     * Retrieves the {@link StackSetOperationStatus} from {@link DescribeStackSetOperationResponse}
     *
     * @param stackSetId  {@link ResourceModel#getStackSetName()}
     * @param operationId Operation ID
     * @return {@link StackSetOperationStatus}
     */
    private static StackSetOperationStatus getStackSetOperationStatus(
            final ProxyClient<CloudFormationClient> proxyClient,
            final String stackSetId,
            final String operationId) {

        final DescribeStackSetOperationResponse response = proxyClient.injectCredentialsAndInvokeV2(
                describeStackSetOperationRequest(stackSetId, operationId),
                proxyClient.client()::describeStackSetOperation);
        return response.stackSetOperation().status();
    }

    /**
     * Compares {@link StackSetOperationStatus} with specific statuses
     *
     * @param status      {@link StackSetOperationStatus}
     * @param operationId Operation ID
     * @return boolean
     */
    @VisibleForTesting
    protected static boolean isStackSetOperationDone(
            final StackSetOperationStatus status, final String operationId, final Logger logger) {

        switch (status) {
            case SUCCEEDED:
                logger.log(String.format("StackSet Operation [%s] has been successfully stabilized.", operationId));
                return true;
            case RUNNING:
            case QUEUED:
                return false;
            default:
                logger.log(String.format("StackInstanceOperation [%s] unexpected status [%s]", operationId, status));
                throw new TerminalException(
                        String.format("Stack set operation [%s] was unexpectedly stopped or failed", operationId));
        }
    }

    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        logger.log(request.getDesiredResourceState().toString());
        return handleRequest(proxy, request, callbackContext != null ?
                callbackContext : new CallbackContext(), proxy.newProxy(ClientBuilder::getClient), logger);
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudFormationClient> proxyClient,
            final Logger logger);

    protected boolean filterException(AwsRequest request, Exception e, ProxyClient<CloudFormationClient> client, ResourceModel model, CallbackContext context) {
        return e instanceof OperationInProgressException;
    }

    /**
     * Invocation of CreateStackInstances would possibly used by CREATE/UPDATE handler, after the template being analyzed
     * by {@link InstancesAnalyzer}
     *
     * @param proxy          {@link AmazonWebServicesClientProxy} to initiate proxy chain
     * @param client         the aws service client {@link ProxyClient<CloudFormationClient>} to make the call
     * @param progress       {@link ProgressEvent<ResourceModel, CallbackContext>} to place hold the current progress data
     * @param stackInstances StackInstances that need to create, see in {@link InstancesAnalyzer#analyzeForCreate}
     * @param logger         {@link Logger}
     * @return {@link ProgressEvent<ResourceModel, CallbackContext>}
     */
    protected ProgressEvent<ResourceModel, CallbackContext> createStackInstances(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<CloudFormationClient> client,
            final ProgressEvent<ResourceModel, CallbackContext> progress,
            final Set<StackInstance> stackInstances,
            final Logger logger) {

        final ResourceModel model = progress.getResourceModel();
        final CallbackContext callbackContext = progress.getCallbackContext();
        return proxy
                .initiate("AWS-CloudFormation-StackSet::CreateStackInstances", client, model, callbackContext)
                .translateToServiceRequest(modelRequest -> createStackInstancesRequest(model, stackInstances))
                .backoffDelay(MULTIPLE_OF)
                .makeServiceCall((modelRequest, proxyInvocation) -> {
                    final CreateStackInstancesResponse response = proxyInvocation.injectCredentialsAndInvokeV2(modelRequest, proxyInvocation.client()::createStackInstances);
                    logger.log(String.format("%s [%s] CreateStackInstances initiated", ResourceModel.TYPE_NAME, model.getStackSetName()));
                    return response;
                })
                .stabilize((request, response, proxyInvocation, resourceModel, context) -> isOperationStabilized(proxyInvocation, resourceModel, response.operationId(), logger))
                .retryErrorFilter(this::filterException)
                .progress();
    }

    /**
     * Invocation of DeleteStackInstances would possibly used by UPDATE/DELETE handler, after the template being analyzed
     * by {@link InstancesAnalyzer}
     *
     * @param proxy              {@link AmazonWebServicesClientProxy} to initiate proxy chain
     * @param client             the aws service client {@link ProxyClient<CloudFormationClient>} to make the call
     * @param progress           {@link ProgressEvent<ResourceModel, CallbackContext>} to place hold the current progress data
     * @param stackInstances StackInstances that need to create, see in {@link InstancesAnalyzer#analyzeForDelete}
     * @param logger             {@link Logger}
     * @return {@link ProgressEvent<ResourceModel, CallbackContext>}
     */
    protected ProgressEvent<ResourceModel, CallbackContext> deleteStackInstances(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<CloudFormationClient> client,
            final ProgressEvent<ResourceModel, CallbackContext> progress,
            final Set<StackInstance> stackInstances,
            final Logger logger) {

        final ResourceModel model = progress.getResourceModel();
        final CallbackContext callbackContext = progress.getCallbackContext();

        return proxy
                .initiate("AWS-CloudFormation-StackSet::DeleteStackInstances", client, model, callbackContext)
                .translateToServiceRequest(modelRequest -> deleteStackInstancesRequest(modelRequest, stackInstances))
                .backoffDelay(MULTIPLE_OF)
                .makeServiceCall((modelRequest, proxyInvocation) -> {
                    final DeleteStackInstancesResponse response = proxyInvocation.injectCredentialsAndInvokeV2(modelRequest, proxyInvocation.client()::deleteStackInstances);
                    logger.log(String.format("%s [%s] CreateStackInstances initiated", ResourceModel.TYPE_NAME, model.getStackSetName()));
                    return response;
                })
                .stabilize((request, response, proxyInvocation, resourceModel, context) -> isOperationStabilized(proxyInvocation, resourceModel, response.operationId(), logger))
                .retryErrorFilter(this::filterException)
                .progress();
    }

    /**
     * Invocation of DeleteStackInstances would possibly used by DELETE handler, after the template being analyzed
     * by {@link InstancesAnalyzer}
     *
     * @param proxy              {@link AmazonWebServicesClientProxy} to initiate proxy chain
     * @param client             the aws service client {@link ProxyClient<CloudFormationClient>} to make the call
     * @param progress           {@link ProgressEvent<ResourceModel, CallbackContext>} to place hold the current progress data
     * @param stackInstances StackInstances that need to create, see in {@link InstancesAnalyzer#analyzeForUpdate}
     * @param logger             {@link Logger}
     * @return {@link ProgressEvent<ResourceModel, CallbackContext>}
     */
    protected ProgressEvent<ResourceModel, CallbackContext> updateStackInstances(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<CloudFormationClient> client,
            final ProgressEvent<ResourceModel, CallbackContext> progress,
            final Set<StackInstance> stackInstances,
            final Logger logger) {

        final ResourceModel model = progress.getResourceModel();
        final CallbackContext callbackContext = progress.getCallbackContext();

        return progress
            .then(_progress ->
                    proxy
                    .initiate("AWS-CloudFormation-StackSet::UpdateStackInstances", client, model, callbackContext)
                    .translateToServiceRequest(modelRequest -> updateStackInstancesRequest(modelRequest, stackInstances))
                    .backoffDelay(MULTIPLE_OF)
                    .makeServiceCall((modelRequest, proxyInvocation) -> {
                        final UpdateStackInstancesResponse response = proxyInvocation.injectCredentialsAndInvokeV2(modelRequest, proxyInvocation.client()::updateStackInstances);
                        logger.log(String.format("%s [%s] CreateStackInstances initiated", ResourceModel.TYPE_NAME, model.getStackSetName()));
                        return response;
                    })
                    .stabilize((request, response, proxyInvocation, resourceModel, context) -> isOperationStabilized(proxyInvocation, resourceModel, response.operationId(), logger))
                    .retryErrorFilter(this::filterException)
                    .progress()

            );
    }

    /**
     * Checks if the operation is stabilized using OperationId to interact with
     * {@link DescribeStackSetOperationResponse}
     *
     * @param model       {@link ResourceModel}
     * @param operationId OperationId from operation response
     * @param logger      Logger
     * @return A boolean value indicates if operation is complete
     */
    protected boolean isOperationStabilized(final ProxyClient<CloudFormationClient> proxyClient,
                                            final ResourceModel model,
                                            final String operationId,
                                            final Logger logger) {

        final String stackSetId = model.getStackSetName();
        final StackSetOperationStatus status = getStackSetOperationStatus(proxyClient, stackSetId, operationId);
        return isStackSetOperationDone(status, operationId, logger);
    }

    /**
     * Describe {@link StackSet} from service client using stackSetId
     *
     * @param proxy                 {@link AmazonWebServicesClientProxy} to initiate proxy chain
     * @param client                the aws service client {@link ProxyClient<CloudFormationClient>} to make the call
     * @param progress              {@link ProgressEvent<ResourceModel, CallbackContext>} to place hold the current progress data
     * @param logger                {@link Logger}
     * @throws CfnNotFoundException If the StackSet is DELETED, return NotFound exception
     * @return {@link ProgressEvent<ResourceModel, CallbackContext>}
     */
    protected ProgressEvent<ResourceModel, CallbackContext> describeStackSet(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<CloudFormationClient> client,
            final ProgressEvent<ResourceModel, CallbackContext> progress,
            final Logger logger) {

        final ResourceModel model = progress.getResourceModel();
        final CallbackContext callbackContext = progress.getCallbackContext();

        final ProgressEvent<ResourceModel, CallbackContext> progressEvent = proxy
                .newInitiator(client, model, callbackContext)
                .translateToServiceRequest(modelRequest -> describeStackSetRequest(modelRequest.getStackSetName()))
                .backoffDelay(MULTIPLE_OF)
                .makeServiceCall((modelRequest, proxyInvocation) -> {
                    final DescribeStackSetResponse response = proxyInvocation.injectCredentialsAndInvokeV2(modelRequest, proxyInvocation.client()::describeStackSet);
                    logger.log(String.format("Describe StackSet [%s] successfully", model.getStackSetName()));
                    if (StackSetStatus.DELETED == response.stackSet().status()) {
                        logger.log(String.format("StackSet [%s] is %s", model.getStackSetName(), StackSetStatus.DELETED.toString()));
                        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getStackSetName());
                    }
                    return response;
                })
                .progress();

        if (!progressEvent.isSuccess()) {
            return progressEvent;
        }

        return ProgressEvent.progress(model, callbackContext);
    }
}
