package com.beijunyi.parallelgit.runtime;

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

import com.beijunyi.parallelgit.runtime.cache.AddDirectory;
import com.beijunyi.parallelgit.runtime.cache.AddFile;
import com.beijunyi.parallelgit.runtime.cache.UpdateFile;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.utils.CommitUtils;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;

public final class ParallelCommitCommand extends CacheBasedCommand<ParallelCommitCommand, AnyObjectId> {
  private AnyObjectId revisionId;
  private String revisionIdStr;
  private String branch;
  private boolean amend;
  private boolean allowEmptyCommit;
  private AnyObjectId treeId;
  private DirCache cache;
  private RevCommit head;
  private List<AnyObjectId> parents;
  private PersonIdent author;
  private PersonIdent committer;
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
  public ParallelCommitCommand revision(@Nonnull AnyObjectId revisionId) {
    this.revisionId = revisionId;
    return this;
  }

  @Nonnull
  public ParallelCommitCommand revision(@Nonnull String revisionIdStr) {
    this.revisionIdStr = revisionIdStr;
    return this;
  }

  @Nonnull
  public ParallelCommitCommand branch(@Nonnull String branch) {
    this.branch = branch;
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
  public ParallelCommitCommand committer(@Nonnull PersonIdent committer) {
    this.committer = committer;
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
  public ParallelCommitCommand addFile(@Nonnull byte[] bytes, @Nonnull FileMode mode, @Nonnull String path) {
    AddFile editor = new AddFile(path);
    editor.setBytes(bytes);
    editor.setMode(mode);
    editors.add(editor);
    return this;
  }

  @Nonnull
  public ParallelCommitCommand addFile(@Nonnull byte[] bytes, @Nonnull String path) {
    return addFile(bytes, FileMode.REGULAR_FILE, path);
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
  public ParallelCommitCommand addFile(@Nonnull String content, @Nonnull String path) {
    return addFile(content, FileMode.REGULAR_FILE, path);
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
  public ParallelCommitCommand addFile(@Nonnull InputStream inputStream, @Nonnull String path) {
    return addFile(inputStream, FileMode.REGULAR_FILE, path);
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
  public ParallelCommitCommand addFile(@Nonnull Path sourcePath, @Nonnull String path) {
    return addFile(sourcePath, FileMode.REGULAR_FILE, path);
  }

  @Nonnull
  public ParallelCommitCommand addFile(@Nonnull File sourceFile, @Nonnull FileMode mode, @Nonnull String path) {
    AddFile editor = new AddFile(path);
    editor.setSourceFile(sourceFile);
    editor.setMode(mode);
    editors.add(editor);
    return this;
  }

  @Nonnull
  public ParallelCommitCommand addFile(@Nonnull File sourceFile, @Nonnull String path) {
    return addFile(sourceFile, FileMode.REGULAR_FILE, path);
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
  public ParallelCommitCommand updateFile(@Nonnull byte[] bytes, @Nonnull String path, boolean create) {
    UpdateFile editor = new UpdateFile(path);
    editor.setBytes(bytes);
    editor.setCreate(create);
    editors.add(editor);
    return this;
  }

  @Nonnull
  public ParallelCommitCommand updateFile(@Nonnull byte[] bytes, @Nonnull String path) {
    return updateFile(bytes, path, true);
  }

  @Nonnull
  public ParallelCommitCommand updateFile(@Nonnull String path, @Nonnull String content, boolean create) {
    UpdateFile editor = new UpdateFile(path);
    editor.setContent(content);
    editor.setCreate(create);
    editors.add(editor);
    return this;
  }

  @Nonnull
  public ParallelCommitCommand updateFile(@Nonnull String path, @Nonnull String content) {
    return updateFile(content, path, true);
  }

  @Nonnull
  public ParallelCommitCommand updateFile(@Nonnull String path, @Nonnull InputStream inputStream, boolean create) {
    UpdateFile editor = new UpdateFile(path);
    editor.setInputStream(inputStream);
    editor.setCreate(create);
    editors.add(editor);
    return this;
  }

  @Nonnull
  public ParallelCommitCommand updateFile(@Nonnull String path, @Nonnull InputStream inputStream) {
    return updateFile(path, inputStream, true);
  }

  @Nonnull
  public ParallelCommitCommand updateFile(@Nonnull String path, @Nonnull Path sourcePath, boolean create) {
    UpdateFile editor = new UpdateFile(path);
    editor.setSourcePath(sourcePath);
    editor.setCreate(create);
    editors.add(editor);
    return this;
  }

  @Nonnull
  public ParallelCommitCommand updateFile(@Nonnull String path, @Nonnull Path sourcePath) {
    return updateFile(path, sourcePath, true);
  }

  @Nonnull
  public ParallelCommitCommand updateFile(@Nonnull String path, @Nonnull File sourceFile, boolean create) {
    UpdateFile editor = new UpdateFile(path);
    editor.setSourceFile(sourceFile);
    editor.setCreate(create);
    editors.add(editor);
    return this;
  }

  @Nonnull
  public ParallelCommitCommand updateFile(@Nonnull String path, @Nonnull File sourceFile) {
    return updateFile(path, sourceFile, true);
  }

  private void prepareHead() throws IOException {
    assert repository != null;
    if(revisionId != null)
      head = CommitUtils.getCommit(revisionId, repository);
    else if(revisionIdStr != null)
      head = CommitUtils.getCommit(revisionIdStr, repository);
    else if(branch != null)
      head = CommitUtils.getCommit(branch, repository) ;
  }

  private boolean isResultTreeSpecified() {
    return treeId != null || cache != null;
  }

  private void prepareBase() throws IOException {
    if(isResultTreeSpecified() || isBaseSpecified())
      return;
    if(head != null)
      baseCommit(head);
  }

  private void prepareParents() throws IOException {
    if(parents != null)
      return;
    parents = new ArrayList<>();
    if(head != null) {
      if(amend)
        parents.addAll(Arrays.asList(head.getParents()));
      else
        parents.add(head);
    }
  }

  private void prepareCache() throws IOException {
    if(cache == null) {
      cache = buildCache();
    }
  }

  private boolean hasStagedChanges() {
    return cache != null || !editors.isEmpty();
  }

  private void prepareTree(@Nonnull ObjectInserter inserter) throws IOException {
    if(treeId == null) {
      if(!hasStagedChanges() && isBaseSpecified()) {
        assert repository != null;
        resolveBaseTree(repository);
        treeId = baseTreeId;
      } else {
        prepareCache();
        treeId = cache.writeTree(inserter);
      }
    }
  }

  private void prepareCommitter() {
    if(committer == null)
      committer = new PersonIdent(repository);
  }

  private void prepareAuthor() {
    if(author == null)
      author = committer;
  }

  private void prepareMessage() {
    if(message == null && amend && head != null)
      message = head.getFullMessage();
  }

  private boolean isDifferentTree() throws IOException {
    if(allowEmptyCommit || parents.isEmpty())
      return true;
    assert repository != null;
    return !treeId.equals(CommitUtils.getCommit(parents.get(0), repository).getTree());
  }

  private void updateBranchRef(@Nonnull AnyObjectId newCommitId) throws IOException {
    if(branch != null) {
      assert repository != null;
      RevCommit newCommit = CommitUtils.getCommit(newCommitId, repository);
      if(head == null)
        BranchUtils.initBranch(branch, newCommit, repository);
      else if(amend)
        BranchUtils.amendCommit(branch, newCommit, repository);
      else
        BranchUtils.newCommit(branch, newCommit, repository);
    }
  }

  @Nonnull
  private AnyObjectId buildCommit(@Nonnull ObjectInserter inserter) throws IOException {
    CommitBuilder builder = new CommitBuilder();
    builder.setTreeId(treeId);
    builder.setAuthor(author);
    builder.setCommitter(committer);
    builder.setMessage(message);
    builder.setParentIds(parents);
    return inserter.insert(builder);
  }

  @Nullable
  @Override
  protected AnyObjectId doCall() throws IOException {
    assert repository != null;
    try(ObjectInserter inserter = repository.newObjectInserter()) {
      prepareHead();
      prepareBase();
      prepareParents();
      prepareTree(inserter);
      prepareCommitter();
      prepareAuthor();
      prepareMessage();
      if(!isDifferentTree())
        return null;
      AnyObjectId commit = buildCommit(inserter);
      inserter.flush();
      updateBranchRef(commit);
      return commit;
    }
  }

  @Nonnull
  public static ParallelCommitCommand prepare(@Nonnull Repository repository) {
    return new ParallelCommitCommand(repository);
  }

}