import os
import pandas as pd

def combine_csv_files(file1, file2, output_dir, output_filename):
    # Read the CSV files into DataFrames
    df1 = pd.read_csv(file1)
    df2 = pd.read_csv(file2)
    
    # Concatenate the DataFrames
    combined_df = pd.concat([df1, df2], ignore_index=True)
    
    # Ensure the output directory exists
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    
    # Full path for the output file
    output_file = os.path.join(output_dir, output_filename)
    
    # Write the combined DataFrame to a new CSV file
    combined_df.to_csv(output_file, index=False)
    return output_file

if __name__ == "__main__":
    file1 = "/Users/joeneglia/Desktop/Research_Summer2024/Data/HumanFeatures/HumanFinal.csv"
    file2 = "/Users/joeneglia/Desktop/Research_Summer2024/Data/AIFeatures/AIFinal.csv"
    output_dir = "/Users/joeneglia/Desktop/Research_Summer2024/Data/CombinedFeatures"
    output_filename = "FinalCombinedFeatures.csv"

    output_file = combine_csv_files(file1, file2, output_dir, output_filename)
    print(f"Combined CSV file saved to {output_file}")
