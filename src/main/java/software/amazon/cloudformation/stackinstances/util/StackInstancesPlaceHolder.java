package software.amazon.cloudformation.stackinstances.util;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class StackInstancesPlaceHolder {

    private Set<StackInstance> createStackInstances = new HashSet<>();

    private Set<StackInstance> deleteStackInstances = new HashSet<>();

    private Set<StackInstance> updateStackInstances = new HashSet<>();
}
