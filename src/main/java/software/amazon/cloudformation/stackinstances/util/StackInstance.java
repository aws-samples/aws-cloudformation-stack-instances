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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import software.amazon.cloudformation.stackinstances.Parameter;

import java.util.Set;

@Data
@Builder
@EqualsAndHashCode
public class StackInstance {

    @JsonProperty("Region")
    private String region;

    @JsonProperty("DeploymentTarget")
    private String deploymentTarget;

    @EqualsAndHashCode.Exclude
    private Set<Parameter> parameters;
}
