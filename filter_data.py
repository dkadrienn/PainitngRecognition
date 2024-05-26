import pandas as pd


def contains_non_ascii(s):
    return any(ord(char) > 127 for char in s)


# Load the CSV file
df = pd.read_csv('data/wikiart_csv/style_val_test.csv')


# Function to check if a cell contains non-ASCII characters
def contains_non_ascii(cell):
    if isinstance(cell, str):
        return not cell.isascii()
    return False


# Apply the filter to each row: keep rows where no cell contains non-ASCII characters
filtered_df = df[~df.map(contains_non_ascii).any(axis=1)]

# Save the filtered DataFrame to a new CSV file
filtered_df.to_csv('data/wikiart_csv/style_val_test_filtered.csv', index=False)
