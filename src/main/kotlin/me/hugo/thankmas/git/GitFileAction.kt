package me.hugo.thankmas.git

/** Represents an action to a file in a Git repository. */
public enum class GitFileAction {
    CREATE, UPDATE, DELETE;

    public val isDeletion: Boolean
        get() = this == DELETE
}