import cv2
import numpy as np
import pandas
from matplotlib import pyplot as plt
import resnet50


# IDENTIFY CLASSES
def get_classes(folder):
    style_classes = {}
    with open(folder) as f:
        for line in f:
            (key, value) = line.split()
            style_classes[int(key)] = value
    return style_classes


def get_data_path_w_label(folder):
    dataset = pandas.read_csv(folder, names=['Path', 'Class'])
    im_paths = dataset['Path'].tolist()
    im_labels = dataset['Class'].tolist()
    return im_paths, im_labels


# READ IMAGES INTO ARRAY
def get_images(paths):
    im_data = [cv2.imread('data/wikiart/wikiart/' + path) for path in paths]
    return im_data


# READ DATA W HEADER
style_train = pandas.read_csv('data/wikiart_csv/style_train_reduced.csv', names=['Path', 'Class'])
style_test = pandas.read_csv('data/wikiart_csv/style_test_reduced.csv', names=['Path', 'Class'])
style_val = pandas.read_csv('data/wikiart_csv/style_val_reduced.csv', names=['Path', 'Class'])

# GET PATH AND LABEL
style_train_im_path = style_train['Path'].tolist()
style_train_label = style_train['Class'].tolist()

style_test_im_path = style_test['Path'].tolist()
style_test_label = style_test['Class'].tolist()

style_val_im_path = style_val['Path'].tolist()
style_val_label = style_val['Class'].tolist()

# style_train_im = get_images(style_train_im_path)

# for i in range(16):
#     plt.subplot(4, 4, i+1)
#     plt.xticks([])
#     plt.yticks([])
#     plt.imshow(style_train_im[i], cmap=plt.cm.binary)
#     plt.xlabel(style_classes[style_train_label[i][0]])
#
# plt.show()
