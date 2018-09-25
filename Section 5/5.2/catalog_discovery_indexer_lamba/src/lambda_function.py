import json
import boto3

def lambda_handler(event, context):
    cloudsearch_client = boto3.client('cloudsearchdomain', endpoint_url="http://<cloudsearch_domain_document_endpoint>")
    print('Initialised cloudsearch client')

    for record in event['Records']:
        message_payload = json.loads(record["body"])
        print('Consuming message: ' + json.dumps(message_payload))
        song_id = message_payload["id"]
        document = {
            "type": "add",
            "id": song_id,
            "fields": message_payload
        }
        doclist = json.dumps([document])
        cloudsearch_result = cloudsearch_client.upload_documents(documents=doclist, contentType="application/json")
        print('Cloudsearch result: ' + json.dumps(cloudsearch_result))
