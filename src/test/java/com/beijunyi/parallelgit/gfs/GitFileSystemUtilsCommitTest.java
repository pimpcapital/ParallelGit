package com.beijunyi.parallelgit.gfs;

import java.io.IOException;
import java.nio.file.Files;

import com.beijunyi.parallelgit.utils.BlobHelper;
import com.beijunyi.parallelgit.utils.CommitHelper;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemUtilsCommitTest extends AbstractGitFileSystemTest {

  @Test
  public void commitInExistingBranchTest() throws IOException {
    initRepository();
    writeFile("old_file.txt");
    ObjectId prevCommit = commitToBranch("some_branch");
    injectGitFileSystem(GitFileSystems.newFileSystem(repo, "some_branch"));

    GitPath file = gfs.getPath("/file.txt");
    byte[] data = "some plain text data".getBytes();
    Files.write(file, data);
    RevCommit commit = GitFileSystemUtils.commit(file, TEST_USER_NAME, TEST_USER_EMAIL, "some message");

    Assert.assertNotNull(commit);
    Assert.assertEquals(commit, CommitHelper.getCommit(repo, "some_branch"));
    Assert.assertEquals(commit, gfs.getFileStore().getBaseCommit());
    Assert.assertEquals(commit.getTree(), gfs.getFileStore().getBaseTree());

    Assert.assertEquals("some message", commit.getFullMessage());
    PersonIdent author = commit.getAuthorIdent();
    Assert.assertEquals(TEST_USER_NAME, author.getName());
    Assert.assertEquals(TEST_USER_EMAIL, author.getEmailAddress());
    Assert.assertEquals(1, commit.getParentCount());
    Assert.assertEquals(prevCommit, commit.getParent(0));

    ObjectId fileBlob = BlobHelper.findBlobId(repo, "file.txt", commit);
    Assert.assertNotNull(fileBlob);
    Assert.assertArrayEquals(data, BlobHelper.getBytes(repo, fileBlob));
  }

  @Test
  public void commitInNewBranchTest() throws IOException {
    initRepository();
    injectGitFileSystem(GitFileSystems.newFileSystem(repo, "new_branch"));

    GitPath file = gfs.getPath("/file.txt");
    byte[] data = "some plain text data".getBytes();
    Files.write(file, data);
    RevCommit commit = GitFileSystemUtils.commit(file, TEST_USER_NAME, TEST_USER_EMAIL, "some message");

    Assert.assertNotNull(commit);
    Assert.assertEquals(commit, CommitHelper.getCommit(repo, "new_branch"));
    Assert.assertEquals(commit, gfs.getFileStore().getBaseCommit());
    Assert.assertEquals(commit.getTree(), gfs.getFileStore().getBaseTree());

    Assert.assertEquals("some message", commit.getFullMessage());
    PersonIdent author = commit.getAuthorIdent();
    Assert.assertEquals(TEST_USER_NAME, author.getName());
    Assert.assertEquals(TEST_USER_EMAIL, author.getEmailAddress());
    Assert.assertEquals(0, commit.getParentCount());

    ObjectId fileBlob = BlobHelper.findBlobId(repo, "file.txt", commit);
    Assert.assertNotNull(fileBlob);
    Assert.assertArrayEquals(data, BlobHelper.getBytes(repo, fileBlob));
  }

  @Test
  public void commitWithoutSpecifiedBranchTest() throws IOException {
    initRepository();
    writeFile("old_file.txt");
    ObjectId branchCommit = commitToBranch("some_branch");
    injectGitFileSystem(GitFileSystems.newFileSystem(repo, branchCommit));

    GitPath file = gfs.getPath("/file.txt");
    byte[] data = "some plain text data".getBytes();
    Files.write(file, data);
    RevCommit commit = GitFileSystemUtils.commit(file, TEST_USER_NAME, TEST_USER_EMAIL, "some message");

    Assert.assertEquals(branchCommit, CommitHelper.getCommit(repo, "some_branch"));

    Assert.assertNotNull(commit);
    Assert.assertEquals(commit, gfs.getFileStore().getBaseCommit());
    Assert.assertEquals(commit.getTree(), gfs.getFileStore().getBaseTree());

    Assert.assertEquals("some message", commit.getFullMessage());
    PersonIdent author = commit.getAuthorIdent();
    Assert.assertEquals(TEST_USER_NAME, author.getName());
    Assert.assertEquals(TEST_USER_EMAIL, author.getEmailAddress());
    Assert.assertEquals(1, commit.getParentCount());
    Assert.assertEquals(branchCommit, commit.getParent(0));

    ObjectId fileBlob = BlobHelper.findBlobId(repo, "file.txt", commit);
    Assert.assertNotNull(fileBlob);
    Assert.assertArrayEquals(data, BlobHelper.getBytes(repo, fileBlob));
  }

  @Test
  public void commitMultipleFilesTest() throws IOException {
    initRepository();
    writeFile("old_file.txt");
    commitToBranch("some_branch");
    injectGitFileSystem(GitFileSystems.newFileSystem(repo, "some_branch"));

    GitPath file1 = gfs.getPath("/file1.txt");
    byte[] data1 = "some plain text data".getBytes();
    Files.write(file1, data1);
    GitPath file2 = gfs.getPath("/file2.txt");
    byte[] data2 = "some plain text data".getBytes();
    Files.write(file2, data2);
    GitPath file3 = gfs.getPath("/file3.txt");
    byte[] data3 = "some different plain text data".getBytes();
    Files.write(file3, data3);

    PersonIdent author = new PersonIdent(TEST_USER_NAME, TEST_USER_EMAIL);
    RevCommit commit = GitFileSystemUtils.commit(gfs, author, author, "some message", false);

    Assert.assertNotNull(commit);
    ObjectId fileBlob1 = BlobHelper.findBlobId(repo, "file1.txt", commit);
    Assert.assertNotNull(fileBlob1);
    Assert.assertArrayEquals(data1, BlobHelper.getBytes(repo, fileBlob1));
    ObjectId fileBlob2 = BlobHelper.findBlobId(repo, "file2.txt", commit);
    Assert.assertNotNull(fileBlob2);
    Assert.assertArrayEquals(data2, BlobHelper.getBytes(repo, fileBlob2));
    ObjectId fileBlob3 = BlobHelper.findBlobId(repo, "file3.txt", commit);
    Assert.assertNotNull(fileBlob3);
    Assert.assertArrayEquals(data3, BlobHelper.getBytes(repo, fileBlob3));

  }

  @Test
  public void commitNoFileTest() throws IOException {
    initRepository();
    writeFile("old_file.txt");
    ObjectId lastCommit = commitToBranch("some_branch");
    injectGitFileSystem(GitFileSystems.newFileSystem(repo, "some_branch"));

    PersonIdent author = new PersonIdent(TEST_USER_NAME, TEST_USER_EMAIL);
    RevCommit commit = GitFileSystemUtils.commit(gfs, author, author, "some message", false);
    Assert.assertNull(commit);

    Assert.assertEquals(lastCommit, CommitHelper.getCommit(repo, "some_branch"));
  }

  @Test
  public void commitNoChangeTest() throws IOException {
    initRepository();
    byte[] data = "some plain text data".getBytes();
    writeFile("file.txt", data);
    ObjectId lastCommit = commitToBranch("some_branch");
    injectGitFileSystem(GitFileSystems.newFileSystem(repo, "some_branch"));

    GitPath file = gfs.getPath("/file.txt");
    Files.write(file, data);
    RevCommit commit = GitFileSystemUtils.commit(file, TEST_USER_NAME, TEST_USER_EMAIL, "some message");

    Assert.assertNull(commit);
    Assert.assertEquals(lastCommit, CommitHelper.getCommit(repo, "some_branch"));
  }



}
