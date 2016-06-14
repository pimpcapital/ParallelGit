package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Ref;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class CacheUtilsCreateTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void createCacheFromCommit_theResultCacheShouldContainTheFilesInTheSpecifiedCommit() throws IOException {
    writeToCache("/file1.txt");
    writeToCache("/file2.txt");
    AnyObjectId commitId = commit(someCommitMessage(), null);
    DirCache cache = CacheUtils.forRevision(commitId, repo);
    assertNotNull(CacheUtils.getEntry("/file1.txt", cache));
    assertNotNull(CacheUtils.getEntry("/file2.txt", cache));
  }

  @Test
  public void createCacheFromTag_theResultCacheShouldContainTheFilesInTheTaggedCommit() throws IOException {
    writeToCache("/file1.txt");
    writeToCache("/file2.txt");
    AnyObjectId tagId = TagUtils.tagCommit("test_tag", commit(someCommitMessage(), null), repo).getObjectId();
    DirCache cache = CacheUtils.forRevision(tagId, repo);
    assertNotNull(CacheUtils.getEntry("/file1.txt", cache));
    assertNotNull(CacheUtils.getEntry("/file2.txt", cache));
  }

  @Test
  public void createCacheFromTagRef_theResultCacheShouldContainTheFilesInTheTaggedCommit() throws IOException {
    writeToCache("/file1.txt");
    writeToCache("/file2.txt");
    Ref tagRef = TagUtils.tagCommit("test_tag", commit(someCommitMessage(), null), repo);
    DirCache cache = CacheUtils.forRevision(tagRef, repo);
    assertNotNull(CacheUtils.getEntry("/file1.txt", cache));
    assertNotNull(CacheUtils.getEntry("/file2.txt", cache));
  }

  @Test
  public void createCacheFromBranch_theResultCacheShouldContainTheFilesInTheBranchHeadCommit() throws IOException {
    writeToCache("/file1.txt");
    writeToCache("/file2.txt");
    commitToBranch("test_branch", someCommitMessage(), null);
    DirCache cache = CacheUtils.forRevision("test_branch", repo);
    assertNotNull(CacheUtils.getEntry("/file1.txt", cache));
    assertNotNull(CacheUtils.getEntry("/file2.txt", cache));
  }

  @Test
  public void createCacheFromTree_theResultCacheShouldContainTheFilesInTheSpecifiedTree() throws IOException {
    writeToCache("/file1.txt");
    writeToCache("/file2.txt");
    AnyObjectId treeId = commit().getTree();
    DirCache cache = CacheUtils.forTree(treeId, repo);
    assertNotNull(CacheUtils.getEntry("/file1.txt", cache));
    assertNotNull(CacheUtils.getEntry("/file2.txt", cache));
  }

}
