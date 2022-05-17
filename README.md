**repository _moved_, maintained elsewhere -> https://github.com/curlim/aws-cloudformation-stack-instances**

# Concurrent AWS CloudFormation Stack Instance Resource Provider Type

This repository implements an AWS CloudFormation Resource Provider,
which **concurrently** creates stack instances within a particular AWS Cloudformation StackSet.

## ProServe::Cloudformation::StackInstances

See example usage of the resource below. Detailed documentation can be found in the [/docs](docs) folder.

Cloudformation itself has not yet the capability to run operations concurrent on StackSets. This resource helps to overcome this by implementing a retry mechanism.
Whenever an operation is already ongoing on the same Stackset, the resource provider takes care of retrying with proper backoff configurations. 

Hence, this resource can be used to meet requirements from different use cases without taking care of currently running operations.

### Usage

```yaml
InstanceC:
  Type: ProServe::Cloudformation::StackInstances
  Properties:
    StackSetName: test
    StackInstances:
      DeploymentTargets:
        Accounts:
        - '123456789012'
        - '123456789013'
      Regions:
      - eu-west-1
      - us-east-1
    OperationPreferences:
      FailureToleranceCount: 0
      MaxConcurrentCount: 1
      RegionConcurrencyType: PARALLEL
    Capabilities:
    - CAPABILITY_IAM
```

### Typical Use Cases
####  AWS Account Vending process integrated in AWS Service Catalog

This resource provider can be used within an Account Blueprint published via AWS Service Catalog Products.
In case multiple accounts are requested at once, the resource provider takes care handling the concurrency.

#### AWS Service Catalog Products

Any generic AWS Service Catalog Product might have a pre-requisite to enroll a region or account into a central CloudFormation Stackset.
Multiple users might launch a product at once, which can result in operation conflicts on CloudFormation StackSets. This resource can be integrated in any product template and takes care of concurrency. 


### Quickstart

You can use the following link to deploy the CloudFormation resource provider directly into your AWS account. Ensure you are logged into the AWS Console before following it.
After following the link, ensure you picked the desired **Region** on the top right within the AWS Console.

[Quickstart CloudFormation Link](https://console.aws.amazon.com/cloudformation/home?region=eu-west-1#/stacks/new?templateURL=https:%2F%2Fs3.amazonaws.com%2Faws-enterprise-jumpstart%2Faws-cloudformation-stack-instances%2Fcfn-provider-registration.yaml)

### Manual Deployment

For manual deployments check the [cfn-provider-registration.yaml](quickstart/cfn-provider-registration.yaml) CloudFormation template.

### Development

Please refer to the AWS CloudFormation CLI documentation [https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-types.html](https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-types.html).

#### Pre-requisites

* Install CFN CLI with Java Plugin: https://github.com/aws-cloudformation/cloudformation-cli-java-plugin
* Install Docker and start service
* Install SAM CLI: https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html
* Have Java 1.8 and Maven installed

#### Build

* `mvn package` to build sources
* `cfn submit --region <aws-region> --dry-run` for a dry-run
* `cfn submit --region <aws-region>` to finally submit the resource provider to AWS CloudFormation

### Future Work

* Add the option to specify multiple deployment targets with different parameter overrides.

## Notes

* While code samples in this repository has been tested and believe it works well, as always, be sure to test it in your environment before using it in production!

> Read and List operations implementations do not meet the contract test requirements. This is on purpose, as deleting twice or creating of stack instances multiple times does create any drift and still ensures proper state handling. Additionally, adoption of this AWS CloudFormation resource provider is very straight forward this way. Instances do not have to be deleted first and re-created with this resource.

The RPDK will automatically generate the correct resource model from the schema whenever the project is built via Maven. You can also do this manually with the following command: `cfn generate`.

> Please don't modify files under `target/generated-sources/rpdk`, as they will be automatically overwritten.

The code uses [Lombok](https://projectlombok.org/), and [you may have to install IDE integrations](https://projectlombok.org/setup/overview) to enable auto-complete for Lombok-annotated classes.

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This project is licensed under the MIT-0 License.

