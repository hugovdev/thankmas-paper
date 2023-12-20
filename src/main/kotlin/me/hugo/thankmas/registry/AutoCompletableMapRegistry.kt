package me.hugo.thankmas.registry

import revxrsal.commands.bukkit.BukkitCommandHandler
import revxrsal.commands.command.CommandActor
import revxrsal.commands.command.CommandParameter
import revxrsal.commands.exception.CommandErrorException

/**
 * Map Based Registry with a string as a key that can be
 * registered in a CommandHandler.
 */
public open class AutoCompletableMapRegistry<V>(private val type: Class<V>) : MapBasedRegistry<String, V>() {

    /** Register suggestions for this registry. */
    public fun registerCompletions(commandHandler: BukkitCommandHandler) {
        commandHandler.autoCompleter.registerParameterSuggestions(type) { _, _, _ -> getKeys() }
        commandHandler.registerValueResolver(type) { context -> getOrNull(context.pop()) }
        commandHandler.registerParameterValidator(type) { value, a: CommandParameter, _: CommandActor ->
            if (value == null) throw CommandErrorException("This ${a.name} doesn't exist!")
        }
    }

}