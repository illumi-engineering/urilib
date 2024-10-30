package com.oxyggen.net

abstract class ContextURI(uriString: String, uri: ContextURI?) : ResolvedURI(uriString, uri) {
    open fun resolve(uri: URI) = (when (uri) {
        is ResolvedURI -> uri
        is UnresolvedURI ->parse(uriString = uri.schemeSpecificPart, context = this)
        else -> this
    }) as ResolvedURI
}
