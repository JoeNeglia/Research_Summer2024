import zipfile
import os

def extract_java_files(zip_file_path, output_dir):
    # Ensure the output directory exists
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    with zipfile.ZipFile(zip_file_path, 'r') as zip_ref:
        # Extract all contents of the zip file into a temporary directory
        temp_dir = os.path.join(output_dir, "temp")
        zip_ref.extractall(temp_dir)

        # Find all .java files
        java_files = []
        for root, dirs, files in os.walk(temp_dir):
            for file in files:
                if file.endswith(".java"):
                    java_files.append(os.path.join(root, file))

        # Read the content of each Java file and write it to a combined file
        project_name = os.path.splitext(os.path.basename(zip_file_path))[0]
        combined_file_path = os.path.join(output_dir, f"{project_name}_combined.java")
        with open(combined_file_path, 'w') as combined_file:
            for java_file in java_files:
                with open(java_file, 'r') as jf:
                    combined_file.write(jf.read())
                    combined_file.write("\n\n")  # Add a new line between files

        # Clean up the temporary directory
        for root, dirs, files in os.walk(temp_dir, topdown=False):
            for file in files:
                os.remove(os.path.join(root, file))
            for dir in dirs:
                os.rmdir(os.path.join(root, dir))
        os.rmdir(temp_dir)

def process_zip_files(zip_files_dir, output_dir):
    for root, dirs, files in os.walk(zip_files_dir):
        for file in files:
            if file.endswith(".zip"):
                zip_file_path = os.path.join(root, file)
                extract_java_files(zip_file_path, output_dir)

# Example usage
zip_files_dir = '/Users/joeneglia/Desktop/Data/HumanProjects'  # Directory containing your zip files
output_dir = '/Users/joeneglia/Desktop/Data/HumanJavaFiles'  # Directory where you want to save the combined java files

process_zip_files(zip_files_dir, output_dir)
