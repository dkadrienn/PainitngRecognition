import cv2
import numpy as np
from keras.src.callbacks import ReduceLROnPlateau, EarlyStopping
from keras.applications.resnet50 import ResNet50, preprocess_input
from keras.models import Model
from keras.layers import Dense, GlobalAveragePooling2D, BatchNormalization, Activation
from keras.optimizers import Adam
from keras.optimizers import SGD
from keras.utils import to_categorical
from keras.regularizers import l2
import time

from matplotlib import pyplot as plt

import init_data


def load_images_from_folder(data_flag, folder):
    print('Loading images...')
    image_paths, labels = init_data.get_data_path_w_label(folder)
    img_array = init_data.get_images(data_flag, image_paths)
    images = []
    for img in img_array:
        if img is not None:
            img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)  # Convert from BGR to RGB
            img = cv2.resize(img, (224, 224))  # Resize to (224, 224) as expected by ResNet50
            images.append(img)
    print('Image loading finished.')
    print(images.__len__())
    return np.array(images), np.array(labels)


class CustomResNet50:
    def __init__(self, num_classes, learning_rate=0.001):
        self.num_classes = num_classes
        self.learning_rate = learning_rate
        self.base_model = ResNet50(weights='imagenet', include_top=False)
        self.model = self._prepare_model()

    def _prepare_model(self):
        print('Preparing model...')
        x = self.base_model.output
        x = GlobalAveragePooling2D()(x)
        x = BatchNormalization()(x)
        x = Dense(1024, activation='relu', kernel_regularizer=l2(0.01))(x)  # Dense layer
        x = BatchNormalization()(x)
        predictions = Dense(self.num_classes, activation='softmax', kernel_regularizer=l2(0.01))(x)  # Output layer
        model = Model(inputs=self.base_model.input, outputs=predictions)

        for layer in self.base_model.layers:
            layer.trainable = False

        model.compile(optimizer=SGD(self.learning_rate), loss='categorical_crossentropy', metrics=['accuracy'])
        print('Model preparation finished.')
        return model

    def train(self, train_folder, val_folder, batch_size=32, epochs=50):
        train_images, train_labels = load_images_from_folder(1, train_folder)
        train_labels = to_categorical(train_labels, num_classes=self.num_classes)

        val_images, val_labels = load_images_from_folder(2, val_folder)
        val_labels = to_categorical(val_labels, num_classes=self.num_classes)

        train_images = preprocess_input(train_images)
        val_images = preprocess_input(val_images)

        lr_reduction = ReduceLROnPlateau(monitor='val_loss',
                                         patience=5,
                                         verbose=1,
                                         factor=0.5,
                                         min_lr=0.00001)

        early_stopping = EarlyStopping(
            monitor='val_loss',  # Monitor validation loss
            min_delta=0.01,      # Minimum change to qualify as an improvement
            patience=10,         # Stop after 10 epochs without improvement
            verbose=1,           # Print messages
            restore_best_weights=True  # Restore model weights from the epoch with the best validation loss
        )

        # Train model
        print(f'Training started at {time.perf_counter()}')
        history = self.model.fit(train_images, train_labels, batch_size=batch_size, epochs=epochs,
                                 validation_data=(val_images, val_labels), callbacks=[lr_reduction, early_stopping])
        print(f'Training finished at {time.perf_counter()}')

        # Summarize history for accuracy
        plt.figure(figsize=(8, 6))
        plt.plot(history.history['accuracy'], label='Train Accuracy')
        plt.plot(history.history['val_accuracy'], label='Validation Accuracy')
        plt.title('ResNet 50 - Model Accuracy')
        plt.ylabel('Accuracy')
        plt.xlabel('Epoch')
        plt.legend(loc='upper left')
        plt.show()

        # Summarize history for loss
        plt.figure(figsize=(8, 6))
        plt.plot(history.history['loss'], label='Train Loss')
        plt.plot(history.history['val_loss'], label='Validation Loss')
        plt.title('ResNet 50 - Model Loss')
        plt.ylabel('Loss')
        plt.xlabel('Epoch')
        plt.legend(loc='upper right')
        plt.show()

    def evaluate(self, test_folder, batch_size=32):
        print(f'Evaluating started at {time.perf_counter()}')
        test_images, test_labels = load_images_from_folder(3, test_folder)
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
