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

import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.cloudformation.proxy.*;
import software.amazon.cloudformation.stackinstances.util.InstancesAnalyzer;
import software.amazon.cloudformation.stackinstances.util.StackInstancesPlaceHolder;

public class UpdateHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudFormationClient> proxyClient,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final ResourceModel previousModel = request.getPreviousResourceState();
        final StackInstancesPlaceHolder placeHolder = new StackInstancesPlaceHolder();

        InstancesAnalyzer.builder().desiredModel(model).previousModel(previousModel).build().analyzeForUpdate(placeHolder);

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        placeHolder.getDeleteStackInstances().size() > 0 ?
                                deleteStackInstances(proxy, proxyClient, progress, placeHolder.getDeleteStackInstances(), logger) :
                                ProgressEvent.defaultInProgressHandler(progress.getCallbackContext(), 0, progress.getResourceModel())
                )
                .then(progress ->
                        placeHolder.getCreateStackInstances().size() > 0 ?
                                createStackInstances(proxy, proxyClient, progress, placeHolder.getCreateStackInstances(), logger) :
                                ProgressEvent.defaultInProgressHandler(progress.getCallbackContext(), 0, progress.getResourceModel())
                )
                .then(progress ->
                        placeHolder.getUpdateStackInstances().size() > 0 ?
                                updateStackInstances(proxy, proxyClient, progress, placeHolder.getUpdateStackInstances(), logger) :
                                ProgressEvent.defaultInProgressHandler(progress.getCallbackContext(), 0, progress.getResourceModel())
                )
                .then(progress -> ProgressEvent.defaultSuccessHandler(model));
    }
}
