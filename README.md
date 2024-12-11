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

2. **Add GitHub Token**:
   - Create a GitHub Personal Access Token (PAT) by allowing necessary accesses.
   - Store your token in a `config.json` file in the `src/main/resources` directory:

   Example `config.json`:
    ```json
    {
      "githubToken": "github_token",
      "githubUsername": "github_username"
    }
    ```