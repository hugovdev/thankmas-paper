package me.hugo.thankmas.git

import me.hugo.thankmas.ThankmasPlugin

/** Represents a change in a file in a Git repository. */
public data class GitFileChange(
    val filePath: String,
    val action: GitFileAction,

    var sha: String? = null, // Required when updating or creating a file
    val newValue: String? = null, // Required when updating or creating a file
    val logFile: Boolean = true
) {
    private val logger = ThankmasPlugin.instance<ThankmasPlugin<*>>().logger

    init {
        if (logFile) logger.info("[$action] to file $filePath")
    }
}