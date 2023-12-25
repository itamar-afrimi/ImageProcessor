import os

from pynamodb.attributes import UnicodeAttribute
from pynamodb.models import Model


class ProcessedImage(Model):
    """
    A representation of an image and its processing status.
    When the processing is completed, the ProcessedImage model should contain the S3 bucket and key
    for the processed image.
    """
    # Possible Status Values
    CREATED = 'created'
    PROCESSING = 'processing'
    SUCCESS = 'success'
    FAILURE = 'failure'

    class Meta:
        region = "us-west-2"
        table_name = "images"
    image_job_id = UnicodeAttribute(hash_key=True)
    status = UnicodeAttribute()
    processed_image_bucket = UnicodeAttribute(null=True)
    processed_image_key = UnicodeAttribute(null=True)
