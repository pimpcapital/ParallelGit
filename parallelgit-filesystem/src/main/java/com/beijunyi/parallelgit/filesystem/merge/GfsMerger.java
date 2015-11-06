package com.beijunyi.parallelgit.filesystem.merge;

import java.io.IOException;
import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.io.DirectoryNode;
import com.beijunyi.parallelgit.filesystem.io.Node;
import com.beijunyi.parallelgit.filesystem.utils.GitFileSystemBuilder;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.merge.MergeResult;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.NameConflictTreeWalk;

import static org.eclipse.jgit.lib.Constants.OBJ_BLOB;

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
    return getUnmergedPaths().isEmpty();
  }

  protected void mergeTreeWalk() throws IOException {
    while(tw.next()) {
      if(mergeTreeNode() && tw.isSubtree())
        tw.enterSubtree();
    }
  }

  public boolean mergeTreeNode() throws IOException {
    readTreeNodes();
    prepareDirectory();

    if(oursIsNotChanged())
      applyTheirs();
    else if(theirsIsNotChanged())
      applyOurs();
    else if(bothHaveSameBlob())
      applyCommonBlob();
    else if(bothHaveBlob())
      mergeAndApplyBlob();
    else {
      return true;
    }
    return false;
  }


  @Nonnull
  private AbstractTreeIterator makeIterator(@Nonnull AnyObjectId tree) throws IOException {
    CanonicalTreeParser p = new CanonicalTreeParser();
    p.reset(reader, tree);
    return p;
  }

  private void prepareIterators(@Nonnull AbstractTreeIterator base, @Nonnull RevTree head, @Nonnull RevTree merge) throws IOException {
    baseTree = base;
    ourTree = makeIterator(head);
    theirTree = makeIterator(merge);
  }

  private void prepareTreeWalk() throws IOException {
    tw = new NameConflictTreeWalk(reader);
    tw.addTree(baseTree);
    tw.addTree(ourTree);
    tw.addTree(theirTree);
  }

  private int getMode(@Nullable AbstractTreeIterator iterator) {
    return iterator != null ? iterator.getEntryRawMode() : FileMode.TYPE_MISSING;
  }

  private boolean isNonTree(int mode) {
    return FileMode.TYPE_MISSING != mode && FileMode.TYPE_TREE != mode;
  }

  private void readTreeNodes() {
    path = tw.getPathString();
    name = tw.getNameString();
    baseMode = baseTree.getEntryRawMode();
    baseId = baseMode != FileMode.TYPE_MISSING ? baseTree.getEntryObjectId() : ObjectId.zeroId();
    ourMode = ourTree.getEntryRawMode();
    ourId = ourMode != FileMode.TYPE_MISSING ? ourTree.getEntryObjectId() : ObjectId.zeroId();
    theirMode = theirTree.getEntryRawMode();
    theirId = theirMode != FileMode.TYPE_MISSING ? theirTree.getEntryObjectId() : ObjectId.zeroId();
  }

  private boolean oursIsNotChanged() {
    return baseMode == ourMode && baseId.equals(ourId);
  }

  private boolean theirsIsNotChanged() {
    return baseMode == theirMode && baseId.equals(theirId);
  }

  private boolean bothHaveSameBlob() {
    return ourId.equals(theirId);
  }

  private boolean bothHaveBlob() {
    return ourMode == FileMode.TYPE_TREE || theirMode == FileMode.TYPE_TREE;
  }

  private void applyTheirs() {
    if(theirMode != FileMode.TYPE_MISSING)
      addNode(name, theirMode, theirId);
  }
  
  private void applyOurs() {
    if(ourMode != FileMode.TYPE_MISSING)
      addNode(name, ourMode, ourId);
  }

  private void applyCommonBlob() {
    int mergedMode = mergeFileModes();
    if(mergedMode != FileMode.TYPE_MISSING)
      addNode(name, mergedMode, ourId);
    else {
      unmergedPaths.add(path);
      mergeResults.put(path, new MergeResult<>(Collections.<RawText>emptyList()));
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

  private void mergeAndApplyBlob() throws IOException {
    if(ourMode == FileMode.TYPE_GITLINK || theirMode == FileMode.TYPE_GITLINK) {
      unmergedPaths.add(tw.getPathString());
    } else {
      MergeResult<RawText> result = mergeContent();
      if(result.containsConflicts())
        unmergedPaths.add(path);
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

  @Nonnull
  private RawText getRawText(@Nonnull AnyObjectId id) throws IOException {
    if(id.equals(ObjectId.zeroId()))
      return new RawText(new byte[0]);
    return new RawText(reader.open(id, OBJ_BLOB).getCachedBytes());
  }

  private void prepareDirectory() {
    int depth = tw.getDepth();
    while(currentDepth > depth) {
      currentDirectory = currentDirectory.getParent();
      currentDepth--;
    }
  }

  private void addNode(@Nonnull String name, int mode, @Nonnull AnyObjectId id) {
    Node node = Node.forObject(id, FileMode.fromBits(mode), currentDirectory);
    currentDirectory.addChild(name, node, false);
  }

  private void addConflict() {

  }

}
