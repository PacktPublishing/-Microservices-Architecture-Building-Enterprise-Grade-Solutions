# Catalog Discovery Indexer

## Create Cloudsearch domain
First, create the Cloudsearch domain and define the fields of the documents.
Note, you will also need to re-index for the fields become active.
You can achieve all that with the following commands:
```
aws cloudsearch create-domain --domain-name published-songs
aws cloudsearch   define-index-field --domain-name published-songs --name id --type text
aws cloudsearch   define-index-field --domain-name published-songs --name author_id --type text
aws cloudsearch   define-index-field --domain-name published-songs --name release_date --type int
aws cloudsearch   define-index-field --domain-name published-songs --name duration_in_seconds --type int
aws cloudsearch   define-index-field --domain-name published-songs --name artifact_uri --type text
aws cloudsearch index-documents --domain-name published-songs
```

## Create IAM role (for EC2) to access CloudSearch
Execute the following command, after replacing the region & aws_account_id in the ec2_app_server_policy.json file
```
aws iam create-role --role-name EC2-CatalogDiscoveryAppServer-Role --assume-role-policy-document file://aws/ec2_trust.json
aws iam put-role-policy --role-name EC2-CatalogDiscoveryAppServer-Role --policy-name EC2-CatalogDiscoveryAppServer-Permissions --policy-document file://aws/ec2_discovery_server_policy.json
aws iam create-instance-profile --instance-profile-name EC2-CatalogDiscoveryAppServer-Instance-Profile
aws iam add-role-to-instance-profile --role-name EC2-CatalogDiscoveryAppServer-Role --instance-profile-name EC2-CatalogDiscoveryAppServer-Instance-Profile
```

## Create EC2 auto-scaling group
This launch configuration will use the corresponding security group, IAM role, AMI and will have CodeDeploy agent installed
```
aws autoscaling create-launch-configuration --launch-configuration-name catalog-discovery-service-config --key-name EC2_instance_key --image-id ami-18e8ef72 --instance-type t2.small --iam-instance-profile EC2-CatalogDiscoveryAppServer-Instance-Profile --security-groups app-server-sg
```

## Create an auto-scaling group
```
aws autoscaling create-auto-scaling-group --auto-scaling-group-name catalog-discovery-service-scaling-group --launch-configuration-name catalog-discovery-service-config --min-size 0 --max-size 3 --availability-zones us-east-1a
```
Warning: In a real production environment, it's unsafe to set the min capacity to 0 instances from the availability point of view.

## Update the desired capacity of the auto-scaling group
```
aws autoscaling update-auto-scaling-group --auto-scaling-group-name catalog-discovery-service-scaling-group --desired-capacity 1
```

## Build the package & deploy the application
```
mvn clean package
#copy the package to the EC2 server & ssh to it
java -jar catalog-discovery-indexer-0.1.0.jar

```