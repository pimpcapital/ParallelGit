ParallelGit
===========

ParallelGit is an open source library built upon [JGit](https://eclipse.org/jgit/) to provide your Java project with easy access to your local Git repository.

[![Build Status](https://travis-ci.org/beijunyi/ParallelGit.svg?branch=master)](https://travis-ci.org/beijunyi/ParallelGit)
[![Coverage Status](https://coveralls.io/repos/beijunyi/ParallelGit/badge.svg?branch=master&service=github)](https://coveralls.io/github/beijunyi/ParallelGit?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.beijunyi.parallelgit/parallelgit/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.beijunyi.parallelgit/parallelgit)

ParallelGit Utils
-----------------

A collection of utilities to assist with developing Git IO functionality.

```xml
<dependency>
  <groupId>com.beijunyi.parallelgit</groupId>
  <artifactId>parallelgit-utils</artifactId>
  <version>0.9.3</version>
</dependency>
```

###Examples
#####Create Repository
```java
public Repository createProjectRepository() {
  File dir = new File("/home/project/repo");
  return RepositoryUtils.createRepository(dir);
}
```

#####Create Branch
```java
public void branchFromMaster(String newBranch, Repository repo) {
  BranchUtils.createBranch(newBranch, "master", repo)
}
```

#####Read File
```java
public void printFile(String filename, Repository repo) {
  byte[] blob = GitFileUtils.readFile(filename, "master", repo);
  String text = new String(blob);
  System.out.println(text);
}
```

ParallelGit FileSystem
----------------------

A Java 7 [nio filesystem](http://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html) implementation to provide the most native way to interact with a Git repository.

```xml
<dependency>
  <groupId>com.beijunyi.parallelgit</groupId>
  <artifactId>parallelgit-filesystem</artifactId>
  <version>0.9.3</version>
</dependency>
```

###Examples
#####Copy File
```java
public void copyFile(String src, Repository repo, Path dest) {
  GitFileSystem gfs = GitFileSystemBuilder.forRevision("master", repo));
  Path srcFile = gfs.getPath(src);
  Files.copy(srcFile, dest);
}
```

#####Commit Files
```java
public void copyAndCommitFile(Path src, String dest, Repository repo) {
  GitFileSystem gfs = GitFileSystemBuilder.forRevision("master", repo));
  Path destFile = gfs.getPath(dest);
  Files.copy(src, destFile);
  Requests.commit(gfs).message("copied " + src).execute();
}
```

ParallelGit Commands
--------------------

An in progress module inspired by [fluent design pattern](https://en.wikipedia.org/wiki/Fluent_interface) aiming to simplify the usages of complex Git functions such as [merge](https://git-scm.com/docs/git-merge), [rebase](https://git-scm.com/docs/git-rebase) and [cherry-pick](https://git-scm.com/docs/git-cherry-pick). 

```xml
<dependency>
  <groupId>com.beijunyi.parallelgit</groupId>
  <artifactId>parallelgit-commands</artifactId>
  <version>0.9.3</version>
</dependency>
```

Donate
------
[![Cancer Research UK](http://www.cancerresearchuk.org/sites/all/themes/custom/cruk/logo.png)](http://www.cancerresearchuk.org/support-us/donate)

If this library has helped you, consider a donation to [Cancer Research UK](http://www.cancerresearchuk.org/support-us/donate).

Thank you!


License
-------
This project is licensed under [Apache License, Version 2.0](http://opensource.org/licenses/apache-2.0).