import random

# Split val data into test and val 15-15% of the whole dataset
with open("data/wikiart_csv/style_val_test_reduced.csv") as fr:
    with open("data/wikiart_csv/style_test_reduced.csv", "w") as f1, open("data/wikiart_csv/style_val_reduced.csv", "w") as f2:
        for line in fr:
            f = random.choice([f1, f2])
            f.write(line)
