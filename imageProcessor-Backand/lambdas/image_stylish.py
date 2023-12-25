
import os
from typing import Dict, Any

import PIL
import boto3
import os
import tensorflow as tf
import IPython.display as display

import matplotlib.pyplot as plt
import matplotlib as mpl
import tensorflow_hub as hub

mpl.rcParams['figure.figsize'] = (12, 12)
mpl.rcParams['axes.grid'] = False
# import lambdas.helper_functions as help
import numpy as np
from PIL import Image
import time
import functools

BLACK = "black and white"
STYLE = "style image"
# PROCESSED_IMAGES_BUCKET_NAME = os.environ["PROCESSED_IMAGES_BUCKET_NAME"]

s3 = boto3.client('s3')
#event: Dict[str, Any], _: Any
def handler():
    """

    :param event:
    :param _:
    :return:
    """
    # bucket_name = event['Records'][0]['s3']['bucket']['name']
    # file_name = event['Records'][0]['s3']['object']['key']
    # job_id = get_job_id_of_image_in_bucket(bucket_name, file_name)
    # update_image_status_in_db(key=job_id, new_status=ProcessedImage.PROCESSING)
    # try:
    #     response = s3.get_object(Bucket=bucket_name, Key=file_name)
    #     image_url = f"https://{bucket_name}.s3.amazonaws.com/{file_name}"
    #
    # except Exception as e:
    #     print(e)
    #     raise e
    style_path = tf.keras.utils.get_file('kandinsky5.jpg',
                                         'https://storage.googleapis.com/download.tensorflow.org/example_images/Vassily_Kandinsky%2C_1913_-_Composition_7.jpg')
    hub_model = hub.load('https://tfhub.dev/google/magenta/arbitrary-image-stylization-v1-256/2')
    local_path = "/Users/itamarafrimi/Desktop/year 3/Semester A/Workshop im Modern application/Exercises/ex_6/ex_6_pair_17/images/car.jpeg"
    content_image = plt.imread(local_path)
    plt.imshow(content_image)
    # plt.show()
    image_path = load_img(local_path)
    style_image = load_img(style_path)
    stylized_image = hub_model(tf.constant(image_path), tf.constant(style_image))[0]
    image = tensor_to_image(stylized_image)
    plt.imshow(image)
    plt.show()
    return image

def load_img(path_to_img):
    max_dim = 512
    img = tf.io.read_file(path_to_img)
    img = tf.image.decode_image(img, channels=3)
    img = tf.image.convert_image_dtype(img, tf.float32)

    shape = tf.cast(tf.shape(img)[:-1], tf.float32)
    long_dim = max(shape)
    scale = max_dim / long_dim

    new_shape = tf.cast(shape * scale, tf.int32)

    img = tf.image.resize(img, new_shape)
    img = img[tf.newaxis, :]
    return img
def tensor_to_image(tensor):
  tensor = tensor*255
  tensor = np.array(tensor, dtype=np.uint8)
  if np.ndim(tensor)>3:
    assert tensor.shape[0] == 1
    tensor = tensor[0]
  return PIL.Image.fromarray(tensor)


if __name__ == '__main__':
    import site

    print(site.getsitepackages())
