import os
import re
import csv
import lizard

def calculate_metrics(java_code):
    total_lines = java_code.split('\n')
    code_lines = 0
    whitespace_lines = 0
    comment_lines = 0
    in_block_comment = False

    variable_name_lengths = []
    method_name_lengths = []
    method_lines = []
    current_method_lines = 0
    in_method = False

    variable_pattern = re.compile(r'\b\w+\b\s+\w+\s*=\s*[^;]+;')
    method_pattern = re.compile(r'\b\w+\b\s+\w+\s*\([^)]*\)\s*\{')
    method_end_pattern = re.compile(r'\}')
    method_start_pattern = re.compile(r'\{')

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

    total_count = len(total_lines)
    if total_count == 0:
        percent_code = percent_whitespace = percent_comments = 0
    else:
        percent_code = (code_lines / total_count) * 100
        percent_whitespace = (whitespace_lines / total_count) * 100
        percent_comments = (comment_lines / total_count) * 100

    average_variable_name_length = (sum(variable_name_lengths) / len(variable_name_lengths)) if variable_name_lengths else 0
    average_method_name_length = (sum(method_name_lengths) / len(method_name_lengths)) if method_name_lengths else 0
    average_lines_per_method = (sum(method_lines) / len(method_lines)) if method_lines else 0

    return (percent_code, percent_whitespace, percent_comments,
            average_variable_name_length, average_method_name_length, average_lines_per_method)

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
            
            combined_data.append([filename, *metrics, total_complexity, average_complexity, 1])

    combined_output_file = os.path.join(output_folder, 'combined_metrics.csv')
    with open(combined_output_file, 'w', newline='') as csvfile:
        csvwriter = csv.writer(csvfile)
        csvwriter.writerow(['Filename', 'Code (%)', 'Whitespace (%)', 'Comments (%)', 
                            'Avg Variable Name Length', 'Avg Method Name Length', 'Avg Lines per Method',
                            'Total Cyclomatic Complexity', 'Avg Cyclomatic Complexity', 'Human/AI Generated'])
        csvwriter.writerows(combined_data)

input_folder = '/Users/joeneglia/Desktop/Research_Summer2024/Data/CleanedAIJavaFiles'
output_folder = '/Users/joeneglia/Desktop/Research_Summer2024/Data/AIFeatures'

process_java_files(input_folder, output_folder)
