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
Git is an awesome data storage. Its special data structure offers many useful features such as:

* Keeping history snapshots at a very low cost
* Automatic duplication detection
* Remote backup
* Merging and conflict resolution

Git is well known and widely used as a VCS, yet few software application uses Git as a internal data storage. One of the reasons is the lack of high level API to interact with Git repository.

When Git is used in software development, the standard process to make changes to Git repository is

Checkout ==> Write file ==> Add file to index ==> Commit

While this model works sufficiently well with developers, it does not fit in the architecture diagram of a server role application. Reasons are:

* Only one branch is checked out
* Checking out a branch is a heavy I/O task as files need to be deleted and re-created
* Every context switching needs a check out

There are ways around these problems, but they usually involve manual blob and tree creations, which are verbose and error prone.

ParallelGit is a layer between the application logic and the Git repository. It abstracts away Git's low level object manipulation details and provides a friendly interface which extends the Java 7 NIO filesystem API. The filesystem itself operates in memory with data pulled from hard drive on demand. 

With ParallelGit an application can control a Git repository as it were a normal filesystem. Arbitrary branch and commit can be checked out at minimal CPU and I/O cost. Multiple filesystem instances can be hosted simultaneously with no interference.   


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