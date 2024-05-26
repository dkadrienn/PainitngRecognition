import pandas as pd

# Load the CSV file
df = pd.read_csv('data/wikiart_csv/style_val_test_filtered.csv')
# df = pd.read_csv('data/wikiart_csv/style_train_filtered.csv')

# for train
# nr = 5653
# nr = 1136
# for val
# nr = 2420
nr = 4840

# Check if the number of rows is greater than 5653
if len(df) > nr:
    # Randomly sample nr rows from the DataFrame
    df = df.sample(n=nr, random_state=42)  # `random_state` for reproducibility

# Save the reduced DataFrame to a new CSV file
df.to_csv('data/wikiart_csv/style_val_test_reduced.csv', index=False)
# df.to_csv('data/wikiart_csv/style_train_reduced.csv', index=False)
