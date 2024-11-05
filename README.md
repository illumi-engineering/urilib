[![Frotting.services Maven](https://maven.frotting.services/api/badge/latest/releases/com/oxyggen/net/urilib/?color=7F52FF)](https://maven.frotting.services/#/releases/com/oxyggen/net/urilib)

# Urilib
Path and URI (URL) parsing library for Kotlin originally made by Oxyggen. The
project has since been continued by [Illumi](https://illumi.sh) as the original
author has stopped working on it.

This library was primarily created for the **c4k** web crawling framework, but
it has been published as a standalone library.

## Installation
Add the following to your `build.gradle.kts` file:
```kts
repositories {
    maven { 
        name = "frotting-services"
        url = uri("https://maven.frotting.services/repos")
    }
}

dependencies {
    implementation("com.oxyggen:urilib:{version}")
}
```

## Paths

The path parsing part is (probably) finished. 
To create a Path object call the `Path.parse` method:

```kt
val p = Path.parse("/first/second/third/../fourth/")
```  
 
You can set an optional parameter `pathSeparator`. The default value is `"/"` (Linux, URL, etc...). 
Set value `"\\"` if you want to parse Windows style paths:

```kt
val w = Path.parse("C:\\temp\\abc.txt", pathSeparator = "\\")
```

Because it is designed to parse URL paths, a few simple rules were introduced:
1) directory is the substring before last separator and file is the substring 
after the last separator
2) path `""` is the same as `"/"`
3) file name and extension separator is `"."` 

I had to introduce rules 1) and 3) because these are not real or local paths, so
it's not possible to check whether given path points to a file or a directory. 
Always add a separator at the end, if the path points to directory! So 
`/dev/etc/` is directory, but `/dev/etc` is file.
 
### Examples
#### Parsing

```kt
val p = Path.parse("/first/second/third/../fourth/myfile.html")
```

| property          | value                                         |
|-------------------|-----------------------------------------------|
| p.complete        | "/first/second/third/../fourth/myfile.html"   |
| p.file            | "myfile.html"                                 |
| p.fileName        | "myfile"                                      |
| p.fileExtension   | "html"                                        |
| p.directory       | "/first/second/third/../fourth/"              |

#### Normalization
Let's normalize this path and check the values:

```kt
val n = p.normalized
```

| property          | value                                         |
|-------------------|-----------------------------------------------|
| n.complete        | **"/first/second/fourth/myfile.html"**        |
| n.file            | "myfile.html"                                 |
| n.fileName        | "myfile"                                      |
| n.fileExtension   | "html"                                        |
| n.directory       | **"/first/second/fourth/"**                   |

Normalized path is a subclass of `Path`, so it's easy to check whether path
object is normalized:

```kt
if (p is NormalizedPath) { /* ... */ }
``` 

This does not mean that Path object can't contain normalized path, but you can
be sure that NormalizedPath object **must** contain normalized path.

#### Resolving relative paths

You can also resolve relative paths. Let's create relative `r` path and resolve
it to absolute `a` using the original path `p`. Check also the normalized values
from `a` (`a.normalized`):

```kt
val r = Path.parse("../anotherfile.html")
val a = p.resolve(r)
```
Result:

| property      | a.(property)                                       | a.normalized.(property)         |
|---------------|----------------------------------------------------|---------------------------------|                                                                           
| complete      | "/first/second/third/../fourth/../anotherfile.php" | "/first/second/anotherfile.php" |
| file          | "anotherfile.php"                                  | "anotherfile.php"               |
| fileName      | "anotherfile"                                      | "anotherfile"                   |
| fileExtension | "php"                                              | "php"                           |
| directory     | "/first/second/third/../fourth/../"                | "/first/second/"                |

## URI and URL...

To parse URI and create URI object call method:

```kt
val u = URI.parse("http://test.com")
```

This is the type hierarchy:

```
 URI
 ├── UnresolvedURI       (partial URI -> no scheme specified)
 └── ResolvedURI         (complete URI -> scheme & scheme specific part specified)
     ├── MailtoURI       (implemented, but not complete)
     └── ContextURI
         └── URL
             └── CommonURL
                 ├── HttpURL
                 └── FtpURL (not yet implemented)
```

As you can see the `URI` class has 2 subclasses: `UnresolvedURI` and 
`ResolvedURI`. The `UnresolvedURI` is a relative URI, which is not complete. It
can be resolved in a context, so each subclass of the class `ContextURI` 
implements a method parse. Using this method you can convert an `UnresolvedURI`
to `ResolvedURI` (the runtime class will be, for example, `HttpURL`).

After parsing, you can test the uri type:
```kt
 val u = URI.parse("http://test.com")
 if (u is HttpURL) { /* ... */ }
```

There are few methods to convert URI to string (depends on class hierarchy):

```
toString()              -> returns object info: Object@xxx (Uri)
toUriString()           -> returns the original URI (in case of relative URI: ./index.html)
toResolvedUriString()   -> returns resolved URI (./index.html in context test.com is test.com/index.html)
toNormalizedUriString() -> returns normalized, resolved URI string 
```

## License
This project is licensed under the [MIT License](LICENSE).