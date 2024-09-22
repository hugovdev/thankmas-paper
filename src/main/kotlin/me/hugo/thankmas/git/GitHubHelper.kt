package me.hugo.thankmas.git

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.config.ConfigurationProvider
import org.bukkit.Bukkit
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Single
/** Provides helper functions for GitHub shenanigans. */
public class GitHubHelper : KoinComponent {

    private val configProvider: ConfigurationProvider by inject()
    private val logger = ThankmasPlugin.instance().logger

    private val accessToken: String
        get() = requireNotNull(configProvider.getOrResources("git.yml", "base").getString("access-token"))
        { "Tried to access a null git token. Set it up on base/git.yml." }

    private val user: String
        get() = requireNotNull(configProvider.getOrResources("git.yml", "base").getString("github-user"))
        { "Tried to access a null github username. Set it up on base/git.yml." }

    private val repository: String
        get() = requireNotNull(configProvider.getOrResources("git.yml", "base").getString("github-repository"))
        { "Tried to access a null github repository. Set it up on base/git.yml." }

    private val branch: String
        get() = configProvider.getOrResources("git.yml", "base").getString("branch") ?: "main"

    private val gitHubBaseUrl: String
        get() = "https://api.github.com/repos/$user/$repository"

    private val gitHubReposUrl: String
        get() = "$gitHubBaseUrl/contents"

    /** Downloads [scope] from the configured GitHub repository into the local path [path]. */
    public fun downloadScope(
        scope: String,
        path: File = Bukkit.getPluginsFolder().resolve(scope).also { it.mkdirs() }
    ) {
        logger.info("Downloading scope $scope...")
        downloadDirectory("scopes/$scope", path)
        logger.info("Done downloading scope $scope!")
    }

    /** Downloads every file and subfolder on [remotePath] to [path]. */
    public fun downloadDirectory(remotePath: String, path: File) {
        fetchRemoteDirectory(remotePath).forEach { file ->
            val jsonFile = file.asJsonObject

            val downloadName = jsonFile.get("name").asString

            if (jsonFile.get("type").asString == "dir") {
                val subfolder = jsonFile.get("path").asString
                val scopePath = subfolder.removePrefix("scopes/")

                if (downloadName.startsWith(".")) {
                    logger.info("Omitted download of scope $scopePath.")
                    return@forEach
                }

                logger.info("Downloading subfolder $scopePath...")
                downloadDirectory("scopes/$scopePath", Bukkit.getPluginsFolder().resolve(scopePath))
                return@forEach
            }

            downloadFileIn(downloadName, jsonFile.get("download_url").asString, path.also { it.mkdir() })
        }
    }

    /** Returns a json array of every file and folder in [remotePath]. */
    public fun fetchRemoteDirectory(remotePath: String): JsonArray {
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create("$gitHubReposUrl/$remotePath?ref=$branch"))
            .header("Authorization", "Bearer $accessToken")
            .header("Accept", "application/vnd.github+json")
            .timeout(10.seconds.toJavaDuration())
            .build()

        val response: HttpResponse<String> = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString())

        return JsonParser.parseString(response.body()).asJsonArray
    }

    /** Downloads the file in [url] to local path [path] with name [fileName]. */
    public fun downloadFileIn(fileName: String, url: String, path: File) {
        if (fileName.startsWith(".")) {
            logger.info("Omitted $fileName from the download.")
            return
        }

        URI(url).toURL().openStream().use { input ->
            if (path.exists() && !path.isDirectory) path.deleteRecursively()

            path.mkdirs()
            Files.copy(input, path.resolve(fileName).toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }

    /** Pushes all the [files] to [branch] with [commitMessage]. */
    public fun pushFileChanges(files: Iterable<GitFileChange>, commitMessage: String) {
        fun gitRequest(): HttpRequest.Builder = HttpRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .header("Accept", "application/vnd.github+json")
            .timeout(10.seconds.toJavaDuration())

        val client = HttpClient.newHttpClient()

        // Retrieve the sha from the latest commit!
        val shaLatestCommit = gitRequest().uri(URI.create("$gitHubBaseUrl/git/refs/heads/$branch")).GET().build()
            .let { JsonParser.parseString(client.send(it, HttpResponse.BodyHandlers.ofString()).body()).asJsonObject }
            .getAsJsonObject("object").get("sha").asString

        // Retrieve the sha from the base tree!
        val shaBaseTree = gitRequest().uri(URI.create("$gitHubBaseUrl/git/commits/$shaLatestCommit")).GET().build()
            .let { JsonParser.parseString(client.send(it, HttpResponse.BodyHandlers.ofString()).body()).asJsonObject }
            .getAsJsonObject("tree").get("sha").asString

        // Post all the file updates into a new tree!
        val newTreeSha = gitRequest().uri(URI.create("$gitHubBaseUrl/git/trees"))
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    JsonObject().also { bodyObject ->
                        bodyObject.addProperty("base_tree", shaBaseTree)

                        // Create the list of files to create, update and delete.
                        bodyObject.add("tree", JsonArray().also { treeArray ->
                            files.forEach { file ->
                                treeArray.add(JsonObject().also { fileObject ->
                                    fileObject.addProperty("path", file.filePath)
                                    fileObject.addProperty("mode", "100644")
                                    fileObject.addProperty("type", "blob")

                                    if (!file.action.isDeletion) fileObject.addProperty("content", file.newValue)
                                    else fileObject.add("sha", null)
                                })
                            }
                        })
                    }.toString()
                )
            ).build().let {
                val response = client.send(it, HttpResponse.BodyHandlers.ofString()).body()
                JsonParser.parseString(response).asJsonObject.get("sha").asString
            }

        // Commit all the file changes and retrieve the commit sha!
        val newCommitSha = gitRequest().uri(URI.create("$gitHubBaseUrl/git/commits"))
            .POST(HttpRequest.BodyPublishers.ofString(JsonObject().also { bodyObject ->
                bodyObject.add("parents", JsonArray().also { it.add(shaLatestCommit) })
                bodyObject.addProperty("tree", newTreeSha)
                bodyObject.addProperty("message", commitMessage)
            }.toString())).build().let {
                val response = client.send(it, HttpResponse.BodyHandlers.ofString()).body()
                JsonParser.parseString(response).asJsonObject.get("sha").asString
            }

        // Change the main branch commit to the one we just made!
        gitRequest().uri(URI.create("$gitHubBaseUrl/git/refs/heads/$branch"))
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    JsonObject().also { it.addProperty("sha", newCommitSha) }.toString()
                )
            ).build().let { client.send(it, HttpResponse.BodyHandlers.ofString()) }
    }

    @Throws(IOException::class)
    public fun copyFolder(src: Path, dest: Path) {
        Files.walk(src).use { stream ->
            stream.forEach { source: Path ->
                Files.copy(source, dest.resolve(src.relativize(source)), StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
}