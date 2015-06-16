package com.beijunyi.parallelgit.command;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.command.cache.AddDirectory;
import com.beijunyi.parallelgit.command.cache.AddFile;
import com.beijunyi.parallelgit.util.BranchHelper;
import com.beijunyi.parallelgit.util.CommitHelper;
import com.beijunyi.parallelgit.util.RevTreeHelper;
import com.beijunyi.parallelgit.util.exception.RefUpdateValidator;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;

public final class ParallelCommitCommand extends CacheBasedCommand<ParallelCommitCommand, ObjectId> {
  private String branch;
  private boolean orphan;
  private boolean amend;
  private boolean allowEmptyCommit;
  private boolean fromScratch;
  private AnyObjectId treeId;
  private DirCache cache;
  private RevCommit head;
  private List<AnyObjectId> parents;
  private PersonIdent author;
  private String authorName;
  private String authorEmail;
  private PersonIdent committer;
  private String committerName;
  private String committerEmail;
  private String message;

  private ParallelCommitCommand(@Nonnull Repository repository) {
    super(repository);
  }

  @Nonnull
  @Override
  protected ParallelCommitCommand self() {
    return this;
  }

  @Nonnull
  public ParallelCommitCommand branch(@Nonnull String branch) {
    this.branch = branch;
    return this;
  }

  @Nonnull
  public ParallelCommitCommand orphan(boolean orphan) {
    this.orphan = orphan;
    return this;
  }

  @Nonnull
  public ParallelCommitCommand amend(boolean amend) {
    this.amend = amend;
    return this;
  }

  @Nonnull
  public ParallelCommitCommand allowEmptyCommit(boolean allowEmptyCommit) {
    this.allowEmptyCommit = allowEmptyCommit;
    return this;
  }

  @Nonnull
  public ParallelCommitCommand fromScratch(boolean fromScratch) {
    this.fromScratch = fromScratch;
    return this;
  }

  @Nonnull
  public ParallelCommitCommand withTree(@Nonnull AnyObjectId treeId) {
    this.treeId = treeId;
    return this;
  }

  @Nonnull
  public ParallelCommitCommand fromCache(@Nonnull DirCache cache) {
    this.cache = cache;
    return this;
  }

  @Nonnull
  public ParallelCommitCommand author(@Nonnull PersonIdent author) {
    this.author = author;
    return this;
  }

  @Nonnull
  public ParallelCommitCommand author(@Nonnull String authorName, @Nonnull String authorEmail) {
    this.authorName = authorName;
    this.authorEmail = authorEmail;
    return this;
  }

  @Nonnull
  public ParallelCommitCommand committer(@Nonnull PersonIdent committer) {
    this.committer = committer;
    return this;
  }

  @Nonnull
  public ParallelCommitCommand committer(@Nonnull String committerName, @Nonnull String committerEmail) {
    this.committerName = committerName;
    this.committerEmail = committerEmail;
    return this;
  }

  @Nonnull
  public ParallelCommitCommand message(@Nonnull String message) {
    this.message = message;
    return this;
  }

  @Nonnull
  public ParallelCommitCommand parents(@Nonnull List<AnyObjectId> parents) {
    this.parents = parents;
    return this;
  }

  @Nonnull
  public ParallelCommitCommand parents(@Nonnull AnyObjectId... parents) {
    return parents(Arrays.asList(parents));
  }

  @Nonnull
  public ParallelCommitCommand addFile(@Nonnull byte[] bytes, @Nonnull String path) {
    AddFile editor = new AddFile(path);
    editor.setBytes(bytes);
    editors.add(editor);
    return this;
  }

  @Nonnull
  public ParallelCommitCommand addFile(@Nonnull byte[] bytes, @Nonnull FileMode mode, @Nonnull String path) {
    AddFile editor = new AddFile(path);
    editor.setBytes(bytes);
    editor.setMode(mode);
    editors.add(editor);
    return this;
  }

  @Nonnull
  public ParallelCommitCommand addFile(@Nonnull String content, @Nonnull String path) {
    AddFile editor = new AddFile(path);
    editor.setContent(content);
    editors.add(editor);
    return this;
  }

  @Nonnull
  public ParallelCommitCommand addFile(@Nonnull String content, @Nonnull FileMode mode, @Nonnull String path) {
    AddFile editor = new AddFile(path);
    editor.setContent(content);
    editor.setMode(mode);
    editors.add(editor);
    return this;
  }

  @Nonnull
  public ParallelCommitCommand addFile(@Nonnull InputStream inputStream, @Nonnull String path) {
    AddFile editor = new AddFile(path);
    editor.setInputStream(inputStream);
    editors.add(editor);
    return this;
  }

  @Nonnull
  public ParallelCommitCommand addFile(@Nonnull InputStream inputStream, @Nonnull FileMode mode, @Nonnull String path) {
    AddFile editor = new AddFile(path);
    editor.setInputStream(inputStream);
    editor.setMode(mode);
    editors.add(editor);
    return this;
  }

  @Nonnull
  public ParallelCommitCommand addFile(@Nonnull Path sourcePath, @Nonnull String path) {
    AddFile editor = new AddFile(path);
    editor.setSourcePath(sourcePath);
    editors.add(editor);
    return this;
  }

  @Nonnull
  public ParallelCommitCommand addFile(@Nonnull Path sourcePath, @Nonnull FileMode mode, @Nonnull String path) {
    AddFile editor = new AddFile(path);
    editor.setSourcePath(sourcePath);
    editor.setMode(mode);
    editors.add(editor);
    return this;
  }

  @Nonnull
  public ParallelCommitCommand addFile(@Nonnull File sourceFile, @Nonnull String path) {
    AddFile editor = new AddFile(path);
    editor.setSourceFile(sourceFile);
    editors.add(editor);
    return this;
  }

  @Nonnull
  public ParallelCommitCommand addFile(@Nonnull File sourceFile, @Nonnull FileMode mode, @Nonnull String path) {
    AddFile editor = new AddFile(path);
    editor.setSourceFile(sourceFile);
    editors.add(editor);
    return this;
  }

  @Nonnull
  public ParallelCommitCommand deleteFile(@Nonnull String path) {
    return deleteBlob(path);
  }

  @Nonnull
  public ParallelCommitCommand addDirectory(@Nonnull DirectoryStream<Path> directoryStream, @Nonnull String path) {
    AddDirectory editor = new AddDirectory(path);
    editor.setDirectoryStream(directoryStream);
    editors.add(editor);
    return this;
  }

  @Nonnull
  public ParallelCommitCommand addDirectory(@Nonnull Path sourcePath, @Nonnull String path) {
    AddDirectory editor = new AddDirectory(path);
    editor.setSourcePath(sourcePath);
    editors.add(editor);
    return this;
  }

  @Nonnull
  public ParallelCommitCommand addDirectory(@Nonnull File sourceFile, @Nonnull String path) {
    AddDirectory editor = new AddDirectory(path);
    editor.setSourceFile(sourceFile);
    editors.add(editor);
    return this;
  }

  @Nonnull
  public ParallelCommitCommand deleteDirectory(@Nonnull String path) {
    return deleteTree(path);
  }

  @Nonnull
  public ParallelCommitCommand updateFile(@Nonnull File sourceFile, @Nonnull String path, boolean create) {
    return this;
  }

  @Nonnull
  public ParallelCommitCommand updateFile(@Nonnull File sourceFile, @Nonnull String path) {
    return updateFile(sourceFile, path, true);
  }

  @Nonnull
  public ParallelCommitCommand updateFile(@Nonnull Path sourcePath, @Nonnull String path, boolean create) {
    return this;
  }

  @Nonnull
  public ParallelCommitCommand updateFile(@Nonnull Path sourcePath, @Nonnull String path) {
    return updateFile(sourcePath, path, true);
  }

  @Nonnull
  public ParallelCommitCommand updateFile(@Nonnull InputStream inputStream, @Nonnull String path, boolean create) {
    return this;
  }

  @Nonnull
  public ParallelCommitCommand updateFile(@Nonnull InputStream inputStream, @Nonnull String path) {
    return updateFile(inputStream, path, true);
  }

  @Nonnull
  public ParallelCommitCommand updateFile(@Nonnull byte[] bytes, @Nonnull String path, boolean create) {
    return this;
  }

  @Nonnull
  public ParallelCommitCommand updateFile(@Nonnull byte[] bytes, @Nonnull String path) {
    return updateFile(bytes, path, true);
  }

  @Nonnull
  public ParallelCommitCommand updateFile(@Nonnull String content, @Nonnull String path, boolean create) {
    return this;
  }

  @Nonnull
  public ParallelCommitCommand updateFile(@Nonnull String content, @Nonnull String path) {
    return updateFile(content, path, true);
  }

  private void prepareHead() throws IOException {
    assert repository != null;
    head = branch != null ? CommitHelper.getCommit(repository, branch) : null;
  }

  private void prepareParents() throws IOException {
    if(parents != null)
      return;
    parents = new ArrayList<>();
    if(orphan)
      return;
    if(branch != null) {
      assert repository != null;
      if(head != null) {
        if(amend)
          parents.addAll(Arrays.asList(head.getParents()));
        else
          parents.add(head);
      }
    }
  }

  private void prepareBase() throws IOException {
    if(treeId == null && cache == null && !fromScratch) {
      if(amend) {
        if(editors.isEmpty())
          treeId = head.getTree();
        else
          baseCommit(head);
      } else {
        if(editors.isEmpty()) {
          if(!allowEmptyCommit)
            throw new IllegalArgumentException("Nothing to commit");
          assert repository != null;
          treeId = !parents.isEmpty() ? RevTreeHelper.getRootTree(repository, parents.get(0)) : null;
        } else if(!parents.isEmpty())
          baseCommit(parents.get(0));
      }
    }
  }

  private void prepareCache() throws IOException {
    if(cache == null) {
      cache = buildCache();
    }
  }

  private void prepareTree(@Nonnull ObjectInserter inserter) throws IOException {
    if(treeId == null) {
      prepareCache();
      treeId = cache.writeTree(inserter);
    }
  }

  private void prepareCommitter() {
    if(committer == null) {
      if(committerName != null && committerEmail != null)
        committer = new PersonIdent(committerName, committerEmail);
      else {
        assert repository != null;
        committer = new PersonIdent(repository);
      }
    }
  }

  private void prepareAuthor() {
    if(author == null) {
      if(authorName != null && authorEmail != null)
        author = new PersonIdent(authorName, authorEmail);
      else
        author = committer;
    }
  }

  private void prepareMessage() {
    if(message == null && amend && head != null)
      message = head.getFullMessage();
  }

  private boolean isDifferentTree() throws IOException {
    if(allowEmptyCommit || parents.isEmpty())
      return true;
    assert repository != null;
    return !treeId.equals(CommitHelper.getCommit(repository, parents.get(0)).getTree());
  }

  private void updateBranchRef(@Nonnull AnyObjectId newCommitId) throws IOException {
    if(branch != null) {
      assert repository != null;
      RevCommit newCommit = CommitHelper.getCommit(repository, newCommitId);
      RefUpdate.Result result;
      if(head == null)
        result = BranchHelper.initBranchHead(repository, branch, newCommit);
      else if(amend)
        result = BranchHelper.amendBranchHead(repository, branch, newCommit);
      else
        result = BranchHelper.commitBranchHead(repository, branch, newCommit);
      RefUpdateValidator.validate(branch, result);
    }
  }

  @Nullable
  @Override
  public ObjectId doCall() throws IOException {
    assert repository != null;
    ObjectInserter inserter = repository.newObjectInserter();
    try {
      prepareHead();
      prepareParents();
      prepareBase();
      prepareTree(inserter);
      prepareCommitter();
      prepareAuthor();
      prepareMessage();
      if(!isDifferentTree())
        return null;
      ObjectId commit = CommitHelper.createCommit(inserter, treeId, author, committer, message, parents);
      inserter.flush();
      updateBranchRef(commit);
      return commit;
    } finally {
      inserter.release();
    }
  }

  @Nonnull
  public static ParallelCommitCommand prepare(@Nonnull Repository repository) {
    return new ParallelCommitCommand(repository);
  }

}