
from http import HTTPStatus
from typing import Dict, Any
from models.processed_image import ProcessedImage
from lambdas.helper_functions import validate_request, generate_url, format_json_response


EXPECTED_API_METHOD = "GET"
S3_CLIENT_METHOD = "get_object"


def handler(event: Dict[str, Any], _: Any) -> Dict[str, Any]:
    try:
        validate_request(event, EXPECTED_API_METHOD)
        job_id = event["queryStringParameters"]["jobId"]
    except (ValueError, TypeError, KeyError, AttributeError):
        return format_json_response(status_code=HTTPStatus.BAD_REQUEST, body={"description": "Invalid request body"})

    # Fetch image row from DB
    try:
        image_db_row = ProcessedImage.get(job_id)
    except ProcessedImage.DoesNotExist:
        return format_json_response(status_code=HTTPStatus.NOT_FOUND, body={"description": "JobId doesn't exist"})

    # Return image status and url (if exists i.e processing has been done successfully)
    status = image_db_row.status
    url = None if status != ProcessedImage.SUCCESS else \
        generate_url(S3_CLIENT_METHOD, image_db_row.processed_image_bucket, image_db_row.processed_image_key, job_id)
    return format_json_response(status_code=HTTPStatus.OK, body={"url": url, "status": status})
