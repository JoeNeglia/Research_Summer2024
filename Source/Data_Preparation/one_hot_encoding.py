import pandas as pd
from sklearn.preprocessing import KBinsDiscretizer

# Load your dataset (update the path to your actual dataset file)
input_csv_path = 'input/file'
output_csv_path = 'output/file'

# Load the dataset into a pandas DataFrame
df = pd.read_csv(input_csv_path)
print("Dataset loaded successfully.")

# Drop the first column if necessary (e.g., an index column that is not needed)
df = df.drop(df.columns[0], axis=1)

# Separate the 'Human/AI Generated' column so it is left untouched
human_ai_column = 'Human/AI Generated'
target_df = df[[human_ai_column]]

# Remove 'Human/AI Generated' from the data that will be processed
df = df.drop(columns=[human_ai_column])

# Separate numerical columns for binning
numerical_columns = df.select_dtypes(exclude=['object', 'category']).columns

print(f"Numerical columns: {numerical_columns}")

# Discretize numerical columns into three bins: Low, Medium, and High
# KBinsDiscretizer will create ordinal categories (0, 1, 2) corresponding to the bins
discretizer = KBinsDiscretizer(n_bins=3, encode='ordinal', strategy='uniform')
df[numerical_columns] = discretizer.fit_transform(df[numerical_columns])

# Now create Low, Medium, and High categorical columns for each numerical feature
# We will create separate binary columns for each bin level (Low, Medium, High)
for column in numerical_columns:
    df[f'{column}_Low'] = (df[column] == 0).astype(int)
    df[f'{column}_Medium'] = (df[column] == 1).astype(int)
    df[f'{column}_High'] = (df[column] == 2).astype(int)

# Drop the original numerical columns since we now have separate binary columns
df = df.drop(columns=numerical_columns)

# Combine the transformed data with the 'Human/AI Generated' column again
final_df = pd.concat([df.reset_index(drop=True), target_df.reset_index(drop=True)], axis=1)

# Save the transformed data to a new CSV file
final_df.to_csv(output_csv_path, index=False)
print(f"One-hot encoded data with Low, Medium, High bins has been saved to {output_csv_path}")
