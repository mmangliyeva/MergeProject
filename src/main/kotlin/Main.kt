import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*




data class Config(val githubToken: String, val githubUsername: String)

class Repository(val name: String, val fullName: String, private val config: Config) {
    companion object {


    }

    fun fetchDefaultBranchSha(): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/repos/$fullName"))
            .header("Authorization", "token ${config.githubToken}")
            .build()

        return try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            when (response.statusCode()) {
                200 -> {
                    val repoInfo: Map<String, Any> = gson.fromJson(response.body(), object : TypeToken<Map<String, Any>>() {}.type)
                    val defaultBranch = repoInfo["default_branch"] as String

                    val branchRequest = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.github.com/repos/$fullName/git/ref/heads/$defaultBranch"))
                        .header("Authorization", "token ${config.githubToken}")
                        .build()

                    val branchResponse = httpClient.send(branchRequest, HttpResponse.BodyHandlers.ofString())
                    when (branchResponse.statusCode()) {
                        200 -> {
                            val branchInfo: Map<String, Any> = gson.fromJson(branchResponse.body(), object : TypeToken<Map<String, Any>>() {}.type)
                            (branchInfo["object"] as Map<*, *>)["sha"] as String
                        }
                        else -> {
                            error("Failed to fetch branch SHA: ${branchResponse.body()}")
                        }
                    }
                }
                else -> {
                    error("Failed to fetch repository info: ${response.body()}")
                }
            }
         } catch (e: Exception) {
            error("Network error: ${e.message}")
        }
    }


    fun createBranch(branchName: String): Boolean {
        val defaultBranchSha = fetchDefaultBranchSha()
        val requestBody = """
            {
                "ref": "refs/heads/$branchName",
                "sha": "$defaultBranchSha"
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/repos/$fullName/git/refs"))
            .header("Authorization", "token ${config.githubToken}")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()

        return try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            when (response.statusCode()) {
                201 -> {
                    println("Branch '$branchName' created successfully")
                    true
                }
                422 -> {
                    println("Branch '$branchName' already exists.")
                    false
                }
                else -> {
                    println("Failed to create branch: ${response.body()}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Network error while creating branch: ${e.message}")
            false
        }
    }

    fun createFile(branchName: String, fileName: String, content: String): Boolean {
        val encodedContent = Base64.getEncoder().encodeToString(content.toByteArray())
        val requestBody = """
            {
                "message": "Add $fileName",
                "content": "$encodedContent",
                "branch": "$branchName"
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/repos/$fullName/contents/$fileName"))
            .header("Authorization", "token ${config.githubToken}")
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()

        return try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            when (response.statusCode()) {
                201 -> {
                    println("File '$fileName' created successfully")
                    true
                }
                422 -> {
                    println("File '$fileName' already exists.")
                    false
                }
                else -> {
                    println("Failed to create file $fileName: ${response.body()}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Network error while creating file: ${e.message}")
            false
        }
    }

    fun createPullRequest(branchName: String, baseBranch: String = "main") {
        val requestBody = """
            {
                "title": "Add Hello.txt",
                "head": "$branchName",
                "base": "$baseBranch"
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/repos/$fullName/pulls"))
            .header("Authorization", "token ${config.githubToken}")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()

        try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            when (response.statusCode()) {
                201 -> {
                    println("Pull request created successfully")
                }
                422 -> {
                    println("Pull request already exists or is invalid.")
                }
                else -> {
                    println("Failed to create pull request: ${response.body()}")
                }
            }
        } catch (e: Exception) {
            println("Network error while creating pull request: ${e.message}")
        }
    }
}

fun main() {
    val config = loadConfig()
    val repositories = fetchRepositories(config)

    if (repositories.isEmpty()) {
        println("No repositories found.")
        return
    }

    println("Available repositories:")
    repositories.forEachIndexed { index, repo -> println("${index + 1}. ${repo.name}") }

    println("Select a repository by number:")
    val selectedIndex = readLine()?.toIntOrNull()?.minus(1)
    if (selectedIndex == null || selectedIndex !in repositories.indices) {
        println("Invalid selection. Please select a valid repository number.")
        return
    }

    val selectedRepo = repositories[selectedIndex]

    println("Enter the name of the new branch:")
    val branchName = readLine()?.trim()
    if (branchName.isNullOrEmpty()) {
        println("Branch name cannot be empty")
        return
    }
    println("Enter the name of the file to create:")
    val fileName = readLine()?.trim()
    if (fileName.isNullOrEmpty()) {
        println("File name cannot be empty")
        return
    }
    println("Enter the content of the file:")
    val fileContent = readLine()?.trim()
    if (fileContent.isNullOrEmpty()) {
        println("File content cannot be empty")
        return
    }

    if (selectedRepo.createBranch(branchName) && selectedRepo.createFile(branchName, fileName, fileContent)) {
        selectedRepo.createPullRequest(branchName)
    }
}

fun loadConfig(): Config {
    val configFile = File("./src/main/resources/config.json")
    if (!configFile.exists()) {
        error("Config file not found")
    }
    return Gson().fromJson(configFile.readText(), Config::class.java)
}


 val httpClient: HttpClient = HttpClient.newHttpClient()
 val gson = Gson()

fun fetchRepositories(config: Config): List<Repository> {
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.github.com/user/repos"))
        .header("Authorization", "token ${config.githubToken}")
        .build()

    return try {
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        when (response.statusCode()) {
            200 -> {
                val repos: List<Map<String, Any>> = gson.fromJson(
                    response.body(),
                    object : TypeToken<List<Map<String, Any>>>() {}.type
                )
                repos.map {
                    Repository(it["name"] as String, it["full_name"] as String, config)
                }
            }
            401 -> {
                error("Authentication failed: Invalid GitHub token.")
            }
            else -> {
                error("Failed to fetch repositories. Status: ${response.statusCode()}. Body: ${response.body()}")
            }
        }
    } catch (e: Exception) {
        error("Network error: ${e.message}")
    }
}