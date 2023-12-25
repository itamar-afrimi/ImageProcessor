import os
import uuid
import json
from typing import Dict, Any
import re
from http import HTTPStatus

import boto3

# from helper_functions import generate_url,format_json_response,validate_request,validate_file_name
from models.processed_image import ProcessedImage
JSON_RESPONSE_HEADER = {"Content-type": "application/json"}
INVALID_CHARACTERS_REGEX = re.compile(r'[\\/*?:"<>|]')
DEFAULT_EXPECTED_PATH = "/"

IMAGES_BUCKET_NAME = os.environ["IMAGES_BUCKET_NAME"]
EXPECTED_API_METHOD = "POST"
S3_CLIENT_METHOD = "put_object"
ACCESS_KEY = os.environ["ACCESS_KEY"]
SECRET_KEY = os.environ["SECRET_KEY"]
def format_json_response(status_code: int, body: Dict[str, Any]):
    return {
        "statusCode": status_code,
        "headers": JSON_RESPONSE_HEADER,
        "body": json.dumps(body)
    }


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

s3_client = boto3.client('s3')
def generate_url(client_method, bucket_name, key, job_id):

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

def handler(event: Dict[str, Any], _: Any) -> Dict[str, Any]:
    # Validate request
    try:
        validate_request(event, EXPECTED_API_METHOD)
        body = json.loads(event["body"])
        file_name = body["file_name"]
        validate_file_name(file_name)
    except (ValueError, TypeError, KeyError, AttributeError):
        return format_json_response(status_code=HTTPStatus.BAD_REQUEST, body={"description": "Invalid request body"})

    # Generate job ID and pre-signed URL
    job_id = str(uuid.uuid4())
    url = generate_url(S3_CLIENT_METHOD, IMAGES_BUCKET_NAME, file_name, job_id)
    # Store job ID in DB
    ProcessedImage(image_job_id=job_id, status=ProcessedImage.CREATED).save()

    return format_json_response(status_code=HTTPStatus.CREATED, body={"url": url, "jobId": job_id})
