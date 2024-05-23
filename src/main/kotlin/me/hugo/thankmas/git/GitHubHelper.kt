package me.hugo.thankmas.git

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import me.hugo.thankmas.config.ConfigurationProvider
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

@Single
/** Provides helper functions for GitHub shenanigans. */
public class GitHubHelper : KoinComponent {

    private val configProvider: ConfigurationProvider by inject()

    public val accessToken: String?
        get() = configProvider.getOrResources("git.yml", "base").getString("access-token")

    public val gitHubApiUrl: String?
        get() = configProvider.getOrResources("git.yml", "base").getString("github-api-url")

    public fun fetchScope(scope: String): JsonArray {
        val scopeUrl = URL("$gitHubApiUrl/$scope")
        val openedConnection = scopeUrl.openConnection() as HttpURLConnection
        openedConnection.setRequestProperty("Authorization", "Bearer $accessToken")
        openedConnection.requestMethod = "GET"

        InputStreamReader(openedConnection.inputStream).use { stream ->
            val reader = BufferedReader(stream)
            val response = reader.readText()

            return JsonParser.parseString(response).asJsonArray
        }
    }

    public fun push(scope: String, sha: String, newValue: String): JsonArray {
        val scopeUrl = URL("$gitHubApiUrl/$scope")
        val openedConnection = scopeUrl.openConnection() as HttpURLConnection
        openedConnection.setRequestProperty("Authorization", "Bearer $accessToken")
        openedConnection.requestMethod = "PUT"

        InputStreamReader(openedConnection.inputStream).use { stream ->
            val reader = BufferedReader(stream)
            val response = reader.readText()

            return JsonParser.parseString(response).asJsonArray
        }
    }

}