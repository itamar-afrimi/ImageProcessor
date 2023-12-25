import os
from typing import Dict, Any

import boto3
import os
import tensorflow as tf
import IPython.display as display

import matplotlib.pyplot as plt
import matplotlib as mpl
import tensorflow_hub as hub

mpl.rcParams['figure.figsize'] = (12, 12)
mpl.rcParams['axes.grid'] = False
import lambdas.helper_functions as help
import numpy as np
from PIL import Image
import time
import functools
from models.processed_image import ProcessedImage
from lambdas.helper_functions import get_job_id_of_image_in_bucket, update_image_status_in_db, get_filtered_labels
BLACK = "black and white"
STYLE = "style image"
PROCESSED_IMAGES_BUCKET_NAME = os.environ["PROCESSED_IMAGES_BUCKET_NAME"]

s3 = boto3.client('s3')
def style_image(image_url):
    style_path = tf.keras.utils.get_file('kandinsky5.jpg',
                                         'https://storage.googleapis.com/download.tensorflow.org/example_images/Vassily_Kandinsky%2C_1913_-_Composition_7.jpg')
    hub_model = hub.load('https://tfhub.dev/google/magenta/arbitrary-image-stylization-v1-256/2')
    local_path = tf.keras.utils.get_file("image.jpg", origin=image_url)

    content_image = help.load_img(local_path)
    style_image = help.load_img(style_path)
    stylized_image = hub_model(tf.constant(content_image), tf.constant(style_image))[0]
    image = help.tensor_to_image(stylized_image)
    return image


def black_and_white(image_url):
    local_path = tf.keras.utils.get_file("image.jpg", origin=image_url)

    image_file =  Image.open(local_path)
    image_file = image_file.convert('1')
    return image_file


def handler(event: Dict[str, Any], _: Any):
    bucket_name = event['Records'][0]['s3']['bucket']['name']
    file_name = event['Records'][0]['s3']['object']['key']
    job_id = get_job_id_of_image_in_bucket(bucket_name, file_name)
    update_image_status_in_db(key=job_id, new_status=ProcessedImage.PROCESSING)
    try:
        response = s3.get_object(Bucket=bucket_name, Key=file_name)
        image_url = f"https://{bucket_name}.s3.amazonaws.com/{file_name}"

    except Exception as e:
        print(e)
        raise e

    labels = get_filtered_labels(bucket_name, file_name)
    print(labels)
