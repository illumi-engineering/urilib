package com.oxyggen.io

/**
 * Path object
 *
 * This class defines the path object. Use method *parse* to to convert string
 * to Path object.
 */
open class Path protected constructor(
    val device: String,
    val parts: List<String>,
    val file: String,
    val isAbsolute: Boolean,
    val pathSeparator: String = "/") {

    companion object {
        private const val DEFAULT_PATH_SEPARATOR = "/"
        const val FILE_EXTENSION_SEPARATOR = "."
        const val DIRECTORY_CURRENT = "."
        const val DIRECTORY_PARENT = ".."

        /**
         * Returns a new Path object for given [path].
         * Default [pathSeparator] is slash used in linux, URL, etc...
         * Empty path is same as root path: "/". Relative path to current directory should be "."
         * @return the new path object
         **/
        fun parse(path: String, pathSeparator: String = DEFAULT_PATH_SEPARATOR): Path {
            var device = ""
            var file = ""
            var isAbsolute = false

            val allParts = path.split(pathSeparator).toMutableList()

            if (allParts.size > 0) {
                // Get the first element:
                //  if it's empty -> path begins with pathSeparator (/dev/sda)
                //  if it's a device name -> windows absolute path (C:\temp)
                val first = allParts.first()

                // If the path begins with device "C:" then remember device
                device = if ("^[a-zA-Z]:".toRegex().matches(first)) first else ""

                // Check whether path is absolute (begins with device or blank)
                // If it's absolute, remove the first part ("C:" or blank - folder before first slash)
                isAbsolute = device.isNotBlank() || first.isBlank()
                if (isAbsolute) allParts.removeAt(0)
            }

            if (allParts.size > 0) {
                // Get the last element
                //  it's a directory if ends with pathSeparator (/dev/)
                //  it's a directory if it ends with "parent" (/dev/..)
                val last = allParts.last()

                val containsFile = last != DIRECTORY_CURRENT && last != DIRECTORY_PARENT && last != ""

                file = if (containsFile) last else ""

                if (containsFile || last.isBlank()) {
                    allParts.removeAt(allParts.size - 1)
                }
            }

            return Path(
                    device = device,
                    parts = allParts,
                    file = file,
                    isAbsolute = isAbsolute,
                    pathSeparator = pathSeparator)
        }
    }

    private fun determineNormalizedFolders(parts: List<String>): List<String> {
        val normalizedFolders = mutableListOf<String>()

        for (i in parts.indices)
            when (val part = parts[i]) {
                "" -> if (normalizedFolders.size == 0 ||  normalizedFolders.last().isNotBlank()) normalizedFolders.add(part)
                DIRECTORY_CURRENT -> if (i == 0) normalizedFolders.add(part) /*else if (i == folders.size - 1) normalizedFolders.add("")*/
                DIRECTORY_PARENT -> if ((normalizedFolders.size > 1) || (normalizedFolders.size == 1 && normalizedFolders[0] != DIRECTORY_CURRENT))
                    normalizedFolders.removeAt(normalizedFolders.size - 1)
                else -> normalizedFolders.add((part))
            }

        return normalizedFolders
    }

    /**
     * The filename without extension
     **/
    open val fileName by lazy {
        if (file.contains(FILE_EXTENSION_SEPARATOR)) file.substringBeforeLast(FILE_EXTENSION_SEPARATOR) else file
    }

    /**
     * The file extension
     **/
    open val fileExtension by lazy {
        if (file.contains(FILE_EXTENSION_SEPARATOR)) file.substringAfterLast(FILE_EXTENSION_SEPARATOR) else ""
    }

    /**
     * The directory name
     **/
    open val directory by lazy {
        (if (isAbsolute) pathSeparator else "") + parts.joinToString(pathSeparator) + (if (parts.isNotEmpty()) pathSeparator else "")
    }

    /**
     * Directory & filename
     **/
    open val complete by lazy {
        device + directory + file
    }

    /**
     * The normalized path
     **/
    open val normalized by lazy {
        if (this is NormalizedPath) this else NormalizedPath(device, determineNormalizedFolders(parts), file, isAbsolute, pathSeparator)
    }

    /**
     * Resolve path within current context
     * @param anotherPath another path object
     * @return the resolved path
     **/
    open fun resolve(anotherPath: Path): Path =
            if (anotherPath.isAbsolute || anotherPath.pathSeparator != pathSeparator) {
                anotherPath
            } else {
                Path(
                        device = this.device,
                        parts = this.parts + anotherPath.parts,
                        file = anotherPath.file,
                        isAbsolute = this.isAbsolute,
                        pathSeparator = this.pathSeparator)
            }


    /**
     * Resolve path within current context
     * @param anotherPath another path string
     * @return the resolved path
     **/
    open fun resolve(anotherPath: String) = resolve(parse(anotherPath))


    /**
     * Resolve path within current context and normalize the new path
     * @param anotherPath another path object
     * @return the resolved normalized path
     **/
    open fun resolveNormalized(anotherPath: Path): NormalizedPath = resolve(anotherPath).normalized


    /**
     * Resolve path within current context and normalize the new path
     * @param anotherPath another path string
     * @return the resolved normalized path
     **/
    open fun resolveNormalized(anotherPath: String) = resolveNormalized(parse(anotherPath))

    private val fullHashCode: Int by lazy { complete.hashCode() }


    /**
     * Return hash code for the path
     * @return hash code
     **/
    override fun hashCode(): Int = fullHashCode

    /**
     * Check whether the path is same as another path
     * @return true if it's same path
     **/
    override fun equals(other: Any?): Boolean =
            if (other is Path) {
                other.hashCode() == this.hashCode() && other.complete == this.complete
            } else {
                false
            }

}