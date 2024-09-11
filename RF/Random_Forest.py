import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report, confusion_matrix, accuracy_score
import matplotlib.pyplot as plt

# Load the dataset
data = pd.read_csv('/Users/joeneglia/Desktop/Research_Summer2024/Data/CombinedFeatures/FinalCombinedFeatures.csv')

# Drop the first column
data = data.drop(data.columns[0], axis=1)

# Separate features and target labels
X = data.drop('Human/AI Generated', axis=1)
y = data['Human/AI Generated']

# Drop the weaker features
weak_features = ['Total Cyclomatic Complexity', 'Imports (%)', 'Unused Imports']
X = X.drop(columns=weak_features)

# Split the data into training and testing sets (80% train, 20% test)
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=2)

# Standardize the features
scaler = StandardScaler()
X_train = scaler.fit_transform(X_train)
X_test = scaler.transform(X_test)

# Initialize the Random Forest model
rf_model = RandomForestClassifier(random_state=2, n_estimators=80)

# Train the model
rf_model.fit(X_train, y_train)

# Make predictions on the test set
y_pred = rf_model.predict(X_test)

# Evaluate the model
print("\nAccuracy:")
print(accuracy_score(y_test, y_pred))

print("Confusion Matrix:")
print(confusion_matrix(y_test, y_pred))

print("\nClassification Report:")
print(classification_report(y_test, y_pred))
