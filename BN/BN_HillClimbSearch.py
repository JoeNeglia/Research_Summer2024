import pandas as pd
from pgmpy.estimators import HillClimbSearch, BDeuScore
from pgmpy.models import BayesianNetwork
from pgmpy.estimators import MaximumLikelihoodEstimator
from pgmpy.inference import VariableElimination

# Load your dataset (replace with your file path)
df = pd.read_csv('/Users/joeneglia/Desktop/Research_Summer2024/Data/CombinedFeatures/combined_metrics_one_hot_encoded.csv')

# Ensure the dataset is integer-based (for discrete values)
df = df.astype(int)

# Use the BDeuScore for scoring the network structures
scoring_method = BDeuScore(df)

# Perform structure learning using Hill Climb Search
hc = HillClimbSearch(df)
best_model = hc.estimate(scoring_method=scoring_method)

# Print the learned structure
print("Learned Structure:")
print(best_model.edges())

# Fit the best learned model to the data using Maximum Likelihood Estimator
model = BayesianNetwork(best_model.edges())
model.fit(df, estimator=MaximumLikelihoodEstimator)

# Print the learned CPDs
for cpd in model.get_cpds():
    print(f"CPD of {cpd.variable}:")
    print(cpd)

# Initialize inference on the model
inference = VariableElimination(model)

# Example prediction: Predict Human/AI Generated given evidence from other features
# You can modify the evidence dictionary with the actual features you want to predict based on
query_result = inference.query(variables=['Human/AI Generated'], evidence={'Code (%)_Low': 1, 'Whitespace (%)_Low': 1})

# Output the prediction results
print("\nPrediction for Human/AI Generated based on given evidence:")
print(query_result)
