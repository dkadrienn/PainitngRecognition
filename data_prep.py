import random
import pandas as pd


def contains_non_ascii(s):
    return any(ord(char) > 127 for char in s)


# check if a cell contains non-ASCII characters
def contains_non_ascii(cell):
    if isinstance(cell, str):
        return not cell.isascii()
    return False


def filter_data(file):
    df = pd.read_csv(file)
    # apply the filter to each row
    # keep rows where no cell contains non-ASCII characters
    filtered_df = df[~df.map(contains_non_ascii).any(axis=1)]

    # save the filtered DataFrame to a new CSV file
    filtered_file_name = file.replace('.csv', '_filtered.csv')
    filtered_df.to_csv(filtered_file_name, index=False)


def reduce_data(file, n):
    df = pd.read_csv(file)

    if len(df) > n:
        # randomly sample nr rows from the DataFrame
        df = df.sample(n=n, random_state=42)  # `random_state` for reproducibility

    # save the reduced DataFrame to a new CSV file
    reduced_file_name = file.replace('_filtered.csv', '_reduced.csv')
    df.to_csv(reduced_file_name, index=False)


def split_data(base_file, val_file, test_file):
    # split val data into test and val 15-15% of the whole dataset
    with open(base_file) as fr:
        with open(val_file, "w") as f1, open(test_file, "w") as f2:
            for line in fr:
                f = random.choice([f1, f2])
                f.write(line)
