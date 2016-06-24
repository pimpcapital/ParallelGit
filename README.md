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
**Read** - Copy a file from repository to hard drive:
```java
public void loadSettings() throws IOException {
  try(GitFileSystem gfs = Gfs.newFileSystem("my_branch", "/project/repository")) {
    Path source = gfs.getPath("/settings.xml"); // repo
    Path target = Paths.get("/app/config/settings.xml"); // hard drive
    Files.copy(source, target);
  }
}
```

**Write** - Copy a file to repository and commit:
```java
public void backupSettings() throws IOException {
  try(GitFileSystem gfs = Gfs.newFileSystem("my_branch", "/project/repository")) {
    Path source = Paths.get("/app/config/settings.xml"); // hard drive
    Path target = gfs.getPath("/settings.xml"); // repo
    Files.copy(source, target);
    Gfs.commit(gfs).message("Update settings").execute();
  }
}
```


Project purpose explained
-------------------------
Git is a unique type of data storage. Its special data structure offers many useful features such as:

* Keeping history snapshots at a very low cost
* Automatic duplication detection
* Remote backup
* Merging and conflict resolution

Git is well known and widely used as a VCS, yet few software application uses Git as a internal data storage. One of the reasons is the lack of high level API that allows efficient communication with Git repository.

Consider the workflow in software development, the standard steps to make changes in Git are:

```
Checkout (branch/commit) ⇒ Write file ⇒ Add file to index ⇒ Commit
```

While this model works sufficiently well with developers, it does not fit in the architecture diagram of a server role application. Reasons are:

* Only one branch can be checked out at a time
* Checking out a branch is a heavy I/O task as files need to be deleted and re-created on hard drive
* Every context switching needs a check out

There are ways around these problems, but they usually involve manual blob and tree creations, which are verbose and error prone.

ParallelGit is a layer between application logic and Git. It abstracts away Git's low level object manipulation details and provides a friendly interface which extends the Java 7 NIO filesystem API. The filesystem itself operates in memory with data pulled from hard drive on demand. 

With ParallelGit an application can control a Git repository as it were a normal filesystem. Arbitrary branch and commit can be checked out at the minimal resource cost. Multiple filesystem instances can be hosted simultaneously with no interference.   


I/O & performance explained
---------------------------
Like with any data storage, the size of a single request is usually very small compared to the total size of the repository. Pre-loading everything into memory is an overkill in most scenarios.

To minimise I/O and memory usage, ParallelGit adopts the lazy loading strategy by only pulling the necessary data from hard drive for every request.

#### Read requests

Imagine a branch with the below file tree in its `HEAD` commit and a task is to read the 3 `.java` files from this branch.
```
 /
 ├──app-core
 │   └──src
 │       ├──main
 │       │   ├──MyFactory.java *(to read)
 │       │   └──MyProduct.java *(to read)
 │       └──test
 │           └──ProductionTest.java *(to read)
 └──app-web
     ├──index.jsp
     └──style.css
```
Directories and files are stored as tree and blob objects in Git. Every tree object has the references to its children nodes.

When the branch is checked out, its `HEAD` commit is parsed and stored in memory. The commit object has the reference to the tree object that corresponds to the root directory. 

To read file `/app-core/src/main/MyFactory.java`, ParallelGit needs to resolve its parent directories recursively i.e:
```
1) /
2) /app-core
3) /app-core/src
4) /app-core/src/main
```
After the last tree object is loaded and parsed, ParallelGit finds the blob object of `MyFactory.java`, which can be then converted into a `byte[]` or `String` according to the requirement details.

The second file, `/app-core/src/main/MyProduct.java`, lives in the same directory. As the required tree objects for this request are already available in memory, ParallelGit simply finds the blob reference from its immediate parent and retrieves the data.

The last file, `/app-core/src/test/Production.java`, shares a common ancestor, `/app-core/src`, with the previous two files. Starting from this node ParallelGit pulls its other child `/app-core/src/test` from Git and then resolves `Production.java`.

#### Write requests

In the same branch, assume a follow up task to change to `MyFactory.java` and commit the changes.
```
 /
 ├──app-core
 │   └──src
 │       ├──main
 │       │   ├──MyFactory.java *(to update)
 │       │   └──MyProduct.java
 │       └──test
 │           └──ProductionTest.java
 └──app-web
     ├──index.jsp
     └──style.css
```
Because all object references in Git are the hash values of their contents, whenever a file's content is changed, its hash reference changes and so do their parent directories'.

All changes are staged in memory before committed to repository. Hence, there is no write access from ParallelGit to hard drive when the file is being updated.

When `Gfs.commit(...).execute()` is called, ParallelGit creates a blob object for the updated content and the necessary tree objects to make this blob reachable i.e: 
```
1) /app-core/src/main
2) /app-core/src
3) /app-core
4) /
```
After the root tree object is created, ParallelGit creates a commit and makes it the new `HEAD` of the branch.  

#### Complexity

The important property in the performance aspect is the size of the repository has little impact on individual task's runtime and memory foot print. The resource usage per task is predominantly decided by the number and the sizes of the files in the task scope.
  
However, it would not be correct to conclude that time and memory complexity are linear to request size as there are overheads generated at different stages. One worth mentioning overhead comes from the siblings of the involved nodes. Each sibling increases the size of the tree object of the parent.


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
2. **BranchUtils** - *Branch creation, branch `HEAD` reference update*
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