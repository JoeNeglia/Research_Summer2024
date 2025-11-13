import os
import re
import csv
import lizard
from collections import defaultdict

def calculate_metrics(java_code):
    total_lines = java_code.split('\n')
    code_lines = 0
    whitespace_lines = 0
    comment_lines = 0
    in_block_comment = False
    import_count = 0

    variable_name_lengths = []
    method_name_lengths = []
    method_lines = []
    current_method_lines = 0
    in_method = False

    imports = []
    classes_used = set()

    variable_pattern = re.compile(r'\b\w+\b\s+\w+\s*=\s*[^;]+;')
    method_pattern = re.compile(r'\b\w+\b\s+\w+\s*\([^)]*\)\s*\{')
    method_end_pattern = re.compile(r'\}')
    class_pattern = re.compile(r'\bnew\s+(\w+)|\b(\w+)\s*\(')

    for line in total_lines:
        stripped_line = line.strip()

        if stripped_line == '':
            whitespace_lines += 1
        elif stripped_line.startswith('//'):
            comment_lines += 1
        elif stripped_line.startswith('/*'):
            comment_lines += 1
            in_block_comment = True
        elif stripped_line.endswith('*/'):
            comment_lines += 1
            in_block_comment = False
        elif in_block_comment:
            comment_lines += 1
        elif stripped_line.startswith('import'):
            import_count += 1
            imports.append(stripped_line.split()[-1].rstrip(';'))  # Store the imported class or package
        else:
            code_lines += 1
            if in_method:
                current_method_lines += 1

            # Variable name length
            variable_match = variable_pattern.search(line)
            if variable_match:
                variable_name = variable_match.group().split()[1]
                variable_name_lengths.append(len(variable_name))

            # Method name length
            method_match = method_pattern.search(line)
            if method_match:
                method_name = method_match.group().split()[1]
                method_name_lengths.append(len(method_name))
                in_method = True
                current_method_lines = 1  # Start counting lines for the new method

            if in_method and method_end_pattern.search(line):
                method_lines.append(current_method_lines)
                in_method = False

            # Track class usage
            class_matches = class_pattern.findall(line)
            for match in class_matches:
                class_name = match[0] if match[0] else match[1]
                classes_used.add(class_name)

    unused_imports = calculate_unused_imports(imports, classes_used)

    total_count = len(total_lines)
    if total_count == 0:
        percent_code = percent_whitespace = percent_comments = percent_imports = 0
    else:
        percent_code = (code_lines / total_count) * 100
        percent_whitespace = (whitespace_lines / total_count) * 100
        percent_comments = (comment_lines / total_count) * 100
        percent_imports = (import_count / total_count) * 100

    average_variable_name_length = (sum(variable_name_lengths) / len(variable_name_lengths)) if variable_name_lengths else 0
    average_method_name_length = (sum(method_name_lengths) / len(method_name_lengths)) if method_name_lengths else 0
    average_lines_per_method = (sum(method_lines) / len(method_lines)) if method_lines else 0

    return (percent_code, percent_whitespace, percent_comments, percent_imports,
            average_variable_name_length, average_method_name_length, average_lines_per_method, import_count, unused_imports)

def calculate_unused_imports(imports, classes_used):
    unused_imports = 0
    for imp in imports:
        class_name = imp.split('.')[-1]
        if class_name not in classes_used:
            unused_imports += 1
    return unused_imports

def calculate_cyclomatic_complexity(java_code):
    analysis = lizard.analyze_file.analyze_source_code('file.java', java_code)
    complexities = [function.cyclomatic_complexity for function in analysis.function_list]
    total_complexity = sum(complexities)
    average_complexity = (total_complexity / len(complexities)) if complexities else 0
    return total_complexity, average_complexity

def process_java_files(input_folder, output_folder):
    if not os.path.exists(output_folder):
        os.makedirs(output_folder)

    combined_data = []

    for filename in os.listdir(input_folder):
        if filename.endswith(".java"):
            file_path = os.path.join(input_folder, filename)
            with open(file_path, 'r', encoding='utf-8') as file:
                java_code = file.read()
            
            metrics = calculate_metrics(java_code)
            total_complexity, average_complexity = calculate_cyclomatic_complexity(java_code)
            
            combined_data.append([filename, *metrics, total_complexity, average_complexity, 0])

    combined_output_file = os.path.join(output_folder, 'HumanFinal.csv')
    with open(combined_output_file, 'w', newline='') as csvfile:
        csvwriter = csv.writer(csvfile)
        csvwriter.writerow(['Filename', 'Code (%)', 'Whitespace (%)', 'Comments (%)', 'Imports (%)', 
                            'Avg Variable Name Length', 'Avg Method Name Length', 'Avg Lines per Method',
                            'Number of Imports', 'Unused Imports', 'Total Cyclomatic Complexity', 'Avg Cyclomatic Complexity', 'Human/AI Generated'])
        csvwriter.writerows(combined_data)

input_folder = 'input/folder'
output_folder = 'output/folder'

process_java_files(input_folder, output_folder)
