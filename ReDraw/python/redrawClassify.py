import os
import sys
import numpy as np
import tensorflow as tf
from google.protobuf import text_format
from tensorflow.python.framework import graph_io
from tensorflow.python.tools.inspect_checkpoint import print_tensors_in_checkpoint_file
from tensorflow.python.platform import gfile


def read_tensor_from_image_file(file_name,
                                input_height=299,
                                input_width=299,
                                input_mean=0,
                                input_std=255):
  input_name = "file_reader"
  output_name = "normalized"
  file_reader = tf.read_file(file_name, input_name)
  if file_name.endswith(".png"):
    image_reader = tf.image.decode_png(
        file_reader, channels=3, name="png_reader")
  elif file_name.endswith(".gif"):
    image_reader = tf.squeeze(
        tf.image.decode_gif(file_reader, name="gif_reader"))
  elif file_name.endswith(".bmp"):
    image_reader = tf.image.decode_bmp(file_reader, name="bmp_reader")
  else:
    image_reader = tf.image.decode_jpeg(
        file_reader, channels=3, name="jpeg_reader")
  float_caster = tf.cast(image_reader, tf.float32)
  dims_expander = tf.expand_dims(float_caster, 0)
  resized = tf.image.resize_bilinear(dims_expander, [input_height, input_width])
  normalized = tf.divide(tf.subtract(resized, [input_mean]), [input_std])
  sess = tf.compat.v1.Session()
  result = sess.run(normalized)

  return result

np.set_printoptions(suppress=True)

tf.reset_default_graph()


Modelcheckpoint = sys.argv[2]

labels = []

proto_as_ascii_lines = tf.gfile.GFile(sys.argv[1]).readlines()
for l in proto_as_ascii_lines:
	labels.append(l.rstrip())

with tf.Session(graph = tf.Graph()) as sess:
	
	with gfile.FastGFile(Modelcheckpoint,'rb') as f:
		graph_def = tf.GraphDef()
		graph_def.ParseFromString(f.read())
		sess.graph.as_default()
		tf.import_graph_def(graph_def,name='')
		for fil in sys.argv[5:]:
			image_data = read_tensor_from_image_file(fil)  # tf.gfile.GFile(fil, 'rb').read()
		
			softmax_tensor = sess.graph.get_tensor_by_name(sys.argv[4])
			predictions = sess.run(softmax_tensor,{sys.argv[3]: image_data})
			
			topprediction = predictions[0].argsort()[-len(predictions[0]):][::-1]
			print(labels[topprediction[0]])
	
	
	