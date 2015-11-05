package com.beijunyi.parallelgit.filesystem.merge;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.io.DirectoryNode;
import com.beijunyi.parallelgit.filesystem.utils.GitFileSystemBuilder;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.NameConflictTreeWalk;

public class GfsMerger extends ResolveMerger {

  private final GitFileSystem gfs;

  private AbstractTreeIterator baseTree;
  private AbstractTreeIterator ourTree;
  private AbstractTreeIterator theirTree;

  private DirectoryNode currentDirectory;
  private int curentDepth;

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
    curentDepth = 0;
  }

  @Override
  protected boolean mergeTrees(@Nonnull AbstractTreeIterator base, @Nonnull RevTree head, @Nonnull RevTree merge, boolean ignoreConflicts) throws IOException {
    prepareIterators(base, head, merge);
    prepareTreeWalk();
    return mergeTreeWalk(ignoreConflicts);
  }

  protected boolean mergeTreeWalk(boolean ignoreConflicts) throws IOException {
    while(tw.next()) {
      mergeTreeNode(ignoreConflicts);
      if(tw.isSubtree())
        tw.enterSubtree();
    }
    return true;
  }

  public boolean mergeTreeNode(boolean ignoreConflicts) {
    readTreeNodes();
    prepareDirectory();

    if(oursHasNoChange())
      applyTheirs();
    else if(theirsHasNoChange())
      applyOurs();
    else if(sameBlobOnBothSides())
      applyCommonBlob();
    else if(neitherSideIsDirectory())
      mergeNode();
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

  private boolean idEquals(int idx1, int idx2) {
    return tw.idEqual(idx1, idx2);
  }

  private boolean modeEquals(int idx1, int idx2) {
    return tw.getRawMode(idx1) == tw.getRawMode(idx2);
  }

//  private int mergeFileModes() {
//    int base = tw.getRawMode(T_BASE);
//    int theirs = tw.getRawMode(T_THEIRS);
//    int ours = tw.getRawMode(T_THEIRS);
//    if (ours == theirs)
//      return ours;
//    if (base == ours)
//      return theirs == FileMode.TYPE_MISSING ? ours : theirs;
//    if (base == theirs)
//      return ours == FileMode.TYPE_MISSING ? theirs : ours;
//    return FileMode.TYPE_MISSING;
//  }

  private void readTreeNodes() {
    name = tw.getNameString();
    baseMode = baseTree.getEntryRawMode();
    baseId = baseTree.getEntryObjectId();
    ourMode = ourTree.getEntryRawMode();
    ourId = ourTree.getEntryObjectId();
    theirMode = theirTree.getEntryRawMode();
    theirId = theirTree.getEntryObjectId();
  }
  
  private boolean oursEqualsTheirs() {
    return isNonTree(ourMode) && isNonTree(theirMode) && ourId.equals(theirId);
  }

  private boolean oursHasNoChange() {
    return baseMode == ourMode && baseId.equals(ourId);
  }

  private boolean theirsHasNoChange() {
    return baseMode == theirMode && baseId.equals(theirId);
  }

  private boolean sameBlobOnBothSides() {
    return ourId.equals(theirId);
  }

  private boolean neitherSideIsDirectory() {
    return ourMode == FileMode.TYPE_TREE || theirMode == FileMode.TYPE_TREE;
  }
  
  private boolean isDirectory() {
    return tw.isSubtree();
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

  }

  private void mergeNode() {

  }

  private void prepareDirectory() {

  }

  private void addNode(@Nonnull String name, int mode, @Nonnull AnyObjectId id) {

  }

  private void addConflict() {

  }

}
