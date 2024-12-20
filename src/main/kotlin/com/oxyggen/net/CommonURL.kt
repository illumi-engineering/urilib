package com.oxyggen.net

import com.oxyggen.io.Path

// Common URL
open class CommonURL(uriString: String, val context: ContextURI? = null) : URL(uriString, context) {

    private val schemeSpecificPartPattern = "^?(//(?<authority>((?<userinfo>[^/?#]*)@)?(?<host>[^/?#:]*)(:(?<port>[^/?#]*))?))?(?<path>[^?#]*)(\\?(?<query>[^#]*))?(#(?<fragment>.*))?".toRegex()

    // Pointers in schemeSpecificPart
    private val userInfoRange: IntRange?
    private val hostRange: IntRange?
    private val pathRange: IntRange?
    private val queryRange: IntRange?
    private val fragmentRange: IntRange?

    val userinfo: String
        get() = if (userInfoRange != null) {
            schemeSpecificPart.substring(userInfoRange)
        } else if (context is CommonURL) {
            context.userinfo
        } else {
            ""
        }

    val host: String
        get() = if (hostRange != null) {
            schemeSpecificPart.substring(hostRange)
        } else if (context is CommonURL) {
            context.host
        } else {
            ""
        }


    val uriPort: Int
    val port: Int
        get() = if (uriPort > 0) uriPort else getDefaultPort()

    val path: Path

    val query: String
        get() = if (queryRange != null) schemeSpecificPart.substring(queryRange) else ""
    val fragment: String
        get() = if (fragmentRange != null) schemeSpecificPart.substring(fragmentRange) else ""


    /**
     * The default port for current scheme (http = 80, https = 443,...)
     * @return the default port
     */
    protected open fun getDefaultPort() = -1

    init {

        // Parse the scheme specific part
        val match = schemeSpecificPartPattern.matchEntire(schemeSpecificPart)
                ?: throw IllegalArgumentException("Can't parse $uriString")

        // Save pointers to userInfo, host and port
        if (match.groups["authority"]?.value.isNullOrBlank()) {
            if (context is CommonURL) {
                userInfoRange = null
                hostRange = null
                uriPort = context.uriPort
            } else {
                throw IllegalArgumentException("Can't handle relative uri $uriString without context!")
            }
        } else {
            userInfoRange = match.groups["userinfo"]?.range
            hostRange = match.groups["host"]?.range
            val strPort = match.groups["port"]?.value?.trim() ?: ""
            uriPort = if (strPort.isNotBlank()) strPort.toInt() else -1
        }

        // Save pointers to path, query and fragment
        pathRange = match.groups["path"]?.range
        queryRange = match.groups["query"]?.range
        fragmentRange = match.groups["fragment"]?.range

        // We have to resolve the full path here in constructor, to check its validness
        val foundPath = Path.parse(if (pathRange != null) schemeSpecificPart.substring(pathRange) else "")
        path = if (foundPath.isAbsolute) {
            foundPath
        } else if (context is CommonURL) {
            context.path.resolve(foundPath)
        } else {
            throw IllegalArgumentException("Can't handle relative path ${foundPath.complete} without context!")
        }
    }


    private fun toUriStringInternal(normalized: Boolean = false): String {
        // Scheme
        var result = "$scheme://"

        // User info (if exists)
        if (userinfo.isNotEmpty()) result += "$userinfo@"

        // Host (always)
        result += host

        // Port only when it's not the default port
        if (getDefaultPort() != port)
            result += ":$port"

        // Normalized path or the complete path
        result += if (normalized)
            path.normalized.complete
        else
            path.complete

        // Query
        if (query.isNotEmpty())
            result += "?$query"

        // Fragment
        if (fragment.isNotEmpty())
            result += "#$fragment"

        return result
    }

    /**
     * Full, resolved URI string
     * @return resolved Uri string
     */
    override fun toResolvedUriString(): String = toUriStringInternal(false)

    /**
     * The normalized Uri string
     * @return the normalized Uri string
     */
    open fun toNormalizedUriString(): String = toUriStringInternal(true)

    /**
     * The CommonURL object containing normalized path
     * @return the CommonURL object containing normalized path
     */
    open fun toNormalizedURL() = parse(toNormalizedUriString()) as CommonURL

}