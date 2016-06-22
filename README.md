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

Git is well known and widely used as a VCS, yet few software application uses Git as a internal data storage. One of the reasons is the lack of high level API to efficiently communicate with a Git repository.

Consider the workflow in software development, the standard steps to make changes to Git repository are:

```
Checkout (branch/commit) ==> Write file ==> Add file to index ==> Commit
```

While this model works sufficiently well with developers, it does not fit in the architecture diagram of a server role application. Reasons are:

* Only one branch can be checked out at a time
* Checking out a branch is a heavy I/O task as files need to be deleted and re-created on hard drive
* Every context switching needs a check out

There are ways around these problems, but they usually involve manual blob and tree creations, which are verbose and error prone.

ParallelGit is a layer between application logic and Git. It abstracts away Git's low level object manipulation details and provides a friendly interface which extends the Java 7 NIO filesystem API. The filesystem itself operates in memory with data pulled from hard drive on demand. 

With ParallelGit an application can control a Git repository as it were a normal filesystem. Arbitrary branch and commit can be checked out at the minimal CPU and I/O cost. Multiple filesystem instances can be hosted simultaneously with no interference.   


Performance explained
---------------------
Git is best at storing changes in many small batches. It is very rare to have a commit that updates all files in a repository. The size of I/O per request is usually very small compared to the size of the repository. Pre-loading everything into memory is usually an overkill for most tasks.

To minimise I/O and memory usage, ParallelGit loads the minimum necessary data to complete a request. Consider a scenario where a branch has the file tree below:
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
When this branch is checked out. The information of its head commit is loaded into memory. That includes the author and committer details, the commit message and the reference to the root node of this file tree. This reference is a 40-char hash, which can be used to find the tree object representing the root directory. 

Assuming the task requires the content of `/app-core/src/main/MyFactory.java`, before this file can be reached, its parent directories (including the root directory) need to be resolved i.e:
```
1) /
2) /app-core
3) /app-core/src
4) /app-core/src/main
```
The last directory (`/app-core/src/main`) represented by a Git tree object has the reference to the blob of `MyFactory.java`. This reference is another 40-char hash value that can be used to find the byte array data of this file in constant time.

Now let's say you want to read the file `/app-core/src/main/MyProduct.java`. This file is in the same directory as the previous one. There is no need to read the directories again as they are already in memory. This time we simply read the blob reference from `/app-core/src/main` and use it to retrieve the content of the file.

Saving files to repository follows a similar pattern. Assuming you have made a change to `MyFactory.java` and you want to commit this change. ParallelGit saves the file as a blob and creates the new tree objects from bottom-up i.e:
```
1) /app-core/src/main
2) /app-core/src
3) /app-core
4) /
```

The whole process above involved 2 out of the total 5 files in the branch, and ParallelGit only focuses on reaching the 2 files. The existence of the other 3 files causes (nearly) zero impact to the performance. Your repository can keep on growing and your request handling time remains constant.


Advanced features
-----------------
#### Merge
```java
public void mergeFeatureBranch() throws IOException {
  try(GitFileSystem gfs = Gfs.newFileSystem("master", "/project/repository")) {
    GfsMerge.Result result = Gfs.merge(gfs).source("feature_branch").execute();
    assert result.isSuccessful();
  }
}
```

#### Conflict resolution
```java
// a magical method that can resolve any conflicts
public abstract void resolveConflicts(GitFileSystem gfs, Map<String, MergeConflict> conflicts);

public void mergeFeatureBranch() throws IOException {
  try(GitFileSystem gfs = Gfs.newFileSystem("master", "/project/repository")) {
    GfsMerge.Result result = Gfs.merge(gfs).source("feature_branch").execute();
    assert result.getStatus() == GfsMerge.Status.CONFLICTING;
      
    resolveConflicts(gfs, result.getConflicts());
    Gfs.commit(gfs).execute();
  }
}
```

#### Create stash
```java
// a magical method that does very interesting work
public abstract void doSomeWork(GitFileSystem gfs);

public void stashIncompleteWork() throws IOException {
  try(GitFileSystem gfs = Gfs.newFileSystem("master", "/project/repository")) {
    doSomeWork(gfs);
    Gfs.createStash(gfs).execute();
  }
}
```

#### Apply stash
```java
// a magical method that does very interesting work
public abstract void doSomeMoreWork(GitFileSystem gfs);

public void continuePreviousWork() throws IOException {
  try(GitFileSystem gfs = Gfs.newFileSystem("master", "/project/repository")) {
    Gfs.applyStash(gfs)
       .stash(0)  // (optional) to specify the index of the stash to apply 
       .execute();
    doSomeMoreWork(gfs);
  }
}
```

#### Reset
```java
// a magical method that always makes bad changes the first time
public abstract void doSomeWork(GitFileSystem gfs);

public void doSomeGoodWork() throws IOException {
  try(GitFileSystem gfs = Gfs.newFileSystem("master", "/project/repository")) {
    doSomeWork(gfs);
    Gfs.reset(gfs).execute();
    doSomeWork(gfs);
  }
}
```


Handy Utils
-----------
Package `com.beijunyi.parallelgit.utils` has a collection of utility classes to perform common Git tasks. 

1. **BlobUtils** - *Blob insertion, byte array retrieval*
2. **BranchUtils** - *Branch creation, branch head reference update*
3. **CacheUtils** - *Index cache manipulation*
4. **CommitUtils** - *Commit creation, commit history retrieval*
5. **GitFileUtils** - *Shortcuts for readonly file accesses*
6. **RefUtils** - *Ref name normalisation, Ref-log retrieval*
7. **RepositoryUtils** - *Repository creation, repository settings*
8. **StashUtils** - *Stash manipulation*
9. **TagUtils** - *Tag manipulation*
10. **TreeUtils** - *Tree insertion, tree/subtree retrieval*




License
-------
This project is licensed under [Apache License, Version 2.0](http://opensource.org/licenses/apache-2.0).