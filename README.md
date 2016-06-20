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
Git is an awesome file storage. Its unique data structure offers many features that other file storages and databases don't have such as:
* Storing history versions at a very low cost
* Duplication detection
* Simple local and remote backup
For a server role application, it is hardly feasible to check out files into hard drive for every request. In fact, a system that serves multiple users should be using a bare repository (a normal repository without its work directory).

How would you make a system that interacts with a Git repository with no work directory? If you know Git really well, I bet you know the tricks to read a file without checking out the branch/commit. But what if you want to make some changes to a file?

Imagine you have this file in a branch:
```
/app-core/src/main/resources/com/example/config/settings.xml
```
If you want to change this file, there is more than one change you need to make to the repository. In fact, you will need to create 1 blob object, 7 tree objects, 1 commit object and update 1 branch reference. Simple things can be very verbose when you use Git's low level API to interact with a bare repository.

ParallelGit solves this problem by exposing Git repository through Java's NIO filesystem API. With ParallelGit you can instantly checkout any branch/commit to a in-memory filesystem and perform read/write accesses.


Performance explained
---------------------
Git is best at storing changes in many small batches. It is very rare to have a commit that updates all files in a repository. The size of I/O per request is usually very small compared to the total number of objects or files in a repository. Pre-loading everything into memory is usually an overkill for most requests.

To minimise I/O and memory usage, ParallelGit loads the minimum necessary objects to complete a request. Consider a scenario where you have these files in a branch:
```
├──app-core
│   └──src
│       ├──main
│       │   ├──MyFactory.java
│       │   └──MyProduct.java
│       └──test
│           └──ProductionTest.java
└──app-web
    ├──index.html
    └──style.css
```
When you check out this branch. The commit object is loaded. It has a reference to the root of this file tree.
Assuming you want to read file `/app-core/src/main/MyFactory.java`. In order to reach this file, you have to resolve its parent directories (including the root directory) i.e:
```
1) /
2) /app-core
3) /app-core/src
4) /app-core/src/main
```
The last tree object (`/app-core/src/main`) contains a reference to the blob object of `MyFactory.java`, which you can use to retrieve the content of this file.

Now let's say you want to read the file `/app-core/src/main/MyProduct.java`. This file is in the same directory as the previous one. There is no need to read the directories again as they are already in memory. This time we simply read the blob reference from `/app-core/src/main` and use it to retrieve the content of the file.

Saving files to repository follows a similar pattern. Assuming you have made a change to `MyFactory.java` and you want to commit this change. ParallelGit saves the file as a blob and creates the new tree objects from bottom-up i.e:
```
1) /app-core/src/main
2) /app-core/src
3) /app-core
4) /
```

The whole process above involved 2 out of the total 5 files in the branch, and ParallelGit only focuses on reaching the 2 files. The existence of the other 2 files has nearly zero impact to the performance. Your repository can keep on growing and your request handling time remains constant.



License
-------
This project is licensed under [Apache License, Version 2.0](http://opensource.org/licenses/apache-2.0).