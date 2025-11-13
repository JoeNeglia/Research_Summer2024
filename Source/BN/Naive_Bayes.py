import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.naive_bayes import GaussianNB
from sklearn.metrics import classification_report, accuracy_score, confusion_matrix
from sklearn.inspection import permutation_importance
import matplotlib.pyplot as plt
import numpy as np

# Load the dataset
file_path = 'dataset/folder'
data = pd.read_csv(file_path)

# Prepare the dataset
X = data.drop(columns=["Filename", "Human/AI Generated"])  # Features
y = data["Human/AI Generated"]  # Target variable

# Drop the weaker features
weak_features = ['Avg Lines per Method', 'Imports (%)', 'Unused Imports', 'Avg Method Name Length']
#X = X.drop(columns=weak_features)

# Split the data into training (80%) and testing (20%) sets
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=2)

# Create and train a Naive Bayes classifier
nb_classifier = GaussianNB()
nb_classifier.fit(X_train, y_train)

# Make predictions on the test set
y_pred = nb_classifier.predict(X_test)

# Evaluate the model
accuracy = accuracy_score(y_test, y_pred)
report = classification_report(y_test, y_pred)
conf_matrix = confusion_matrix(y_test, y_pred)

# Output the results
print(f'Accuracy: {accuracy}')
print(f'Classification Report:\n{report}')
print(f'Confusion Matrix:\n{conf_matrix}')

# Plot the confusion matrix with matplotlib
plt.figure(figsize=(8, 6))
plt.imshow(conf_matrix, interpolation='nearest', cmap='Blues')
plt.title('Confusion Matrix')
plt.colorbar()
tick_marks = np.arange(len(set(y)))
plt.xticks(tick_marks, ['AI Generated', 'Human Generated'], rotation=45)
plt.yticks(tick_marks, ['AI Generated', 'Human Generated'])
plt.xlabel('Predicted Labels')
plt.ylabel('True Labels')

# Add text annotations for each cell in the confusion matrix
for i in range(conf_matrix.shape[0]):
    for j in range(conf_matrix.shape[1]):
        plt.text(j, i, conf_matrix[i, j], horizontalalignment="center", color="black")

plt.show()

# Permutation Importance Functionality
def plot_permutation_importance(model, X_test, y_test, feature_names, n_repeats=30, random_state=0):
    """
    Calculate and plot permutation importance for a given model.

    Parameters:
    - model: Trained machine learning model
    - X_test: Feature test set
    - y_test: Target test set
    - feature_names: Names of features
    - n_repeats: Number of permutation repeats
    - random_state: Random state for reproducibility
    """
    # Calculate permutation importance
    perm_importance = permutation_importance(
        model, X_test, y_test, n_repeats=n_repeats, random_state=random_state
    )
    
    # Extract importance values
    importance_values = perm_importance.importances_mean
    
    # Plot feature importances
    plt.figure(figsize=(10, 6))
    plt.barh(feature_names, importance_values)
    plt.xlabel('Permutation Importance')
    plt.title(f'Feature Importances for {type(model).__name__}')
    plt.show()

# Call the function for the Naive Bayes model
feature_names = X.columns
plot_permutation_importance(nb_classifier, X_test, y_test, feature_names)
