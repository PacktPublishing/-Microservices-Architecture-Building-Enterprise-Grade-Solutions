# Creating the artifact of the lambda function's code
```
zip -j catalog_service_indexer_lambda.zip src/lambda_function.py
```

# Creating the Lambda function
First, we need to create an IAM role for the Lambda function.
To achieve that, execute the following commands:
```
aws iam create-role --role-name EC2-CatalogDiscoveryLambda-Role --assume-role-policy-document file://aws/ec2_trust.json
aws iam put-role-policy --role-name EC2-CatalogDiscoveryLambda-Role --policy-name EC2-CatalogDiscoveryLambda-Permissions --policy-document file://aws/ec2_discovery_server_policy.json
```

Create the lambda function:
```
aws lambda create-function \
           --function-name catalog-discovery-index-function \
           --runtime python3.6 \
           --role arn:aws:iam::<AWS_account_id>:role/EC2-CatalogDiscoveryLambda-Role \
           --handler lambda_function.lambda_handler \
           --description 'A function that consumes pulished songs from the SQS queue and indexes them in Cloudsearch' \
           --zip-file fileb://catalog_service_indexer_lambda.zip
```

# Use the SQS queue as an event source
```
aws lambda create-event-source-mapping \
           --event-source-arn arn:aws:sqs:<region>:<AWS_account_id>:published_songs_queue \
           --function-name catalog-discovery-index-function \
           --batch-size 1
```

# Publish a message to SQS
You can publish the following sample message to SQS to trigger the lambda function:
```
{"id":"id_8","author_id":"author_id_5","release_date":1526070251333,"duration_in_seconds":150,"artifact_uri":"s3://bucket/song8.mp4"}
```