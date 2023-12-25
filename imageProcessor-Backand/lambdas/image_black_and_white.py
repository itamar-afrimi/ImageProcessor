from typing import Dict, Any

import boto3
import numpy as np
import matplotlib.pyplot as plt
from PIL import Image

s3_client = boto3.client('s3')
def handler(event: Dict[str, Any], _:Any):
    """

    :param event:
    :param _:
    :return:
    """
    images_bucket_name = event['Records'][0]['s3']['bucket']['name']
    file_name = event['Records'][0]['s3']['object']['key']

    response = s3_client.get_object(Bucket=images_bucket_name, Key=file_name)
    image = Image.open(response['body']).convert('L')
    s3_client.put_object(Bucket=images_bucket_name, Key='bw_'+file_name,Body=image.tobytes())
    s3_client.close()