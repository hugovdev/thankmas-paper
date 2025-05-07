package me.hugo.thankmas.world

import com.infernalsuite.aswm.api.AdvancedSlimePaperAPI
import dev.kezz.miniphrase.MiniPhraseContext
import dev.kezz.miniphrase.audience.sendTranslated
import live.minehub.polarpaper.Polar
import live.minehub.polarpaper.PolarWorld
import live.minehub.polarpaper.PolarWorldAccess
import live.minehub.polarpaper.PolarWriter
import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.world.registry.PolarWorldRegistry
import me.hugo.thankmas.world.registry.SlimeWorldRegistry
import me.hugo.thankmas.world.s3.S3WorldSynchronizer
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import software.amazon.awssdk.services.s3.model.S3Exception
import java.nio.file.Files
import java.util.concurrent.CompletableFuture

public enum class WorldFormat : KoinComponent {

    ANVIL {
        context(MiniPhraseContext) override fun pushWorld(
            pusher: Player,
            bukkitWorld: World,
            scopeDirectory: String,
            worldSynchronizer: S3WorldSynchronizer,
            worldConfig: FileConfiguration,
            onSuccess: () -> Unit,
            onFailure: () -> Unit,
        ) {
            Bukkit.getScheduler().runTaskAsynchronously(ThankmasPlugin.instance(), Runnable {
                try {
                    // Just push the world folder! :)
                    worldSynchronizer.uploadWorld(bukkitWorld, scopeDirectory)
                    onSuccess()
                } catch (exception: S3Exception) {
                    onFailure()
                    exception.printStackTrace()
                }
            })
        }
    },
    SLIME {
        context(MiniPhraseContext) override fun pushWorld(
            pusher: Player,
            bukkitWorld: World,
            scopeDirectory: String,
            worldSynchronizer: S3WorldSynchronizer,
            worldConfig: FileConfiguration,
            onSuccess: () -> Unit,
            onFailure: () -> Unit
        ) {
            val oldLocations = bukkitWorld.players.associateWith { it.location }

            bukkitWorld.players.forEach { it.teleport(Bukkit.getWorld("world")!!.spawnLocation) }
            Bukkit.unloadWorld(bukkitWorld, true)

            val worldName = bukkitWorld.name

            pusher.sendTranslated("maps.temporary_teleport") {
                parsed("map", worldName)
                parsed("scope", scopeDirectory)
            }

            val slimeWorldRegistry: SlimeWorldRegistry by ThankmasPlugin.instance<ThankmasPlugin<*>>().inject()
            val slimePaperAPI = AdvancedSlimePaperAPI.instance()

            Bukkit.getScheduler().runTaskAsynchronously(ThankmasPlugin.instance<ThankmasPlugin<*>>(), Runnable {
                try {
                    // Save SlimeWorld in memory!
                    val slimeWorld = slimePaperAPI.readVanillaWorld(
                        bukkitWorld.worldFolder,
                        worldName,
                        slimeWorldRegistry.defaultSlimeLoader
                    )

                    // Save into a file!
                    slimePaperAPI.saveWorld(slimeWorld)

                    // Upload the slime file!
                    worldSynchronizer.uploadFile(
                        slimeWorldRegistry.slimeWorldContainer.resolve("$worldName.slime"),
                        scopeDirectory
                    )

                    Bukkit.getScheduler().runTask(ThankmasPlugin.instance<ThankmasPlugin<*>>(), Runnable {
                        val newWorld = Bukkit.createWorld(WorldCreator(bukkitWorld.name))

                        // Teleport players back!
                        oldLocations.forEach { (player, location) ->
                            location.world = newWorld
                            if (player.isOnline) player.teleport(location)
                        }

                        onSuccess()
                    })
                } catch (exception: Exception) {
                    onFailure()
                    exception.printStackTrace()
                }
            })
        }
    },
    POLAR {
        context(MiniPhraseContext) override fun pushWorld(
            pusher: Player,
            bukkitWorld: World,
            scopeDirectory: String,
            worldSynchronizer: S3WorldSynchronizer,
            worldConfig: FileConfiguration,
            onSuccess: () -> Unit,
            onFailure: () -> Unit
        ) {
            val chunkRadius = worldConfig.getInt("${bukkitWorld.name}.chunk-radius")

            require(chunkRadius > 0)
            { "Polar world format requires a chunk-radius field to be pushed. It also needs to be higher than 0." }

            // Create a new polar world instance!
            val polarWorld = PolarWorld()

            val centerChunk = bukkitWorld.spawnLocation.chunk

            val futures: MutableList<CompletableFuture<Chunk>> = mutableListOf()

            for (x in -chunkRadius..<chunkRadius) {
                for (z in -chunkRadius..<chunkRadius) {
                    futures += bukkitWorld.getChunkAtAsync(centerChunk.x + x, centerChunk.z + z)
                }
            }

            CompletableFuture.allOf(*futures.toTypedArray()).thenRun {
                Bukkit.getScheduler().runTaskAsynchronously(ThankmasPlugin.instance<ThankmasPlugin<*>>(), Runnable writeAndUpload@{
                    try {
                        for (x in -chunkRadius..<chunkRadius) {
                            for (z in -chunkRadius..<chunkRadius) {
                                Polar.updateChunkData(
                                    polarWorld,
                                    PolarWorldAccess.DEFAULT,
                                    bukkitWorld.getChunkAt(centerChunk.x + x, centerChunk.z + z),
                                    centerChunk.x + x,
                                    centerChunk.z + z
                                )
                            }
                        }

                        val polarWorldRegistry: PolarWorldRegistry by ThankmasPlugin.instance<ThankmasPlugin<*>>().inject()

                        val polarBytes = PolarWriter.write(polarWorld)
                        val polarFile = polarWorldRegistry.polarWorldContainer.resolve(bukkitWorld.name + ".polar")

                        polarWorldRegistry.polarWorldContainer.mkdirs()
                        Files.write(polarFile.toPath(), polarBytes)

                        // Upload the slime file!
                        worldSynchronizer.uploadFile(polarFile, scopeDirectory)
                        onSuccess()
                    } catch (exception: Exception) {
                        onFailure()
                        exception.printStackTrace()
                        return@writeAndUpload
                    }
                })
            }
        }
    };

    context(MiniPhraseContext)
    public abstract fun pushWorld(
        pusher: Player,
        bukkitWorld: World,
        scopeDirectory: String,
        worldSynchronizer: S3WorldSynchronizer,
        worldConfig: FileConfiguration,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
    )

}