package software.amazon.cloudformation.stackinstances.util;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.cloudformation.model.PermissionModels;
import software.amazon.cloudformation.stackinstances.ResourceModel;

import java.util.Collection;
import java.util.Map;

/**
 * Utility class to help comparing previous model and desire model
 */
public class Comparator {

    /**
     * Compares if two collections equal in a null-safe way.
     *
     * @param collection1
     * @param collection2
     * @return boolean indicates if two collections equal.
     */
    public static boolean equals(final Collection<?> collection1, final Collection<?> collection2) {
        boolean equals = false;
        if (collection1 != null && collection2 != null) {
            equals = collection1.size() == collection2.size()
                    && collection1.containsAll(collection2) && collection2.containsAll(collection1);
        } else if (collection1 == null && collection2 == null) {
            equals = true;
        }
        return equals;
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
