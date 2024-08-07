import openai
import os

# Set your OpenAI API key
openai.api_key = 'API-KEY'

# Directory where the generated Java files will be saved
output_dir = '/Users/joeneglia/Desktop/Research_Summer2024/Data/AIJavaFiles'

# Make sure the output directory exists
if not os.path.exists(output_dir):
    os.makedirs(output_dir)

# Function to get a random Android Java project prompt
def get_project_prompt():
    response = openai.ChatCompletion.create(
        model="gpt-4o",
        messages=[
            {"role": "user", "content": "Write a random Android Java project prompt that you haven't already:"}
        ]
    )
    return response.choices[0].message['content'].strip()

# Function to get the complete code and comments for the project
def get_project_code(prompt):
    response = openai.ChatCompletion.create(
        model="gpt-4o",
        messages=[
            {"role": "user", "content": f"Write the complete code and comments for this project. All of the classes and implementation must be written in a single file. Ensure the implementation is comprehensive and not simplified.\n\nProject prompt: {prompt}"}
        ]
    )
    return response.choices[0].message['content'].strip()

# Generate and save 100 projects
for i in range(1):
    try:
        # Get a project prompt
        prompt = get_project_prompt()
        # Get the project code based on the prompt
        code = get_project_code(prompt)
        # Determine the filename based on the prompt
        filename = os.path.join(output_dir, f"Project_{i+52}.java")
        # Save the code to a file
        with open(filename, 'w') as file:
            file.write(code)
        print(f"Saved project {i+52} to {filename}")
    except Exception as e:
        print(f"Failed to save project {i+52}: {e}")

print("Completed generating and saving projects.")
