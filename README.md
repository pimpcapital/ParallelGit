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

Checkout a branch &#8594; Make changes to the working directory &#8594; Commit changes

The standard way of modifying a repository is by changing its [working directory](https://git-scm.com/book/en/v2/Getting-Started-Git-Basics) and creating a commit. This is quite convenient for common users since a working directory is just a normal directory on your hard drive and you can use all the OS built-in file system facilities to access the contents in the directory. When you create a commit, files and directories are automatically converted into blobs and trees, which are then persisted in the [secret dot git directory](https://git-scm.com/book/en/v1/Git-Internals).

Everything is smooth and easy until you try to use Git in a server role application. A repository (by default) only has one working directory, and one working directory only has one state (checked out revision). When two users want to use one repository, the second user must wait for the first user to exit before he can safely use the repository. The working directory becomes a scarce resource which all users fight for. The hard drive becomes a major performance bottleneck as the system has to perform a [force checkout](https://git-scm.com/docs/git-checkout) every time a user enters the repository.


The goals
---------

For a Git based application to serve multiple users simultaneously it must:

1. Allow **multiple working directories** to exist at the same time
2. Be able to **create** new working directories **on demand**
3. Be able to **remove** working directories **on demand**

More importantly, the creation and removal of working directories must be **inexpensive**.


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

GitFileSystem fully supports the Java JDK 7 nio API. You can access your in-memory working directory the same way as you access a normal directory on your hard drive.

#####Create GitFileSystem
```java
File myRepo = new File("/home/repo");
GitFileSystem gfs = GitFileSystems.forRevision("my_branch", myRepo));
```

#####Close GitFileSystem
Standard *close()* method

```java
GitFileSystem gfs = ...;
gfs.close();
```

Java 7 auto-close feature

```java
try(GitFileSystem gfs = ...) {
  ...
}
```

#####Read file
Read bytes

```java
GitFileSystem gfs = ...;
Path file = gfs.getPath("/myFile.txt");
byte[] bytes = Files.readAllBytes(file);
```

Open *InputStream*

```java
GitFileSystem gfs = ...;
Path file = gfs.getPath("/myFile.txt");
InputStream input = Files.newInputStream(file);
```

#####Write file
Write bytes

```java
GitFileSystem gfs = ...;
Path file = gfs.getPath("/myFile.txt");
byte[] bytes = "my text data".getBytes();
Files.write(file, bytes);
```

Open *OutputStream*

```java
GitFileSystem gfs = ...;
Path file = gfs.getPath("/myFile.txt");
InputStream output = Files.newOutputStream(file);
```

#####Copy file
GFS &#8594; GFS

```java
GitFileSystem gfs = ...;
Path source = gfs.getPath("/source.txt");
Path target = gfs.getPath("/target.txt");
Files.copy(source, target);
```

Native file system &#8594; GFS

```java
Path source = Paths.get("/home/source.txt"); // a file on your hard drive
GitFileSystem gfs = ...;
Path target = gfs.getPath("/target.txt");
Files.copy(source, target);
```

GFS &#8594; Native file system

```java
Path target = Paths.get("/home/target.txt"); // a file on your hard drive
GitFileSystem gfs = ...;
Path source = gfs.getPath("/source.txt");
Files.copy(source, target);
```
 
#####Move/Rename file
GFS &#8594; GFS

```java
GitFileSystem gfs = ...;
Path source = gfs.getPath("/source.txt");
Path target = gfs.getPath("/target.txt");
Files.move(source, target);
```

Native file system &#8594; GFS

```java
Path source = Paths.get("/home/source.txt"); // a file on your hard drive
GitFileSystem gfs = ...;
Path target = gfs.getPath("/target.txt");
Files.move(source, target);
```

GFS &#8594; Native file system

```java
Path target = Paths.get("/home/target.txt"); // a file on your hard drive
GitFileSystem gfs = ...;
Path source = gfs.getPath("/source.txt");
Files.move(source, target);
```

#####Delete file
```java
GitFileSystem gfs = ...;
Path file = gfs.getPath("/myFile.txt");
Files.delete(file);
```

#####Create directory
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

#####Iterate directory
```java
GitFileSystem gfs = ...;
Path myDir = gfs.getPath("/myDirectory");
DirectoryStream<Path> dirStream = Files.newDirectoryStream(myDir);
for(Path child : dirStream) {
  ...
}
```

#####Commit changes
Commit with default user

```java
GitFileSystem gfs = ...;
RevCommit commit = Requests.commit(gfs).message("first commit").execute();
```

Commit with specified user

```java
GitFileSystem gfs = ...;
PersonIdent committer = new PersonIdent("my_name", "my@email.com");
RevCommit commit = Requests.commit(gfs)
                           .committer(committer)
                           .message("first commit")
                           .execute();
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