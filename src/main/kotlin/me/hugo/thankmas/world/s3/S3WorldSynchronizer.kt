package me.hugo.thankmas.world.s3

import me.hugo.thankmas.SimpleThankmasPlugin
import me.hugo.thankmas.config.ConfigurationProvider
import me.hugo.thankmas.config.string
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.craftbukkit.CraftWorld
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.zeroturnaround.zip.ZipUtil
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.io.FileOutputStream

/** Class that downloads or uploads the necessary worlds from an S3 bucket. */
@Single
public class S3WorldSynchronizer : KoinComponent {

    private val logger = SimpleThankmasPlugin.instance().logger
    private val configProvider: ConfigurationProvider by inject()

    private val s3Config: FileConfiguration
        get() = configProvider.getOrLoad("global/s3.yml")

    /** Bucket for world download/upload. */
    private val bucketName: String
        get() = s3Config.string("bucket")

    /** Returns an S3 client. */
    private fun client(): S3Client = S3Client.builder()
        .region(Region.of(s3Config.string("region")))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    s3Config.string("access-key"),
                    s3Config.string("secret-access-key")
                )
            )
        ).build()

    /** Saves [world] with flush enabled. */
    public fun saveWorldWithFlush(world: World) {
        val serverLevel = (world as CraftWorld).handle

        // Save with flush to immediately save all chunks.
        serverLevel.save(null, true, serverLevel.noSave, false)
    }

    /** Downloads the latest world in [worldDirectory] to [localPath]. */
    public fun downloadWorld(worldDirectory: String, localPath: File) {
        // worldDirectory = hub/2024

        logger.info("Downloading world $worldDirectory...")

        if (localPath.exists() && localPath.isDirectory) localPath.deleteRecursively()

        val tempFolder = Bukkit.getWorldContainer().resolve(System.currentTimeMillis().toString()).also { it.mkdirs() }

        client().use { client ->
            val request = GetObjectRequest.builder().bucket(bucketName).key("$worldDirectory/world.zip").build()

            val downloadedZip = tempFolder.resolve("world.zip")

            client.getObjectAsBytes(request)?.let { response ->
                FileOutputStream(downloadedZip).use { it.write(response.asByteArray()) }
            }

            ZipUtil.unpack(downloadedZip, localPath)

            logger.info("Downloaded world $worldDirectory...")
        }

        tempFolder.deleteRecursively()
    }

    /** Downloads the latest world in [fileDirectory] to [localPath]. */
    public fun downloadFile(fileDirectory: String, extension: String, localPath: File) {
        // worldDirectory = hub/2024

        logger.info("Downloading world $fileDirectory...")

        client().use { client ->
            val request = GetObjectRequest.builder().bucket(bucketName).key("$fileDirectory/world.$extension").build()

            client.getObjectAsBytes(request)?.let { response ->
                FileOutputStream(localPath).use { it.write(response.asByteArray()) }
            }

            logger.info("Downloaded slime file $fileDirectory...")
        }
    }

    /** Uploads this world's files to [path]. */
    public fun uploadWorld(world: World, path: String) {
        uploadFile(world.worldFolder, path)
    }

    /** Uploads [file] to the [remotePath] in remote using [client]. */
    public fun uploadFile(file: File, remotePath: String) {
        client().use { client ->

            // Vanilla world being updated!
            if (file.isDirectory) {
                val tempFolder =
                    Bukkit.getWorldContainer().resolve(System.currentTimeMillis().toString()).also { it.mkdirs() }

                val zipFile = tempFolder.resolve("world.zip")
                ZipUtil.pack(file, zipFile)

                client.putObject(
                    PutObjectRequest.builder().bucket(bucketName).key("$remotePath/world.zip").build(),
                    RequestBody.fromFile(zipFile)
                )

                tempFolder.deleteRecursively()
            }
            // Slime world being uploaded!
            else if (file.extension in listOf("slime", "polar")) {
                client.putObject(
                    PutObjectRequest.builder().bucket(bucketName).key("$remotePath/world.${file.extension}").build(),
                    RequestBody.fromFile(file)
                )
            } else throw IllegalArgumentException("File ${file.name} can't be uploaded to S3, unsupported file type!")

        }
    }
}