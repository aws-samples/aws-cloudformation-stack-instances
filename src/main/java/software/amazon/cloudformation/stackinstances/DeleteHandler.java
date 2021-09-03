package software.amazon.cloudformation.stackinstances;

import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.cloudformation.proxy.*;
import software.amazon.cloudformation.stackinstances.util.InstancesAnalyzer;
import software.amazon.cloudformation.stackinstances.util.StackInstancesPlaceHolder;

public class DeleteHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudFormationClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final StackInstancesPlaceHolder placeHolder = new StackInstancesPlaceHolder();

        InstancesAnalyzer.builder().desiredModel(model).build().analyzeForDelete(placeHolder);

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> deleteStackInstances(proxy, proxyClient, progress, placeHolder.getDeleteStackInstances(), logger))
                .then(progress -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .status(OperationStatus.SUCCESS)
                        .build());
    }
}
