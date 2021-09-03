# ProServe::Cloudformation::StackInstances

Resource schema to add cloudformation stack instances to stacksets

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "ProServe::Cloudformation::StackInstances",
    "Properties" : {
        "<a href="#stacksetname" title="StackSetName">StackSetName</a>" : <i>String</i>,
        "<a href="#capabilities" title="Capabilities">Capabilities</a>" : <i>[ String, ... ]</i>,
        "<a href="#stackinstances" title="StackInstances">StackInstances</a>" : <i><a href="stackinstances.md">StackInstances</a></i>,
        "<a href="#operationpreferences" title="OperationPreferences">OperationPreferences</a>" : <i><a href="operationpreferences.md">OperationPreferences</a></i>,
        "<a href="#parameters" title="Parameters">Parameters</a>" : <i>[ <a href="parameter.md">Parameter</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: ProServe::Cloudformation::StackInstances
Properties:
    <a href="#stacksetname" title="StackSetName">StackSetName</a>: <i>String</i>
    <a href="#capabilities" title="Capabilities">Capabilities</a>: <i>
      - String</i>
    <a href="#stackinstances" title="StackInstances">StackInstances</a>: <i><a href="stackinstances.md">StackInstances</a></i>
    <a href="#operationpreferences" title="OperationPreferences">OperationPreferences</a>: <i><a href="operationpreferences.md">OperationPreferences</a></i>
    <a href="#parameters" title="Parameters">Parameters</a>: <i>
      - <a href="parameter.md">Parameter</a></i>
</pre>

## Properties

#### StackSetName

The name to associate with the stack set. The name must be unique in the Region where you create your stack set.

_Required_: Yes

_Type_: String

_Maximum_: <code>128</code>

_Pattern_: <code>^[a-zA-Z][a-zA-Z0-9\-]{0,127}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Capabilities

In some cases, you must explicitly acknowledge that your stack set template contains certain capabilities in order for AWS CloudFormation to create the stack set and related stack instances.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### StackInstances

Stack instances in some specific accounts and Regions.

_Required_: Yes

_Type_: <a href="stackinstances.md">StackInstances</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### OperationPreferences

The user-specified preferences for how AWS CloudFormation performs a stack set operation.

_Required_: No

_Type_: <a href="operationpreferences.md">OperationPreferences</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Parameters

The input parameters for the stack set template.

_Required_: No

_Type_: List of <a href="parameter.md">Parameter</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the InstanceId.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### InstanceId

Id is automatically generated on creation and assigned as the unique identifier.

