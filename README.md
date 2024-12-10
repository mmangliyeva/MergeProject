# Merge Request Assistant
The program will:
- Create a new branch in the selected repository.
- Create a file (`Hello.txt`) with the specified content.
- Open a pull request to merge the branch into the main branch.

## Installation

1. **Clone the Repository**:
    ```bash
    git clone https://github.com/mmangliyeva/MergeProject.git
    ```

2. **Install Dependencies**:
   - Make sure you have Kotlin installed.
   - 

3. **Add GitHub Token**:
   - Create a GitHub Personal Access Token (PAT) by allowing necessary accesses.
   - Store your token in a `config.json` file in the `src/main/resources` directory:

   Example `config.json`:
    ```json
    {
      "githubToken": "github_token",
      "githubUsername": "github_username"
    }
    ```

## Usage

1. **Run the Program**:
   After setting up the config file, run the `Main.kt` file. The program will:
   - Load configuration.
   - Fetch repositories from your GitHub account.
   - Allow you to select a repository to create a branch, add a file, and open a pull request.



2. **Input**:
   - **Branch Name**: Enter the name of the branch you want to create.
   - **File Name**: Enter the name of the file (e.g. `Hello.txt`) to be created.
   - **File Content**: Enter the content that should be added to the file (e.g. `Hello World`).