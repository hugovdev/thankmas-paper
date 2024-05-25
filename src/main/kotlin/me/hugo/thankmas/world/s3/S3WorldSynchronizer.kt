package me.hugo.thankmas.world.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.endpoints.S3EndpointProvider
import aws.sdk.kotlin.services.s3.listObjectsV2
import aws.sdk.kotlin.services.s3.model.PutObjectResponse
import aws.sdk.kotlin.services.s3.putObject
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.auth.awssigning.AwsSigningAttributes
import aws.smithy.kotlin.runtime.auth.awssigning.HashSpecification
import aws.smithy.kotlin.runtime.client.ProtocolRequestInterceptorContext
import aws.smithy.kotlin.runtime.client.endpoints.Endpoint
import aws.smithy.kotlin.runtime.collections.Attributes
import aws.smithy.kotlin.runtime.content.asByteStream
import aws.smithy.kotlin.runtime.http.interceptors.HttpInterceptor
import aws.smithy.kotlin.runtime.http.request.HttpRequest
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import me.hugo.thankmas.config.ConfigurationProvider
import me.hugo.thankmas.config.string
import org.bukkit.World
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

/** Class that downloads or uploads the necessary worlds from an S3 bucket. */
@Single
public class S3WorldSynchronizer : KoinComponent {

    private val configProvider: ConfigurationProvider by inject()

    /** Bucket for world download/upload. */
    public val configBucket: String
        get() = configProvider.getOrLoad("global/s3.yml").string("bucket")

    /** Returns an S3 client. */
    public suspend fun getClient(): S3Client = S3Client.fromEnvironment {
        val s3Config = configProvider.getOrLoad("global/s3.yml")

        interceptors = mutableListOf(DisableChunkedSigning())
        region = s3Config.string("region")
        endpointProvider = S3EndpointProvider { Endpoint("${s3Config.string("endpoint")}/$configBucket") }

        credentialsProvider = object : CredentialsProvider {
            override suspend fun resolve(attributes: Attributes): Credentials = Credentials(
                s3Config.string("access-key"),
                s3Config.string("secret-access-key")
            )
        }
    }

    /** Uploads this world's files to [path]. */
    public suspend fun uploadWorld(world: World, path: String) {
        val client = getClient()

        client.use { s3 ->
            s3.listObjectsV2 {
                bucket = configBucket
                prefix = "$path/"
            }.contents?.forEach { println(it.key) }

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

        runBlocking {
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

    public class DisableChunkedSigning : HttpInterceptor {
        override suspend fun modifyBeforeSigning(context: ProtocolRequestInterceptorContext<Any, HttpRequest>): HttpRequest {
            context.executionContext[AwsSigningAttributes.HashSpecification] = HashSpecification.UnsignedPayload
            return super.modifyBeforeSigning(context)
        }
    }
}