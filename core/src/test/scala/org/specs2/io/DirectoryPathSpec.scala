package org.specs2
package io

import java.io.File
import java.net.URI

class DirectoryPathSpec extends Specification { def is = s2"""

 Paths are of 2 sorts:

  - directory paths
  - file paths

 The essential difference is only a Directory path can be appended with a path name and become a FilePath.

 Directory Paths
 ===============

 A DirectoryPath can be created from
   a String
   ${ DirectoryPath.unsafe("hello/world").path === "hello/world"  }
   a File
   ${ DirectoryPath.unsafe(new File("hello/world")).path === "hello/world"  }
   a URI
   ${ DirectoryPath.unsafe(new URI("hello/world")).path === "hello/world"  }

 The DirectoryPath loses its scheme if created from a string/file/uri
   ${ DirectoryPath.unsafe(new URI("file://hello/world")).path === "hello/world"  }

 Basic operations can be executed on a DirectoryPath
   get the parent
   ${ DirectoryPath.ROOT.parent must beNone }
   ${ DirectoryPath("test").parent must beSome(DirectoryPath.ROOT) }
   ${ ("test" </> "hello" </> "world").parent must beSome("test" </> "hello") }

   get the basename
   ${ ("test" </> "hello" </> "world").name === FileName.unsafe("world") }

   get the rootname
   ${ ("test" </> "hello" </> "world").root must_== DirectoryPath("test") }

   get the path as a string
   ${ DirectoryPath.ROOT.path must_== "" }
   ${ DirectoryPath("test").path must_== "test" }
   ${ ("test" </> "hello" </> "world").path must_== "test/hello/world" }

   get the path as a string, with a last slash
   ${ DirectoryPath.ROOT.dirPath must_== "/" }
   ${ DirectoryPath("test").dirPath must_== "test/" }
   ${ ("test" </> "hello" </> "world").dirPath must_== "test/hello/world/" }

   get a portion of the path
   ${ ("test" </> "hello" </> "world" </> "eric").relativeTo("test" </> "hello")  === "world" </> "eric" }
   ${ ("test" </> "hello" </> "world" </> "eric").relativeTo("other" </> "hello") === "test" </> "hello" </> "world" </> "eric" }
   ${ ("test" </> "hello" </> "world").fromRoot === "hello" </> "world" }

 FilePaths
 =========

 A FilePath can be created from
   a String
   ${ FilePath.unsafe("hello/world").path === "hello/world"  }
   a File
   ${ FilePath.unsafe(new File("hello/world")).path === "hello/world" }
   a URI
   ${ FilePath.unsafe(new URI("hello/world")).path === "hello/world"  }

   get the path as a string
   ${ FilePath("test").path must_== "test" }
   ${ ("test" </> "hello" <|> "world").path must_== "test/hello/world" }

"""
}
