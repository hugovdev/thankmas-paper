package me.hugo.thankmas.git

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.config.ConfigurationProvider
import org.bukkit.Bukkit
import org.bukkit.WorldCreator
import org.bukkit.scheduler.BukkitRunnable
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.*
import java.net.URI
import java.net.URL
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

    public val accessToken: String
        get() = requireNotNull(configProvider.getOrResources("git.yml", "base").getString("access-token"))
        { "Tried to access a null git token. Set it up on base/git.yml." }

    public val gitHubApiUrl: String
        get() = requireNotNull(configProvider.getOrResources("git.yml", "base").getString("github-api-url"))
        { "Tried to access a null github api url. Set it up on base/git.yml." }

    public fun fetchScope(scope: String): JsonArray {
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create("$gitHubApiUrl/$scope"))
            .header("Authorization", "Bearer $accessToken")
            .header("Accept", "application/vnd.github+json")
            .timeout(10.seconds.toJavaDuration())
            .build()

        val response: HttpResponse<String> = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString())

        return JsonParser.parseString(response.body()).asJsonArray
    }

    public fun pushFileChange(scope: String, newValue: String, commitMessage: String, sha: String? = null): JsonObject {
        val body = JsonObject().also {
            it.addProperty("content", newValue)
            it.addProperty("message", commitMessage)
            if (sha != null) it.addProperty("sha", sha)
        }

        val request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
            .uri(URI.create("$gitHubApiUrl/$scope"))
            .header("Authorization", "Bearer $accessToken")
            .header("Accept", "application/vnd.github+json")
            .timeout(20.seconds.toJavaDuration())
            .build()

        val response: HttpResponse<String> = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString())

        return JsonParser.parseString(response.body()).asJsonObject
    }

    public fun deleteFile(scope: String, commitMessage: String, sha: String): JsonObject {
        val body = JsonObject().also {
            it.addProperty("message", commitMessage)
            it.addProperty("sha", sha)
        }

        val request = HttpRequest.newBuilder()
            .method("DELETE", HttpRequest.BodyPublishers.ofString(body.toString()))
            .uri(URI.create("$gitHubApiUrl/$scope"))
            .header("Authorization", "Bearer $accessToken")
            .header("Accept", "application/vnd.github+json")
            .timeout(20.seconds.toJavaDuration())
            .build()

        val response: HttpResponse<String> = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString())

        return JsonParser.parseString(response.body()).asJsonObject
    }

    public fun downloadFileIn(fileName: String, url: String, path: File) {
        if (fileName.startsWith(".")) {
            logger.info("Omitted $fileName from the download.")
            return
        }

        try {
            URL(url).openStream().use { input ->
                if (path.exists() && !path.isDirectory) path.deleteRecursively()

                path.mkdirs()
                Files.copy(input, path.resolve(fileName).toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    public fun downloadDirectory(scope: String, path: File) {
        fetchScope(scope).forEach { file ->
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
                downloadDirectory(scopePath, Bukkit.getPluginsFolder().resolve(scopePath))
                return@forEach
            }

            downloadFileIn(downloadName, jsonFile.get("download_url").asString, path.also { it.mkdir() })
        }
    }

    public fun downloadScope(scope: String, path: File) {
        logger.info("Downloading scope $scope...")
        downloadDirectory(scope, path)
        logger.info("Done downloading scope $scope!")
    }

    public fun redownloadWorld(worldName: String, scopeWorld: String, onSuccess: () -> Unit) {
        if (Bukkit.unloadWorld(worldName, false)) {
            object : BukkitRunnable() {
                override fun run() {
                    downloadScope(scopeWorld, Bukkit.getPluginsFolder().resolve(scopeWorld))

                    // Remove the old world.
                    val worldFile = Bukkit.getWorldContainer().resolve(worldName)
                    worldFile.deleteRecursively()

                    copyFolder(
                        Bukkit.getPluginsFolder().resolve(scopeWorld).toPath(),
                        Bukkit.getWorldContainer().resolve(worldName).toPath()
                    )

                    object : BukkitRunnable() {
                        override fun run() {
                            Bukkit.createWorld(WorldCreator(worldName))
                            onSuccess()
                        }
                    }.runTask(ThankmasPlugin.instance())
                }
            }.runTaskAsynchronously(ThankmasPlugin.instance())
        }
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