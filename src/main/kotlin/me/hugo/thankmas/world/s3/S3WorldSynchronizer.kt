package me.hugo.thankmas.world.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.endpoints.S3EndpointProvider
import aws.sdk.kotlin.services.s3.listObjectsV2
import aws.sdk.kotlin.services.s3.model.*
import aws.sdk.kotlin.services.s3.putObject
import aws.sdk.kotlin.services.s3.withConfig
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.client.endpoints.Endpoint
import aws.smithy.kotlin.runtime.collections.Attributes
import aws.smithy.kotlin.runtime.content.asByteStream
import aws.smithy.kotlin.runtime.content.writeToFile
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.config.ConfigurationProvider
import me.hugo.thankmas.config.string
import me.hugo.thankmas.coroutines.runBlockingMine
import org.bukkit.World
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

/** Class that downloads or uploads the necessary worlds from an S3 bucket. */
@Single
public class S3WorldSynchronizer : KoinComponent {

    private val logger = ThankmasPlugin.instance().logger
    private val configProvider: ConfigurationProvider by inject()

    /** Bucket for world download/upload. */
    public val configBucket: String
        get() = configProvider.getOrLoad("global/s3.yml").string("bucket")

    /** Returns an S3 client. */
    public suspend fun getClient(): S3Client = S3Client.fromEnvironment {
        val s3Config = configProvider.getOrLoad("global/s3.yml")

        region = s3Config.string("region")
        endpointProvider = S3EndpointProvider { Endpoint("${s3Config.string("endpoint")}/$configBucket") }

        credentialsProvider = object : CredentialsProvider {
            override suspend fun resolve(attributes: Attributes): Credentials = Credentials(
                s3Config.string("access-key"),
                s3Config.string("secret-access-key")
            )
        }
    }.withConfig {
        // Disable chunk signing!
        enableAwsChunked = false
    }

    /** Downloads the latest world in [worldDirectory] to [localPath]. */
    public suspend fun downloadWorld(worldDirectory: String, localPath: File) {
        // worldDirectory = hub/2024
        // selectedDirectory = hub/2024/{timestamp}
        // relativeDirectory = [[hub/2024/{timestamp}]]/entities

        logger.info("Downloading world $worldDirectory...")

        if (localPath.exists() && localPath.isDirectory) localPath.deleteRecursively()
        val tasks: MutableList<Deferred<*>> = mutableListOf()

        getClient().use { client ->
            runBlockingMine {
                val selectedDirectories = client.listObjectsV2 {
                    bucket = configBucket
                    prefix = "$worldDirectory/"
                    delimiter = "/"
                }.commonPrefixes?.mapNotNull { it.prefix }
                    ?.sortedByDescending { it.removePrefix("$worldDirectory/") }
                    ?: emptyList()

                val selectedDirectory = selectedDirectories.firstOrNull() ?: return@runBlockingMine

                // Download all the files!
                client.listObjectsV2 {
                    bucket = configBucket
                    prefix = selectedDirectory
                }.contents?.filter { it.key != null }?.forEach {
                    val request = GetObjectRequest {
                        key = it.key
                        bucket = configBucket
                    }

                    val relativeDirectory = it.key!!.removePrefix(selectedDirectory)

                    tasks += async {
                        client.getObject(request) { resp ->
                            resp.body?.writeToFile(localPath.resolve(relativeDirectory).also { it.parentFile.mkdirs() })
                        }
                    }
                }

                // Delete the oldest map cache if we already have 15 versions!
                if (selectedDirectories.size > 15) {
                    val oldestMapFiles = client.listObjectsV2 {
                        bucket = configBucket
                        prefix = selectedDirectories.last()
                    }.contents?.mapNotNull {
                        it.key?.let {
                            ObjectIdentifier {
                                key = it
                            }
                        }
                    } ?: return@runBlockingMine

                    client.deleteObjects(DeleteObjectsRequest {
                        bucket = configBucket
                        delete = Delete { objects = oldestMapFiles }
                    })
                }
            }

            tasks.awaitAll()

            logger.info("Downloaded world $worldDirectory...")
        }
    }

    /** Uploads this world's files to [path]. */
    public suspend fun uploadWorld(world: World, path: String) {
        val client = getClient()

        client.use { s3 ->
            uploadFolder(s3, world.worldFolder, "$path/${System.currentTimeMillis()}").awaitAll()
        }
    }

    /** Uploads [folder] to the [newPath] in remote using [client]. */
    public suspend fun uploadFolder(
        client: S3Client,
        folder: File,
        newPath: String
    ): List<Deferred<PutObjectResponse>> {
        val tasks: MutableList<Deferred<PutObjectResponse>> = mutableListOf()

        runBlockingMine {
            folder.listFiles()?.forEach {
                if (it.isDirectory) {
                    tasks += uploadFolder(client, it, "$newPath/${it.name}")
                    return@forEach
                }

                tasks += async {
                    client.putObject {
                        bucket = configBucket
                        key = "$newPath/${it.name}"
                        body = it.asByteStream()
                    }
                }
            }
        }

        return tasks
    }
}