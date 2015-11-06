package com.beijunyi.parallelgit.filesystem.merge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.io.DirectoryNode;
import com.beijunyi.parallelgit.filesystem.io.Node;
import com.beijunyi.parallelgit.filesystem.utils.GitFileSystemBuilder;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.merge.MergeFormatter;
import org.eclipse.jgit.merge.MergeResult;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.NameConflictTreeWalk;

import static org.eclipse.jgit.lib.Constants.*;

public class GfsMerger extends ResolveMerger {

  private final GitFileSystem gfs;

  private AbstractTreeIterator baseTree;
  private AbstractTreeIterator ourTree;
  private AbstractTreeIterator theirTree;

  private DirectoryNode currentDirectory;
  private int currentDepth;

  private String path;
  private String name;
  private int baseMode;
  private int ourMode;
  private int theirMode;
  private AnyObjectId baseId;
  private AnyObjectId ourId;
  private AnyObjectId theirId;

  public GfsMerger(@Nonnull Repository repo) throws IOException {
    super(repo);
    implicitDirCache = false;
    gfs = GitFileSystemBuilder.prepare().repository(repo).build();
    currentDirectory = gfs.getFileStore().getRoot();
    currentDepth = 0;
  }

  @Nonnull
  public GitFileSystem getGfs() {
    return gfs;
  }

  @Override
  protected boolean mergeTrees(@Nonnull AbstractTreeIterator base, @Nonnull RevTree head, @Nonnull RevTree merge, boolean ignoreConflicts) throws IOException {
    prepareIterators(base, head, merge);
    prepareTreeWalk();
    mergeTreeWalk();
    if(getUnmergedPaths().isEmpty()) {
      resultTree = (ObjectId) gfs.persist();
      return true;
    } else
      gfs.flush();
    return false;
  }

  private void prepareIterators(@Nonnull AbstractTreeIterator base, @Nonnull RevTree head, @Nonnull RevTree merge) throws IOException {
    baseTree = base;
    ourTree = makeIterator(head);
    theirTree = makeIterator(merge);
  }

  @Nonnull
  private AbstractTreeIterator makeIterator(@Nonnull AnyObjectId tree) throws IOException {
    CanonicalTreeParser p = new CanonicalTreeParser();
    p.reset(reader, tree);
    return p;
  }


  private void prepareTreeWalk() throws IOException {
    tw = new NameConflictTreeWalk(reader);
    tw.addTree(baseTree);
    tw.addTree(ourTree);
    tw.addTree(theirTree);
  }

  private void mergeTreeWalk() throws IOException {
    while(tw.next()) {
      if(mergeTreeNode() && tw.isSubtree())
        tw.enterSubtree();
    }
  }

  private boolean mergeTreeNode() throws IOException {
    readTreeNodes();
    prepareDirectory();

    if(oursIsNotChanged()) {
      applyTheirs();
      return false;
    }

    if(theirsIsNotChanged()) {
      applyOurs();
      return false;
    }

    if(bothHaveSameId()) {
      applyCommonChanges();
      return false;
    }

    if(bothAreBlob()) {
      mergeAndApplyBlob();
      return false;
    }

    if(bothAreTree()) {
      addDirectory();
      return true;
    }

    handleFileDirectoryConflict();
    return false;
  }

  private void readTreeNodes() {
    path = tw.getPathString();
    name = tw.getNameString();
    baseMode = tw.getRawMode(T_BASE);
    baseId = tw.getObjectId(T_BASE);
    ourMode = tw.getRawMode(T_OURS);
    ourId = tw.getObjectId(T_OURS);
    theirMode = tw.getRawMode(T_THEIRS);
    theirId = tw.getObjectId(T_THEIRS);
  }

  private void prepareDirectory() {
    int depth = tw.getDepth();
    while(currentDepth > depth) {
      currentDirectory = currentDirectory.getParent();
      currentDepth--;
    }
  }

  private boolean oursIsNotChanged() {
    return baseMode == ourMode && baseId.equals(ourId);
  }

  private boolean applyTheirs() {
    if(theirMode != FileMode.TYPE_MISSING)
      insertNode(theirMode, theirId);
    return false;
  }

  private boolean theirsIsNotChanged() {
    return baseMode == theirMode && baseId.equals(theirId);
  }

  private void applyOurs() {
    if(ourMode != FileMode.TYPE_MISSING)
      insertNode(ourMode, ourId);
  }

  private boolean bothHaveSameId() {
    return ourId.equals(theirId);
  }

  private void applyCommonChanges() {
    int mergedMode = mergeFileModes();
    if(mergedMode != FileMode.TYPE_MISSING)
      insertNode(mergedMode, ourId);
    else {
      unmergedPaths.add(path);
      mergeResults.put(path, new MergeResult<>(Collections.<RawText>emptyList()));
      insertNode(ourMode, ourId);
      addConflict();
    }
  }

  private int mergeFileModes() {
    int base = tw.getRawMode(T_BASE);
    int theirs = tw.getRawMode(T_THEIRS);
    int ours = tw.getRawMode(T_THEIRS);
    if (ours == theirs)
      return ours;
    if (base == ours)
      return theirs == FileMode.TYPE_MISSING ? ours : theirs;
    if (base == theirs)
      return ours == FileMode.TYPE_MISSING ? theirs : ours;
    return FileMode.TYPE_MISSING;
  }

  private boolean bothAreBlob() {
    return ourMode != FileMode.TYPE_TREE && theirMode != FileMode.TYPE_TREE;
  }

  private void mergeAndApplyBlob() throws IOException {
    if(ourMode == FileMode.TYPE_GITLINK || theirMode == FileMode.TYPE_GITLINK) {
      unmergedPaths.add(tw.getPathString());
      insertNode(ourMode, ourId);
      addConflict();
    } else {
      MergeResult<RawText> result = mergeContent();
      writeMergedFile(result);
      if(result.containsConflicts()) {
        unmergedPaths.add(path);
        mergeResults.put(path, result);
        addConflict();
      }
      modifiedFiles.add(path);
    }
  }

  @Nonnull
  private MergeResult<RawText> mergeContent() throws IOException {
    RawText baseContent = getRawText(baseId);
    RawText ourContent = getRawText(ourId);
    RawText theirContent = getRawText(theirId);
    return mergeAlgorithm.merge(RawTextComparator.DEFAULT, baseContent, ourContent, theirContent);
  }

  private void writeMergedFile(@Nonnull MergeResult<RawText> result) throws IOException {
    int mergedMode = mergeFileModes();
    FileMode mode = mergedMode == FileMode.TYPE_MISSING ? FileMode.REGULAR_FILE : FileMode.fromBits(mergedMode);

    byte[] bytes;
    try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      new MergeFormatter().formatMerge(out, result, Arrays.asList(commitNames), CHARACTER_ENCODING);
      bytes = out.toByteArray();
    }

    Node node;
    if(!result.containsConflicts())
      node = Node.forBytes(bytes, mode, currentDirectory);
    else {
      AnyObjectId blob = gfs.saveBlob(bytes);
      node = Node.forObject(blob, mode, currentDirectory);
    }

    addChild(node);
  }

  @Nonnull
  private RawText getRawText(@Nonnull AnyObjectId id) throws IOException {
    if(id.equals(ObjectId.zeroId()))
      return new RawText(new byte[0]);
    return new RawText(reader.open(id, OBJ_BLOB).getCachedBytes());
  }

  private void insertNode(int mode, @Nonnull AnyObjectId id) {
    Node node = Node.forObject(id, FileMode.fromBits(mode), currentDirectory);
    addChild(node);
  }

  private void addChild(@Nonnull Node child) {
    if(currentDirectory.getChildren() == null)
      currentDirectory.setChildren(new HashMap<String, Node>());
    currentDirectory.addChild(name, child, false);
  }

  private void addConflict() {
    //TODO: save conflict
  }

  private boolean bothAreTree() {
    return ourMode == FileMode.TYPE_TREE && theirMode == FileMode.TYPE_TREE;
  }

  private void addDirectory() {
    DirectoryNode node = DirectoryNode.newDirectory(currentDirectory);
    addChild(node);
    currentDirectory = node;
  }

  private void handleFileDirectoryConflict() {
    unmergedPaths.add(path);
    insertNode(ourMode, ourId);
  }

}
