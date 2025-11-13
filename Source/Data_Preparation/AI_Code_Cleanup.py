import os
import re

def extract_code_from_string(content):
    pattern = re.compile(r"```(?:java|kotlin)\n(.*?)```", re.DOTALL)
    match = pattern.search(content)
    if match:
        return match.group(1).strip()
    return None

def process_files(input_dir, output_dir):
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    files_processed = 0

    for filename in os.listdir(input_dir):
        if filename.endswith(".java") or filename.endswith(".kt"):
            input_path = os.path.join(input_dir, filename)
            output_path = os.path.join(output_dir, filename)

            with open(input_path, 'r') as file:
                content = file.read()
            
            code = extract_code_from_string(content)
            if code:
                with open(output_path, 'w') as file:
                    file.write(code)
                print(f"Processed {filename} and saved cleaned code to {output_path}")
            else:
                print(f"No Java or Kotlin code block found in {filename}")
            files_processed += 1

    print(f"Total files processed: {files_processed}")

if __name__ == "__main__":
    input_directory = "input/directory"
    output_directory = "output/directory"

    process_files(input_directory, output_directory)
