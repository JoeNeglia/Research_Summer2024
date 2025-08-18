import pandas as pd
from pgmpy.estimators import HillClimbSearch, BicScore

# Load the dataset
data = pd.read_csv('/Users/joeneglia/Desktop/Research_Summer2024/Data/CombinedFeatures/combined_metrics_one_hot_encoded.csv')

# Initialize HillClimbSearch with the dataset
hc = HillClimbSearch(data)

# Use BIC scoring to find the best network structure
bic = BicScore(data)

# Find the best model structure using hill-climbing search
best_model = hc.estimate(scoring_method=bic)

# Output the best structure (edges in the network)
print(best_model.edges())
