ParallelGit
===========

A high performance [Java JDK 7 nio](https://docs.oracle.com/javase/tutorial/essential/io/fileio.html) in-memory filesystem for Git.

[![Build Status](https://travis-ci.org/beijunyi/ParallelGit.svg?branch=master)](https://travis-ci.org/beijunyi/ParallelGit)
[![Coverage Status](https://coveralls.io/repos/beijunyi/ParallelGit/badge.svg?branch=master&service=github)](https://coveralls.io/github/beijunyi/ParallelGit?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.beijunyi/parallelgit/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.beijunyi/parallelgit)


Quick start
-----------
Maven:

```xml
<dependency>
  <groupId>com.beijunyi</groupId>
  <artifactId>parallelgit-filesystem</artifactId>
  <version>2.0.0</version>
</dependency>
```

Gradle:

```gradle
'com.beijunyi:parallelgit-filesystem:2.0.0'
```


Basic usages
------------
Copy a file from repository to hard drive:
```java
public void loadSettings() throws IOException {
  try(GitFileSystem gfs = Gfs.newFileSystem("my_branch", "/project/repository")) {
    Path source = gfs.getPath("/settings.xml");
    Path target = Paths.get("/app/config/settings.xml");
    Files.copy(source, target);
  }
}
```

Copy a file to repository and commit:
```java
public void backupSettings() throws IOException {
  try(GitFileSystem gfs = Gfs.newFileSystem("my_branch", "/project/repository")) {
    Path source = Paths.get("/app/config/settings.xml");
    Path target = gfs.getPath("/settings.xml");
    Files.copy(source, target);
    Gfs.commit(gfs).message("Update settings").execute();
  }
}
```


Project purpose explained
-------------------------
When you build a server role application, you cannot afford checking out files to your hard drive every time user makes a request. In fact, you should use a bare repository (a normal repository without its work directory) when you are serving multiple users.

How would you interact with a Git repository with no work directory? If you know Git really well, I bet you know the tricks to read a file without checking out the branch/commit. But what if you want to make some changes to a file?

Imagine you have this file in a branch:
```
/app-core/src/main/resources/com/example/config/settings.xml
```
If you want to change this file, there is more than one change you need to make to the repository. In fact, you will need to create 1 blob object, 7 tree objects, 1 commit object and update 1 branch reference. Simple things can be very verbose when you use Git's low level API to interact with a bare repository.

ParallelGit solves this problem by exposing Git repository through Java's NIO filesystem API. With ParallelGit you can instantly checkout any branch/commit to a in-memory filesystem and perform read/write accesses.


License
-------
This project is licensed under [Apache License, Version 2.0](http://opensource.org/licenses/apache-2.0).