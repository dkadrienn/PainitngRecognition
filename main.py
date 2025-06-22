import data_prep
import init_data
import resnet50

style_classes_csv = 'data/wikiart_csv/style_class.txt'

train_csv = 'data/wikiart_csv/style_train.csv'
val_test_csv = 'data/wikiart_csv/style_val_test.csv'

train_filtered_csv = 'data/wikiart_csv/style_train_filtered.csv'
val_test_filtered_csv = 'data/wikiart_csv/style_val_test_filtered.csv'

train_reduced_csv = 'data/wikiart_csv/style_train_reduced.csv'
val_test_reduced_csv = 'data/wikiart_csv/style_val_test_reduced.csv'

val_reduced_csv = 'data/wikiart_csv/style_val_reduced.csv'
test_reduced_csv = 'data/wikiart_csv/style_test_reduced.csv'


def prepare_data():
    # filter data
    data_prep.filter_data(train_csv)
    data_prep.filter_data(val_test_csv)

    # reduce data
    # 1/4 of the whole dataset
    data_prep.reduce_data(train_filtered_csv, 14135)
    data_prep.reduce_data(val_test_filtered_csv, 6050)

    # split val dataset into val and test
    data_prep.split_data(val_test_reduced_csv, val_reduced_csv, test_reduced_csv)


def main():
    # filter non acii characters
    # reduce datasets sizes
    # split validation data into validation and testing datasets
    # prepare_data()

    style_classes = init_data.get_classes(style_classes_csv)

    model = resnet50.CustomResNet50(style_classes.__len__())
    model.train(train_reduced_csv, val_reduced_csv)
    loss, accuracy = model.evaluate(test_reduced_csv)
    print(f'Test accuracy: {accuracy}, test loss: {loss}')


if __name__ == "__main__":
    main()
