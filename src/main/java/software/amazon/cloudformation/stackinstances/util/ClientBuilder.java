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

import com.amazonaws.util.StringUtils;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.core.retry.RetryPolicyContext;
import software.amazon.awssdk.core.retry.backoff.BackoffStrategy;
import software.amazon.awssdk.core.retry.conditions.OrRetryCondition;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.cloudformation.LambdaWrapper;

public class ClientBuilder {

    private ClientBuilder() {
    }

    public static CloudFormationClient getClient() {
        return LazyHolder.SERVICE_CLIENT;
    }

    /**
     * Get CloudFormationClient for requests to interact with StackSet client
     *
     * @return {@link CloudFormationClient}
     */
    private static class LazyHolder {

        private static final Integer MAX_RETRIES = 5;

        public static CloudFormationClient SERVICE_CLIENT = CloudFormationClient.builder()
                .httpClient(LambdaWrapper.HTTP_CLIENT)
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .retryPolicy(RetryPolicy.builder()
                                .backoffStrategy(BackoffStrategy.defaultThrottlingStrategy())
                                .throttlingBackoffStrategy(BackoffStrategy.defaultThrottlingStrategy())
                                .numRetries(MAX_RETRIES)
                                .retryCondition(OrRetryCondition.create(new RetryCondition[]{
                                        RetryCondition.defaultRetryCondition(),
                                        CloudFormationRetryCondition.create()
                                }))
                                .build())
                        .build())
                .build();
    }

    /**
     * CloudFormation Throttling Exception StatusCode is 400 while default throttling code is 429
     * https://github.com/aws/aws-sdk-java-v2/blob/master/core/sdk-core/src/main/java/software/amazon/awssdk/core/exception/SdkServiceException.java#L91
     * which means we would need to customize a RetryCondition
     */
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class CloudFormationRetryCondition implements RetryCondition {

        public static CloudFormationRetryCondition create() {
            return new CloudFormationRetryCondition();
        }

        @Override
        public boolean shouldRetry(RetryPolicyContext context) {
            final String errorMessage = context.exception().getMessage();
            if (StringUtils.isNullOrEmpty(errorMessage)) return false;
            return errorMessage.contains("Rate exceeded");
        }
    }

}
