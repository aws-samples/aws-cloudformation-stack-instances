package software.amazon.cloudformation.stackinstances;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

//    @Test
//    public void handleRequest_SimpleSuccess() {
//        final ReadHandler handler = new ReadHandler();
//
//        final Set<String> regions= new HashSet<>();
//        regions.add("eu-west-1");
//        final Set<String> accounts= new HashSet<>();
//        accounts.add("123456789012");
//
//        final DeploymentTargets targets = DeploymentTargets
//                .builder()
//                .accounts(accounts)
//                .build();
//
//        final StackInstances stackInstancesGroup = StackInstances
//                .builder()
//                .regions(regions)
//                .deploymentTargets(targets)
//                .build();
//        final Set<StackInstances> groups = new HashSet<>();
//        groups.add(stackInstancesGroup);
//
//        final ResourceModel model = ResourceModel.builder()
//            .stackSetName("test")
////            .stackInstancesGroup(groups)
//            .build();
//
//        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
//            .desiredResourceState(model)
//            .build();
//
//        final ProgressEvent<ResourceModel, CallbackContext> response
//            = handler.handleRequest(proxy, request, null, logger);
//
//        assertThat(response).isNotNull();
//        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
//        assertThat(response.getCallbackContext()).isNull();
//        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
//        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
//        assertThat(response.getResourceModels()).isNull();
//        assertThat(response.getMessage()).isNull();
//        assertThat(response.getErrorCode()).isNull();
//    }
}