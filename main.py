# from tensorflow.keras import datasets, layers, models
import pandas

# (training_images, training_labels), (testing_images, testing_labels) = datasets.cifar10.load_data()
#
# # normalize data from 0-255 (pixel brightness) to 0-1
# training_images, testing_images = training_images / 255, testing_images / 255
#
# class_names = ['Plane', 'Car', 'Bird', 'Cat', 'Deer', 'Dog', 'Frog', 'Horse', 'Ship', 'Truck']
#
# for i in range(16):
#     plt.subplot(4, 4, i+1)
#     plt.xticks([])
#     plt.yticks([])
#     plt.imshow(training_images[i], cmap=plt.cm.binary)
#     plt.xlabel(class_names[training_labels[i][0]])
#
# plt.show()
#
# training_images = training_images[:20000]
# training_labels = training_labels[:20000]
# testing_images = testing_images[:4000]
# testing_labels = testing_labels[:4000]
#
# model = models.Sequential()
# model.add(layers.Conv2D(32, (3, 3), activation='relu', input_shape=(32, 32, 3)))
# model.add(layers.MaxPooling2D((2, 2)))
# model.add(layers.Conv2D(64, (3, 3), activation='relu'))
# model.add(layers.MaxPooling2D((2, 2)))
# model.add(layers.Conv2D(64, (3, 3), activation='relu'))
# model.add(layers.Flatten())
# model.add(layers.Dense(64, activation='relu'))
# model.add(layers.Dense(10, activation='softmax')) #softmax scales, how likely is the given result
#
# model.compile(optimizer='adam', loss='sparse_categorical_crossentropy', metrics=['accuracy'])

import init_data
import resnet50

style_classes = init_data.get_classes('data/wikiart_csv/style_class.txt')

model = resnet50.CustomResNet50(style_classes.__len__())
model.train('data/wikiart_csv/style_train_reduced.csv', 'data/wikiart_csv/style_val_reduced.csv')
loss, accuracy = model.evaluate('data/wikiart_csv/style_test_reduced.csv')
print(f'Test accuracy: {accuracy}, test loss: {loss}')



