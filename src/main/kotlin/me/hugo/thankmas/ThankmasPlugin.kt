package me.hugo.thankmas

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import dev.kezz.miniphrase.MiniPhrase
import dev.kezz.miniphrase.i18n.PropertiesFileTranslationRegistry
import me.hugo.thankmas.config.ConfigurationProvider
import me.hugo.thankmas.dependencyinjection.ThankmasModules
import me.hugo.thankmas.items.clickable.ClickableItemRegistry
import me.hugo.thankmas.listener.MenuManager
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*


/**
 * JavaPlugin that also registers default translations for
 * the plugin and registers dependency injection for these
 * APIs utility classes.
 */
public open class ThankmasPlugin(
    private val configScopes: List<String> = listOf(),
    private val localTranslationDirectory: String =
        if (configScopes.isNotEmpty()) "${configScopes.first()}/lang/"
        else "local",
    private val downloadGlobalScope: Boolean = true
) :
    JavaPlugin(), KoinComponent {

    private val configProvider: ConfigurationProvider by inject()

    /** Global minimessage instance with all custom tags and styling. */
    public lateinit var miniMessage: MiniMessage.Builder

    /** Default plugin translations in: "plugins/plugin_name/lang" */
    public lateinit var translations: DefaultTranslations

    /** Global shared translations for all Thankmas plugins. */
    public lateinit var globalTranslations: MiniPhrase

    public companion object {
        private var instance: ThankmasPlugin? = null

        public fun instance(): ThankmasPlugin {
            val instance = instance
            requireNotNull(instance) { "Tried to fetch a ThankmasPlugin instance while it's null!" }

            return instance
        }
    }

    override fun onLoad() {
        instance = this

        // Register the dependency injection modules.
        startKoin { modules(ThankmasModules().module) }

        val gitConfig = configProvider.getOrResources("git.yml", "base")
        val accessToken = gitConfig.getString("access-token")

        if (accessToken == null) {
            logger.warning("No GitHub access token provided, shutting down server.")
            Bukkit.shutdown()
            return
        }

        downloadConfigFiles(accessToken)
    }

    override fun onEnable() {
        miniMessage = MiniMessage.builder().tags(TagResolver.builder().apply {
            val stylesConfig = configProvider.getOrLoad("global/custom_styles.yml")

            stylesConfig.getConfigurationSection("custom_styles")?.getKeys(false)?.forEach {
                val style =
                    TextColor.fromHexString(stylesConfig.getString("custom_styles.$it", "#fffff")!!) ?: return@forEach

                tag(it, Tag.styling(style))
            }

            resolver(TagResolver.standard())
        }.build())

        translations = DefaultTranslations(File(Bukkit.getPluginsFolder(), localTranslationDirectory), miniMessage)

        globalTranslations = MiniPhrase.configureAndBuild {
            miniMessage(translations.translations.miniMessage)
            translationRegistry(PropertiesFileTranslationRegistry(File(Bukkit.getPluginsFolder(), "global/lang/")))
            defaultLocale(Locale.US)
        }

        val pluginManager = Bukkit.getPluginManager()

        val itemRegistry: ClickableItemRegistry by inject()
        pluginManager.registerEvents(itemRegistry, this)

        val menuManager: MenuManager by inject()
        pluginManager.registerEvents(menuManager, this)
    }

    /** Downloads all the config files from GitHub. */
    private fun downloadConfigFiles(accessToken: String) {
        logger.info("Starting scope download...")

        val pluginsFolder = Bukkit.getPluginsFolder()

        fun fetchScope(scope: String): JsonArray {
            val scopeUrl = URL("https://api.github.com/repos/hugovdev/ThankmasScopes/contents/scopes/$scope")
            val openedConnection = scopeUrl.openConnection() as HttpURLConnection
            openedConnection.setRequestProperty("Authorization", "Bearer $accessToken")
            openedConnection.requestMethod = "GET"

            val reader = BufferedReader(InputStreamReader(openedConnection.inputStream))

            val response = reader.readText()

            return JsonParser.parseString(response).asJsonArray
        }

        fun downloadFileIn(fileName: String, url: String, path: File) {
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

        fun downloadScope(scope: String, path: File) {
            logger.info("Downloading scope $scope...")

            fetchScope(scope).forEach { file ->
                val jsonFile = file.asJsonObject

                val downloadUrl = jsonFile.get("download_url")
                val downloadName = jsonFile.get("name").asString

                if (downloadUrl.isJsonNull) {
                    val subfolder = jsonFile.get("path").asString
                    val scopePath = subfolder.removePrefix("scopes/")

                    if (downloadName.startsWith(".")) {
                        logger.info("Omitted download of scope $scopePath.")
                        return@forEach
                    }

                    downloadScope(scopePath, pluginsFolder.resolve(scopePath))
                    return@forEach
                }

                downloadFileIn(downloadName, downloadUrl.asString, path.also { it.mkdir() })
            }

            logger.info("Done downloading scope $scope!")
        }

        if (downloadGlobalScope) downloadScope("global", pluginsFolder.resolve("global"))
        configScopes.forEach { downloadScope(it, pluginsFolder.resolve(it).also { it.mkdirs() }) }
    }

}