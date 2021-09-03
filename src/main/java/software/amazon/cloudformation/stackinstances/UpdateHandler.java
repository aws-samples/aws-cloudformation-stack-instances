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
