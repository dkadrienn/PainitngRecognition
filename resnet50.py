import time

import cv2
import numpy as np
from keras.applications.resnet50 import ResNet50, preprocess_input
from keras.layers import Dense, GlobalAveragePooling2D
from keras.layers import Dropout
from keras.models import Model
from keras.optimizers import Adam
from keras.src.callbacks import ReduceLROnPlateau
from keras.utils import to_categorical

import init_data


class CustomResNet50:
    def __init__(self, num_classes, learning_rate=0.0001):
        self.num_classes = num_classes
        self.learning_rate = learning_rate
        self.base_model = ResNet50(weights='imagenet', include_top=False)
        self.model = self._prepare_model()

    def _prepare_model(self):
        print('Preparing model...')
        x = self.base_model.output
        x = GlobalAveragePooling2D()(x)
        x = Dense(1024, activation='relu')(x)  # Dense layer
        x = Dropout(0.5)(x)
        predictions = Dense(self.num_classes, activation='softmax')(x)  # Output layer
        model = Model(inputs=self.base_model.input, outputs=predictions)

        for layer in self.base_model.layers:
            layer.trainable = False

        model.compile(optimizer=Adam(self.learning_rate), loss='categorical_crossentropy', metrics=['accuracy'])
        print('Model preparation finished.')
        return model

    def load_images_from_folder(self, folder):
        print('Loading images...')
        image_paths, labels = init_data.get_data_path_w_label(folder)
        img_array = init_data.get_images(image_paths)
        images = []
        for img in img_array:
            if img is not None:
                img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)  # Convert from BGR to RGB
                img = cv2.resize(img, (224, 224))  # Resize to (224, 224) as expected by ResNet50
                images.append(img)
        print('Image loading finished.')
        return np.array(images), np.array(labels)

    def train(self, train_folder, val_folder, batch_size=32, epochs=50):

        train_images, train_labels = self.load_images_from_folder(train_folder)
        train_labels = to_categorical(train_labels, num_classes=self.num_classes)

        val_images, val_labels = self.load_images_from_folder(val_folder)
        val_labels = to_categorical(val_labels, num_classes=self.num_classes)

        # train_images = preprocess_input(train_images)
        # val_images = preprocess_input(val_images)

        # Setup learning rate reduction


lr_reduction = ReduceLROnPlateau(monitor='val_loss',  # Monitor the validation loss
                                 factor=0.5,  # Reduce the learning rate by half
                                 patience=5,
                                 # Number of epochs with no improvement after which learning rate will be reduced
                                 verbose=1,  # Integrate verbosity level
                                 min_lr=0.00001)  # Minimum learning rate that the reduction can reach

# Train model
print(f'Training started at {time.perf_counter()}')
self.model.fit(train_images, train_labels, batch_size=batch_size, epochs=epochs,
               validation_data=(val_images, val_labels), callbacks=[lr_reduction])
print(f'Training finished at {time.perf_counter()}')


def evaluate(self, test_folder, batch_size=32):
    print(f'Evaluating started at {time.perf_counter()}')
    test_images, test_labels = self.load_images_from_folder(test_folder)
    test_labels = to_categorical(test_labels, num_classes=self.num_classes)
    test_images = preprocess_input(test_images)

    # Evaluate model
    test_loss, test_acc = self.model.evaluate(test_images, test_labels, batch_size=batch_size)
    print(f'Evaluating finished at {time.perf_counter()}')
    self.model.save('resnet50.h5')
    # self.model.save_weights('resnet50_weights.h5')
    json_string = self.model.to_json()
    # save JSON to a file (optional)
    with open('my_model_architecture.json', 'w') as f:
        f.write(json_string)
    return test_loss, test_acc
