ParallelGit
===========

ParallelGit is a high performance [Java JDK 7 nio](https://docs.oracle.com/javase/tutorial/essential/io/fileio.html) in-memory file system for Git based application.

[![Build Status](https://travis-ci.org/beijunyi/ParallelGit.svg?branch=master)](https://travis-ci.org/beijunyi/ParallelGit)
[![Coverage Status](https://coveralls.io/repos/beijunyi/ParallelGit/badge.svg?branch=master&service=github)](https://coveralls.io/github/beijunyi/ParallelGit?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.beijunyi/parallelgit/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.beijunyi/parallelgit)

For more examples and the latest news, please visit from our [official site](https://beijunyi.github.io/ParallelGit)!


The problems
------------

The common usage of Git follows this pattern.

Checkout -> Make changes to the worktree -> Commit changes

The standard way of modifying a repository is by changing its worktree and creating a commit. This is quite convenient for common users since a worktree is just a normal directory in your hard drive and you can use all the OS built-in file system facilities to access the contents in the directory. When you create a commit, files and directories are automatically converted into blobs and trees and persisted in the secret dot git directory.

Everything is smooth and easy until you try to use Git in a server role application. A normal repository only has one worktree, and one worktree only has one state (checked out revision). When two users want to use one repository, the second user must wait for the first user to exit before he can safely enter and access the repository. The worktree becomes a scarce resource which all users fight for. The hard drive becomes a major performance bottleneck as the system has to perform a checkout every time a new user enters the repository.


The goals
---------

For a Git based application to serve multiple users simultaneously it must:

1. Allow multiple worktrees to exist at the same time
2. Be able to create new worktree on demand
3. Be able to remove worktree on demand

Additionally, the creation and removal of worktree should be inexpensive.


The way we play
---------------

ParallelGit is an in-memory file system that implements Java JDK 7 nio interface. It enables you to create a GitFileSystem from an arbitrary revision. GitFileSystem stages your changes in memory instead of in your hard drive. When all the work is done, you can commit the changes straight from memory into your repository.


Get started
-----------

Add ParallelGit to your project via build tool.

Maven:

```xml
<dependency>
  <groupId>com.beijunyi</groupId>
  <artifactId>parallelgit-filesystem</artifactId>
  <version>1.0.0</version>
</dependency>
```

Gradle:

```gradle
'com.beijunyi:parallelgit-filesystem:1.0.0'
```

Examples
--------

GitFileSystem fully supports the Java JDK 7 nio API. You can access your in-memory worktree the same way as you access a normal directory in your hard drive.

###Create GitFileSystem
```java
File myRepo = new File("/home/repo");
GitFileSystem gfs = GitFileSystemBuilder.forRevision("master", myRepo));
}
```

###Close GitFileSystem
```java
GitFileSystem gfs = ...;
gfs.close();
```

###Read file
```java
GitFileSystem gfs = ...;
Path file = gfs.getPath("/myFile.txt");
byte[] bytes = Files.readAllBytes(file);
```

```java
GitFileSystem gfs = ...;
Path file = gfs.getPath("/myFile.txt");
InputStream input = Files.newInputStream(file);
```

###Write file
```java
GitFileSystem gfs = ...;
Path file = gfs.getPath("/myFile.txt");
byte[] bytes = "my text data".getBytes();
Files.write(file, bytes);
```

```java
GitFileSystem gfs = ...;
Path file = gfs.getPath("/myFile.txt");
InputStream output = Files.newOutputStream(file);
```

###Copy file
```java
GitFileSystem gfs = ...;
Path source = gfs.getPath("/source.txt");
Path target = gfs.getPath("/target.txt");
Files.write(source, target);
```
 
###Move/Rename file
```java
GitFileSystem gfs = ...;
Path source = gfs.getPath("/source.txt");
Path target = gfs.getPath("/target.txt");
Files.move(source, target);
```

###Delete file
```java
GitFileSystem gfs = ...;
Path file = gfs.getPath("/myFile.txt");
Files.delete(file);
```

###Create directory
```java
GitFileSystem gfs = ...;
Path myDir = gfs.getPath("/myDirectory");
Files.createDirectory(myDir);
```

```java
GitFileSystem gfs = ...;
Path myDir = gfs.getPath("/dir1/dir2/dir3");
Files.createDirectories(myDir);
```

###Iterate directory
```java
GitFileSystem gfs = ...;
Path myDir = gfs.getPath("/myDirectory");
DirectoryStream<Path> dirStream = Files.newDirectoryStream(myDir);
for(Path child : dirStream) {
  ...
}
```

###Commit changes
```java
GitFileSystem gfs = ...;
RevCommit commit = Requests.commit(gfs)
                           .message("my commit")
                           .execute;
```

For more documentations and examples, please visit our [official site](https://beijunyi.github.io/ParallelGit/#/examples).


Donate
------
[![Cancer Research UK](http://www.cancerresearchuk.org/sites/all/themes/custom/cruk/logo.png)](http://www.cancerresearchuk.org/support-us/donate)

If this library has helped you, consider a donation to [Cancer Research UK](http://www.cancerresearchuk.org/support-us/donate).

Thank you!


License
-------
This project is licensed under [Apache License, Version 2.0](http://opensource.org/licenses/apache-2.0).