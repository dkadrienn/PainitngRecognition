import cv2
import pandas
import gc
import os

from matplotlib import pyplot as plt


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
def get_images(data_flag, paths):
    base_path = 'data/output/'
    save_folder = base_path + 'train_images_transformed/'
    if data_flag == 2:
        save_folder = base_path + 'val_images_transformed/'
    if data_flag == 3:
        save_folder = base_path + 'test_images_transformed/'

    if not os.path.exists(save_folder):
        os.makedirs(save_folder)

    processed_images = []  # To store processed images

    for path in paths:
        # Load one image at a time
        image = cv2.imread('data/wikiart/wikiart/' + path)
        if image is None:
            continue  # Skip if the image wasn't loaded properly

        # Resize the image
        scale_percent = 50  # percentage of original size
        width = int(image.shape[1] * scale_percent / 100)
        height = int(image.shape[0] * scale_percent / 100)
        dim = (width, height)
        resized = cv2.resize(image, dim, interpolation=cv2.INTER_AREA)

        # Process the resized image
        processed_image = cv2.GaussianBlur(resized, (5, 5), 0)

        # save the processed image into files based on thw dataset type (1 - train, 2 - val, 3 - test)
        cv2.imwrite(save_folder + path.replace('/', '_'), processed_image)
        cv2.waitKey(0)

        processed_images.append(processed_image)

        # Free up memory
        del image, resized, processed_image
        gc.collect()

    for i in range(16):
        plt.subplot(4, 4, i+1)
        plt.xticks([])
        plt.yticks([])
        plt.imshow(processed_images[i], cmap=plt.cm.binary)
        plt.xlabel(get_classes('data/wikiart_csv/style_class.txt')[style_train_label[i][0]])

    plt.show()

    return processed_images


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
