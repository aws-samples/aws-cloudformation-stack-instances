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

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.cloudformation.model.PermissionModels;
import software.amazon.cloudformation.stackinstances.Parameter;
import software.amazon.cloudformation.stackinstances.ResourceModel;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Utility class to help comparing previous model and desire model
 */
public class Comparator {

    /**
     * Compares if two collections equal in a null-safe way.
     *
     * @param parameters1
     * @param parameters2
     * @return boolean indicates if two collections equal.
     */
    public static boolean equals(final Set<Parameter> parameters1, final Set<Parameter> parameters2) {
        if (parameters1 == null && parameters2 == null) return true;
        if (parameters1 == null || parameters2 == null) return false;
        for (Parameter param1 : parameters1) {
            if (parameters2.stream().noneMatch(p -> p.getParameterKey().compareTo(param1.getParameterKey()) == 0)) return false;
            Optional<Parameter> param2 = parameters2.stream().filter(p -> p.getParameterKey().compareTo(param1.getParameterKey()) == 0).findFirst();
            if (!param2.isPresent() || param2.get().getParameterValue().compareTo(param1.getParameterValue()) != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares if two objects equal in a null-safe way.
     *
     * @param object1
     * @param object2
     * @return boolean indicates if two objects equal.
     */
    public static boolean equals(final Object object1, final Object object2) {
        boolean equals = false;
        if (object1 != null && object2 != null) {
            equals = object1.equals(object2);
        } else if (object1 == null && object2 == null) {
            equals = true;
        }
        return equals;
    }
}
