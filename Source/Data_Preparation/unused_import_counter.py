import os
import subprocess
import re

# Path to the Checkstyle JAR file
checkstyle_jar = "jar/file"

# Path to the Google-style Checkstyle configuration file
checkstyle_config = "config/file"

# Path to the folder containing Java files
folder_path = "java/folder"

# Function to analyze a single Java file and count unused imports
def count_unused_imports(java_file):
    try:
        # Run Checkstyle on the Java file
        result = subprocess.run(
            ['java', '-jar', checkstyle_jar, '-c', checkstyle_config, java_file],
            stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True
        )
        
        # Check for the output that contains 'UnusedImports'
        unused_imports = re.findall(r'UnusedImports', result.stdout)
        
        # Return the number of unused imports
        return len(unused_imports)
    
    except Exception as e:
        print(f"Error analyzing {java_file}: {e}")
        return 0

# Traverse all Java files in the folder and count unused imports
def analyze_folder(folder_path):
    for root, dirs, files in os.walk(folder_path):
        for file in files:
            if file.endswith(".java"):
                java_file = os.path.join(root, file)
                print(f"Analyzing {java_file}...")
                
                # Count the number of unused imports in the file
                unused_count = count_unused_imports(java_file)
                
                print(f"Unused imports in {file}: {unused_count}")

# Run the analysis on the folder
analyze_folder(folder_path)
