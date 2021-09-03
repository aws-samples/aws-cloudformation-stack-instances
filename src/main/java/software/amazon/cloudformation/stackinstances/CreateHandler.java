package software.amazon.cloudformation.stackinstances;

import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.CreateStackInstancesResponse;
import software.amazon.awssdk.services.cloudformation.model.CreateStackSetResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.*;
import software.amazon.cloudformation.stackinstances.util.InstancesAnalyzer;
import software.amazon.cloudformation.stackinstances.util.StackInstancesPlaceHolder;

import java.util.UUID;

import static software.amazon.cloudformation.stackinstances.translator.RequestTranslator.createStackInstancesRequest;

public class CreateHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudFormationClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final StackInstancesPlaceHolder placeHolder = new StackInstancesPlaceHolder();

        InstancesAnalyzer.builder().desiredModel(model).build().analyzeForCreate(placeHolder);
        if (model.getInstanceId() == null) {
            model.setInstanceId(UUID.randomUUID().toString());
        }
        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> createStackInstances(proxy, proxyClient, progress, placeHolder.getCreateStackInstances(), logger))
                .then(progress -> ProgressEvent.defaultSuccessHandler(model));
    }
}
