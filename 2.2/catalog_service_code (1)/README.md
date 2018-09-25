# Microservices course project

# Pre-requisites
You can download the local DynamoDB library here: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.html

# Local Development
This section shows how you can run locally the application,communicating with a local DynamoDB instance.

## Start DynamoDB
```
java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -inMemory
```

## Create DynamoDB table
```
aws dynamodb create-table --endpoint-url http://localhost:8000 --table-name songs --key-schema AttributeName=id,KeyType=HASH --attribute-definitions AttributeName=id,AttributeType=S --provisioned-throughput ReadCapacityUnits=100,WriteCapacityUnits=100
```

## Start tomcat (with development profile)
```
mvn tomcat7:run -Dspring.profiles.active=dev
```

The application is available in the location: http://localhost:8080/

# Production Deployment
This section shows how you can deploy the application in the AWS cloud.

## Create an EC2 keypair
- Create the key from the AWS console and store the private key locally as `EC2_instance_key.pem`
- Restrict the permissions in the file
```
chmod 600 EC2_instance_key.pem
```

## Create IAM role (for EC2) to access DynamoDB
Execute the following command, after replacing the region & aws_account_id in the ec2_app_server_policy.json file
```
aws iam create-role --role-name EC2-AppServer-Role --assume-role-policy-document file://aws/ec2_trust.json
aws iam put-role-policy --role-name EC2-AppServer-Role --policy-name EC2-AppServer-Permissions --policy-document file://aws/ec2_app_server_policy.json
aws iam create-instance-profile --instance-profile-name EC2-AppServer-Instance-Profile
aws iam add-role-to-instance-profile --role-name EC2-AppServer-Role --instance-profile-name EC2-AppServer-Instance-Profile
```

## Create a security group & allow inbound connections for SSH/HTTP
Execute the following CLI commands:
```
aws ec2 create-security-group --description 'Security group for application servers' --group-name app-server-sg
aws ec2 authorize-security-group-ingress --group-id <security_group_id> --protocol tcp --port 22 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-id <security_group_id> --protocol tcp --port 8080 --cidr 0.0.0.0/0
```

## Create EC2 instance
- Using an AMI image that contains Java 8 + Maven
- Using the security group and the IAM role we previously created

## Create the DynamoDB table
```
aws dynamodb create-table --table-name songs --key-schema AttributeName=id,KeyType=HASH --attribute-definitions AttributeName=id,AttributeType=S --provisioned-throughput ReadCapacityUnits=100,WriteCapacityUnits=100
```

## Copy the package with the code to the server
```
scp -r -i EC2_instance_key.pem <folder_to_the_package_locally>  ec2-user@<ec2_domain_name>:/home/ec2-user
```

## Connect to the server with SSH
```
ssh -i EC2_instance_key.pem ec2-user@<ec2_domain_name>
```

## Deploy the application to the server (with production profile)
```
mvn tomcat7:run -Dspring.profiles.active=prod
```

The application is available in the location http://<ec2_domain_name>:8080/

# Continuous Integration

## Create buckets
Execute the following commands, having in mind that you might have to use different S3 bucket names, if they exist:
```
aws s3api create-bucket --bucket com.packtpub.catalogservice.source
aws s3api create-bucket --bucket com.packtpub.catalogservice.artifacts
```

## Create AWS Codebuild IAM policy & role
```
aws iam create-role --role-name CodeBuildServiceRole --assume-role-policy-document file://aws/code_build_role.json
aws iam put-role-policy --role-name CodeBuildServiceRole --policy-name CodeBuildServiceRolePolicy --policy-document file://aws/code_build_policy.json
```

## Create KMS key & keep track of ID/arn
```
aws kms create-key
aws kms get-key-policy --key-id <key_id> --policy-name default --output text > aws/kms_policy.json
```

## Add the following policies in it (replacing the account & region)
```
{
  "Sid": "Allow access through Amazon S3 for all principals in the account that are authorized to use Amazon S3",
  "Effect": "Allow",
  "Principal": {
    "AWS": "*"
  },
  "Action": [
    "kms:Encrypt",
    "kms:Decrypt",
    "kms:ReEncrypt*",
    "kms:GenerateDataKey*",
    "kms:DescribeKey"
  ],
  "Resource": "*",
  "Condition": {
    "StringEquals": {
      "kms:ViaService": "s3.<region>.amazonaws.com",
      "kms:CallerAccount": "<account-ID>"
    }
  }
},
{
  "Effect": "Allow", 
  "Principal": {
    "AWS": "arn:aws:iam::<account-ID>:role/CodeBuildServiceRole"
  },
  "Action": [
    "kms:Encrypt",
    "kms:Decrypt",
    "kms:ReEncrypt*",
    "kms:GenerateDataKey*",
    "kms:DescribeKey"
  ],
  "Resource": "*"
}
```

## Put the updated policy to the KMS key
```
aws kms put-key-policy --key-id <kms_key_id> --policy-name default --policy file://aws/kms_policy.json
```

## Create the skeleton input for the build project
```
aws codebuild create-project --generate-cli-skeleton > aws/build_project.json
```

## Add the following content in the build_project.json file (replacing the kms key ARN)
```
{
    "name": "CatalogService_CodeBuild_project",
    "description": "",
    "source": {
        "type": "S3",
        "location": "com.packtpub.catalogservice.source/CatalogService.zip"
    },
    "artifacts": {
        "type": "S3",
        "location": "com.packtpub.catalogservice.artifacts"
    },
    "environment": {
        "type": "LINUX_CONTAINER",
        "image": "aws/codebuild/java:openjdk-8",
        "computeType": "BUILD_GENERAL1_SMALL"
    },
    "serviceRole": "CodeBuildServiceRole",
    "encryptionKey": "<kms_key_arn>"
}
```

## Create the CodeBuild project
```
aws codebuild create-project --cli-input-json file://aws/build_project.json
```

## Upload to the S3 bucket
```
zip -r target/CatalogService.zip .
aws s3 mv target/CatalogService.zip s3://com.packtpub.catalogservice.source/CatalogService.zip
```

## Run a build
```
aws codebuild start-build --project-name CatalogService_CodeBuild_project
```
