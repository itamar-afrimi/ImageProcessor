import boto3
import json
import re
from typing import Dict, Any
from models.processed_image import ProcessedImage
from collections import defaultdict
# import tensorflow as tf


INVALID_CHARACTERS_REGEX = re.compile(r'[\\/*?:"<>|]')
DEFAULT_EXPECTED_PATH = "/"
JSON_RESPONSE_HEADER = {"Content-type": "application/json"}


def format_json_response(status_code: int, body: Dict[str, Any]):
    return {
        "statusCode": status_code,
        "headers": JSON_RESPONSE_HEADER,
        "body": json.dumps(body)
    }


def update_image_status_in_db(key: str, new_status: str):
    image_db_row = ProcessedImage.get(key)
    image_db_row.status = new_status
    image_db_row.save()


def get_filtered_labels(bucket_name, file_name):
    rekognition_client = boto3.client('rekognition')
    labels = rekognition_client.detect_labels(
        Image={'S3Object': {'Bucket': bucket_name, 'Name': file_name}}, MinConfidence=90
    )['Labels']
    labels = list(filter(lambda label: 'Instances' in label and label['Instances'], labels))
    return labels


def generate_url(client_method, bucket_name, key, job_id, access_key, secret_key):
    s3_client = boto3.client('s3', access_key, secret_key)
    url = s3_client.generate_presigned_url(
        ClientMethod=client_method,
        Params={
            "Bucket": bucket_name,
            "Key": key,
            "Metadata": {"job-id": job_id},
            "ContentType": "image/jpeg",
        }
    )
    s3_client.close()
    return url


def get_job_id_of_image_in_bucket(bucket_name, key):
    s3_client = boto3.client('s3')
    job_id = s3_client.head_object(Bucket=bucket_name, Key=key)["Metadata"]["job-id"]
    s3_client.close()
    return job_id


# VALIDATORS
def validate_request(event: Dict[str, Any], expected_method: str, expected_path: str = DEFAULT_EXPECTED_PATH):
    # Check that the method is the one expected
    if event["requestContext"]["http"]["method"] != expected_method:
        raise ValueError
    # Check that the path is the one expected
    if event["requestContext"]["http"]["path"] != expected_path:
        raise ValueError


def validate_file_name(file_name: str):
    # Check that the file_name doesn't contain invalid characters
    if re.match(INVALID_CHARACTERS_REGEX, file_name):
        raise ValueError
