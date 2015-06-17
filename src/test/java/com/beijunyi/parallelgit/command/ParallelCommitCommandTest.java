package com.beijunyi.parallelgit.command;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.util.*;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Test;

public class ParallelCommitCommandTest extends AbstractParallelGitTest {

  private void writeSomethingToCache() throws IOException {
    writeFile("something.txt");
  }

  @Test
  public void createCommitWithTreeTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    ObjectInserter inserter = repo.newObjectInserter();
    AnyObjectId treeId;
    try {
      treeId = cache.writeTree(inserter);
    } finally {
      inserter.release();
    }
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .withTree(treeId)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    Assert.assertEquals(treeId, commit.getTree());
  }

  @Test
  public void createCommitFromCacheTest() throws IOException {
    initRepository();
    String testFile = "testfile.txt";
    ObjectId blobId = writeFile(testFile);
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .fromCache(cache)
                          .call();
    Assert.assertNotNull(commitId);
    DirCache cache = DirCacheHelper.forRevision(repo, commitId);
    Assert.assertEquals(1, cache.getEntryCount());
    Assert.assertEquals(blobId, cache.getEntry(testFile).getObjectId());
  }

  @Test
  public void createEmptyCommitTest() throws IOException {
    ObjectId currentHeadId = initRepository();
    RevCommit currentHead = CommitHelper.getCommit(repo, currentHeadId);
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .withTree(currentHead.getTree())
                          .parents(currentHead)
                          .call();
    Assert.assertNull(commitId);
  }

  @Test
  public void createEmptyCommitWithAllowEmptyOptionTest() throws IOException {
    ObjectId currentHeadId = initRepository();
    RevCommit currentHead = CommitHelper.getCommit(repo, currentHeadId);
    AnyObjectId treeId = currentHead.getTree();
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .withTree(currentHead.getTree())
                          .allowEmptyCommit(true)
                          .parents(currentHead)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    Assert.assertEquals(treeId, commit.getTree());
  }

  @Test
  public void createCommitWithAuthorTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    PersonIdent author = new PersonIdent("testuser", "testuser@email.com");
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .fromCache(cache)
                          .author(author)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    Assert.assertEquals(author, commit.getAuthorIdent());
  }

  @Test
  public void createCommitWithAuthorNameAndEmailTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    String authorName = "testuser";
    String authorEmail = "testuser@email.com";
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .fromCache(cache)
                          .author(authorName, authorEmail)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    PersonIdent author = commit.getAuthorIdent();
    Assert.assertEquals(authorName, author.getName());
    Assert.assertEquals(authorEmail, author.getEmailAddress());
  }

  @Test
  public void createCommitWithCommitterTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    PersonIdent committer = new PersonIdent("testuser", "testuser@email.com");
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .fromCache(cache)
                          .committer(committer)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    Assert.assertEquals(committer, commit.getCommitterIdent());
    Assert.assertEquals(committer, commit.getAuthorIdent());
  }

  @Test
  public void createCommitWithCommitterNameAndEmailTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    String committerName = "testuser";
    String committerEmail = "testuser@email.com";
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .fromCache(cache)
                          .committer(committerName, committerEmail)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    PersonIdent committer = commit.getCommitterIdent();
    Assert.assertEquals(commit.getAuthorIdent(), committer);
    Assert.assertEquals(committerName, committer.getName());
    Assert.assertEquals(committerEmail, committer.getEmailAddress());
  }

  @Test
  public void createCommitWithAuthorAndCommitterTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    PersonIdent author = new PersonIdent("author", "author@email.com");
    PersonIdent committer = new PersonIdent("committer", "committer@email.com");
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .fromCache(cache)
                          .author(author)
                          .committer(committer)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    Assert.assertEquals(author, commit.getAuthorIdent());
    Assert.assertEquals(committer, commit.getCommitterIdent());
  }

  @Test
  public void createCommitWithDefaultAuthorTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .fromCache(cache)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    PersonIdent committer = commit.getCommitterIdent();
    UserConfig userConfig = repo.getConfig().get(UserConfig.KEY);
    Assert.assertEquals(userConfig.getCommitterName(), committer.getName());
    Assert.assertEquals(userConfig.getCommitterEmail(), committer.getEmailAddress());
  }

  @Test
  public void createCommitWithMessageTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    String commitMessage = "test message";
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .fromCache(cache)
                          .message(commitMessage)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    Assert.assertEquals(commitMessage, commit.getFullMessage());
  }

  @Test
  public void createCommitAmendBranchHeadMessageTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    String branch = "test_branch";
    AnyObjectId treeId = RevTreeHelper.getRootTree(repo, commitToBranch(branch));
    String amendedMessage = "amended message";
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .branch(branch)
                          .amend(true)
                          .message(amendedMessage)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit branchHead = CommitHelper.getCommit(repo, branch);
    Assert.assertNotNull(branchHead);
    Assert.assertEquals(treeId, branchHead.getTree());
    Assert.assertEquals(amendedMessage, branchHead.getFullMessage());
  }

  @Test
  public void createCommitAmendBranchHeadContentTest() throws IOException {
    initRepository();
    writeFile("file1.txt");
    String branch = "test_branch";
    AnyObjectId treeId = RevTreeHelper.getRootTree(repo, commitToBranch(branch));
    String amendedMessage = "amended message";
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .branch(branch)
                          .amend(true)
                          .message(amendedMessage)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit branchHead = CommitHelper.getCommit(repo, branch);
    Assert.assertNotNull(branchHead);
    Assert.assertEquals(treeId, branchHead.getTree());
    Assert.assertEquals(amendedMessage, branchHead.getFullMessage());
  }

  @Test
  public void addFileFromByteArrayTest() throws IOException {
    byte[] bytes = "temp file content".getBytes();

    initRepository();
    String branch = "test_branch";
    String existingFile = "existing_file.txt";
    ObjectId existingFileBlob = writeFile(existingFile);
    commitToBranch(branch);
    String newFile = "new_file.txt";
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .branch(branch)
                          .addFile(bytes, newFile)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit branchHead = CommitHelper.getCommit(repo, branch);
    Assert.assertNotNull(branchHead);
    Assert.assertEquals(existingFileBlob, BlobHelper.findBlobId(repo, branchHead, existingFile));
    Assert.assertArrayEquals(bytes, BlobHelper.getBytes(repo, branchHead, newFile));
  }

  @Test
  public void addFileFromStringContentTest() throws IOException {
    String content = "temp file content";

    initRepository();
    String branch = "test_branch";
    String existingFile = "existing_file.txt";
    ObjectId existingFileBlob = writeFile(existingFile);
    commitToBranch(branch);
    String newFile = "new_file.txt";
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .branch(branch)
                          .addFile(content, newFile)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit branchHead = CommitHelper.getCommit(repo, branch);
    Assert.assertNotNull(branchHead);
    Assert.assertEquals(existingFileBlob, BlobHelper.findBlobId(repo, branchHead, existingFile));
    Assert.assertArrayEquals(Constants.encode(content), BlobHelper.getBytes(repo, branchHead, newFile));
  }

  @Test
  public void addFileFromInputStreamTest() throws IOException {
    byte[] bytes = "temp file content".getBytes();
    InputStream inputStream = new ByteArrayInputStream(bytes) ;

    initRepository();
    String branch = "test_branch";
    String existingFile = "existing_file.txt";
    ObjectId existingFileBlob = writeFile(existingFile);
    commitToBranch(branch);
    String newFile = "new_file.txt";
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .branch(branch)
                          .addFile(inputStream, newFile)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit branchHead = CommitHelper.getCommit(repo, branch);
    Assert.assertNotNull(branchHead);
    Assert.assertEquals(existingFileBlob, BlobHelper.findBlobId(repo, branchHead, existingFile));
    Assert.assertArrayEquals(bytes, BlobHelper.getBytes(repo, branchHead, newFile));
  }

  @Test
  public void addFileFromSourcePathTest() throws IOException {
    Path tempFilePath = Files.createTempFile(null, null);
    try {
      byte[] bytes = "temp file content".getBytes();
      Files.write(tempFilePath, bytes);

      initRepository();
      String branch = "test_branch";
      String existingFile = "existing_file.txt";
      ObjectId existingFileBlob = writeFile(existingFile);
      commitToBranch(branch);
      String newFile = "new_file.txt";
      ObjectId commitId = ParallelCommitCommand.prepare(repo)
                            .branch(branch)
                            .addFile(tempFilePath, newFile)
                            .call();
      Assert.assertNotNull(commitId);
      RevCommit branchHead = CommitHelper.getCommit(repo, branch);
      Assert.assertNotNull(branchHead);
      Assert.assertEquals(existingFileBlob, BlobHelper.findBlobId(repo, branchHead, existingFile));
      Assert.assertArrayEquals(bytes, BlobHelper.getBytes(repo, branchHead, newFile));
    } finally {
      Assert.assertTrue(Files.deleteIfExists(tempFilePath));
    }
  }

  @Test
  public void addFileFromSourceFileTest() throws IOException {
    Path tempFilePath = Files.createTempFile(null, null);
    try {
      byte[] bytes = "temp file content".getBytes();
      Files.write(tempFilePath, bytes);

      initRepository();
      String branch = "test_branch";
      String existingFile = "existing_file.txt";
      ObjectId existingFileBlob = writeFile(existingFile);
      commitToBranch(branch);
      String newFile = "new_file.txt";
      ObjectId commitId = ParallelCommitCommand.prepare(repo)
                            .branch(branch)
                            .addFile(tempFilePath.toFile(), newFile)
                            .call();
      Assert.assertNotNull(commitId);
      RevCommit branchHead = CommitHelper.getCommit(repo, branch);
      Assert.assertNotNull(branchHead);
      Assert.assertEquals(existingFileBlob, BlobHelper.findBlobId(repo, branchHead, existingFile));
      Assert.assertArrayEquals(bytes, BlobHelper.getBytes(repo, branchHead, newFile));
    } finally {
      Assert.assertTrue(Files.deleteIfExists(tempFilePath));
    }
  }

  @Test
  public void deleteFileTest() throws IOException {
    initRepository();
    String branch = "test_branch";
    String existingFile1 = "existing_file1.txt";
    writeFile(existingFile1);
    String existingFile2 = "existing_file2.txt";
    writeFile(existingFile2);
    String existingFile3 = "existing_file3.txt";
    writeFile(existingFile3);
    commitToBranch(branch);
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .branch(branch)
                          .deleteFile(existingFile2)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit branchHead = CommitHelper.getCommit(repo, branch);
    Assert.assertNotNull(branchHead);
    Assert.assertNotNull(BlobHelper.findBlobId(repo, branchHead, existingFile1));
    Assert.assertNull(BlobHelper.findBlobId(repo, branchHead, existingFile2));
    Assert.assertNotNull(BlobHelper.findBlobId(repo, branchHead, existingFile3));
  }

  @Test
  public void addDirectoryFromSourcePathTest() throws IOException {
    Path tempDirectoryPath = Files.createTempDirectory(null);
    String tempFile1 = "file1.txt";
    Path tempFilePath1 = tempDirectoryPath.resolve(tempFile1);
    String tempFile2 = "file2.txt";
    Path tempFilePath2 = tempDirectoryPath.resolve(tempFile2);
    String tempFile3 = "sub_directory/file3.txt";
    Path tempFilePath3 = tempDirectoryPath.resolve(tempFile3);
    try {
      byte[] tempBytes1 = "temp content 1".getBytes();
      Files.write(tempFilePath1, tempBytes1);
      byte[] tempBytes2 = "temp content 2".getBytes();
      Files.write(tempFilePath2, tempBytes2);
      byte[] tempBytes3 = "temp content 3".getBytes();
      Files.createDirectory(tempFilePath3.getParent());
      Files.write(tempFilePath3, tempBytes3);

      initRepository();
      String branch = "test_branch";
      String existingFile = "existing_file.txt";
      ObjectId existingFileBlob = writeFile(existingFile);
      commitToBranch(branch);
      String newDirectory = "new_directory";
      ObjectId commitId = ParallelCommitCommand.prepare(repo)
                            .branch(branch)
                            .addDirectory(tempDirectoryPath, newDirectory)
                            .call();
      Assert.assertNotNull(commitId);
      RevCommit branchHead = CommitHelper.getCommit(repo, branch);
      Assert.assertNotNull(branchHead);
      Assert.assertEquals(existingFileBlob, BlobHelper.findBlobId(repo, branchHead, existingFile));
      Assert.assertArrayEquals(tempBytes1, BlobHelper.getBytes(repo, branchHead, newDirectory + "/" + tempFile1));
      Assert.assertArrayEquals(tempBytes2, BlobHelper.getBytes(repo, branchHead, newDirectory + "/" + tempFile2));
      Assert.assertArrayEquals(tempBytes3, BlobHelper.getBytes(repo, branchHead, newDirectory + "/" + tempFile3));
    } finally {
      Files.deleteIfExists(tempFilePath1);
      Files.deleteIfExists(tempFilePath2);
      Files.deleteIfExists(tempFilePath3);
      Files.deleteIfExists(tempFilePath3.getParent());
      Assert.assertTrue(Files.deleteIfExists(tempDirectoryPath));
    }
  }

  @Test
  public void addDirectoryFromDirectoryStreamTest() throws IOException {
    Path tempDirectoryPath = Files.createTempDirectory(null);
    String tempFile1 = "file1.txt";
    Path tempFilePath1 = tempDirectoryPath.resolve(tempFile1);
    String tempFile2 = "file2.txt";
    Path tempFilePath2 = tempDirectoryPath.resolve(tempFile2);
    String tempFile3 = "sub_directory/file3.txt";
    Path tempFilePath3 = tempDirectoryPath.resolve(tempFile3);
    try {
      byte[] tempBytes1 = "temp content 1".getBytes();
      Files.write(tempFilePath1, tempBytes1);
      byte[] tempBytes2 = "temp content 2".getBytes();
      Files.write(tempFilePath2, tempBytes2);
      byte[] tempBytes3 = "temp content 3".getBytes();
      Files.createDirectory(tempFilePath3.getParent());
      Files.write(tempFilePath3, tempBytes3);

      initRepository();
      String branch = "test_branch";
      String existingFile = "existing_file.txt";
      ObjectId existingFileBlob = writeFile(existingFile);
      commitToBranch(branch);
      String newDirectory = "new_directory";
      ObjectId commitId;
      try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(tempDirectoryPath)) {
        commitId = ParallelCommitCommand.prepare(repo)
                     .branch(branch)
                     .addDirectory(directoryStream, newDirectory)
                     .call();
      }
      Assert.assertNotNull(commitId);
      RevCommit branchHead = CommitHelper.getCommit(repo, branch);
      Assert.assertNotNull(branchHead);
      Assert.assertEquals(existingFileBlob, BlobHelper.findBlobId(repo, branchHead, existingFile));
      Assert.assertArrayEquals(tempBytes1, BlobHelper.getBytes(repo, branchHead, newDirectory + "/" + tempFile1));
      Assert.assertArrayEquals(tempBytes2, BlobHelper.getBytes(repo, branchHead, newDirectory + "/" + tempFile2));
      Assert.assertArrayEquals(tempBytes3, BlobHelper.getBytes(repo, branchHead, newDirectory + "/" + tempFile3));
    } finally {
      Files.deleteIfExists(tempFilePath1);
      Files.deleteIfExists(tempFilePath2);
      Files.deleteIfExists(tempFilePath3);
      Files.deleteIfExists(tempFilePath3.getParent());
      Assert.assertTrue(Files.deleteIfExists(tempDirectoryPath));
    }
  }


  @Test
  public void addDirectoryFromSourceFileTest() throws IOException {
    Path tempDirectoryPath = Files.createTempDirectory(null);
    String tempFile1 = "file1.txt";
    Path tempFilePath1 = tempDirectoryPath.resolve(tempFile1);
    String tempFile2 = "file2.txt";
    Path tempFilePath2 = tempDirectoryPath.resolve(tempFile2);
    String tempFile3 = "sub_directory/file3.txt";
    Path tempFilePath3 = tempDirectoryPath.resolve(tempFile3);
    try {
      byte[] tempBytes1 = "temp content 1".getBytes();
      Files.write(tempFilePath1, tempBytes1);
      byte[] tempBytes2 = "temp content 2".getBytes();
      Files.write(tempFilePath2, tempBytes2);
      byte[] tempBytes3 = "temp content 3".getBytes();
      Files.createDirectory(tempFilePath3.getParent());
      Files.write(tempFilePath3, tempBytes3);

      initRepository();
      String branch = "test_branch";
      String existingFile = "existing_file.txt";
      ObjectId existingFileBlob = writeFile(existingFile);
      commitToBranch(branch);
      String newDirectory = "new_directory";
      ObjectId commitId = ParallelCommitCommand.prepare(repo)
                            .branch(branch)
                            .addDirectory(tempDirectoryPath.toFile(), newDirectory)
                            .call();
      Assert.assertNotNull(commitId);
      RevCommit branchHead = CommitHelper.getCommit(repo, branch);
      Assert.assertNotNull(branchHead);
      Assert.assertEquals(existingFileBlob, BlobHelper.findBlobId(repo, branchHead, existingFile));
      Assert.assertArrayEquals(tempBytes1, BlobHelper.getBytes(repo, branchHead, newDirectory + "/" + tempFile1));
      Assert.assertArrayEquals(tempBytes2, BlobHelper.getBytes(repo, branchHead, newDirectory + "/" + tempFile2));
      Assert.assertArrayEquals(tempBytes3, BlobHelper.getBytes(repo, branchHead, newDirectory + "/" + tempFile3));
    } finally {
      Files.deleteIfExists(tempFilePath1);
      Files.deleteIfExists(tempFilePath2);
      Files.deleteIfExists(tempFilePath3);
      Files.deleteIfExists(tempFilePath3.getParent());
      Assert.assertTrue(Files.deleteIfExists(tempDirectoryPath));
    }
  }

  @Test
  public void deleteDirectoryTest() throws IOException {
    initRepository();
    String branch = "test_branch";
    String file1 = "dir/1.txt";
    writeFile(file1);
    String file2 = "dir/subdir/2.txt";
    writeFile(file2);
    String file3 = "dir/subdir/3.txt";
    writeFile(file3);
    commitToBranch(branch);
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .branch(branch)
                          .deleteDirectory("dir/subdir")
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit branchHead = CommitHelper.getCommit(repo, branch);
    Assert.assertNotNull(branchHead);
    Assert.assertNotNull(BlobHelper.findBlobId(repo, branchHead, file1));
    Assert.assertNull(BlobHelper.findBlobId(repo, branchHead, file2));
    Assert.assertNull(BlobHelper.findBlobId(repo, branchHead, file3));
  }

  @Test
  public void updateFileFromByteArrayTest() throws IOException {
    byte[] bytes = "temp file content".getBytes();

    initRepository();
    String branch = "test_branch";
    String existingFile = "existing_file.txt";
    writeFile(existingFile);
    commitToBranch(branch);
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .branch(branch)
                          .updateFile(bytes, existingFile)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit branchHead = CommitHelper.getCommit(repo, branch);
    Assert.assertNotNull(branchHead);
    Assert.assertArrayEquals(bytes, BlobHelper.getBytes(repo, branchHead, existingFile));
  }

  @Test
  public void updateFileFromStringContentTest() throws IOException {
    String content = "temp file content";

    initRepository();
    String branch = "test_branch";
    String existingFile = "existing_file.txt";
    writeFile(existingFile);
    commitToBranch(branch);
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .branch(branch)
                          .updateFile(content, existingFile)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit branchHead = CommitHelper.getCommit(repo, branch);
    Assert.assertNotNull(branchHead);
    Assert.assertArrayEquals(Constants.encode(content), BlobHelper.getBytes(repo, branchHead, existingFile));
  }

  @Test
  public void updateFileFromInputStreamTest() throws IOException {
    byte[] bytes = "temp file content".getBytes();
    InputStream inputStream = new ByteArrayInputStream(bytes) ;

    initRepository();
    String branch = "test_branch";
    String existingFile = "existing_file.txt";
    writeFile(existingFile);
    commitToBranch(branch);
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .branch(branch)
                          .updateFile(inputStream, existingFile)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit branchHead = CommitHelper.getCommit(repo, branch);
    Assert.assertNotNull(branchHead);
    Assert.assertArrayEquals(bytes, BlobHelper.getBytes(repo, branchHead, existingFile));
  }

  @Test
  public void updateFileFromSourcePathTest() throws IOException {
    Path tempFilePath = Files.createTempFile(null, null);
    try {
      byte[] bytes = "temp file content".getBytes();
      Files.write(tempFilePath, bytes);

      initRepository();
      String branch = "test_branch";
      String existingFile = "existing_file.txt";
      writeFile(existingFile);
      commitToBranch(branch);
      ObjectId commitId = ParallelCommitCommand.prepare(repo)
                            .branch(branch)
                            .updateFile(tempFilePath, existingFile)
                            .call();
      Assert.assertNotNull(commitId);
      RevCommit branchHead = CommitHelper.getCommit(repo, branch);
      Assert.assertNotNull(branchHead);
      Assert.assertArrayEquals(bytes, BlobHelper.getBytes(repo, branchHead, existingFile));
    } finally {
      Assert.assertTrue(Files.deleteIfExists(tempFilePath));
    }
  }

  @Test
  public void updateFileFromSourceFileTest() throws IOException {
    Path tempFilePath = Files.createTempFile(null, null);
    try {
      byte[] bytes = "temp file content".getBytes();
      Files.write(tempFilePath, bytes);

      initRepository();
      String branch = "test_branch";
      String existingFile = "existing_file.txt";
      writeFile(existingFile);
      commitToBranch(branch);
      ObjectId commitId = ParallelCommitCommand.prepare(repo)
                            .branch(branch)
                            .updateFile(tempFilePath.toFile(), existingFile)
                            .call();
      Assert.assertNotNull(commitId);
      RevCommit branchHead = CommitHelper.getCommit(repo, branch);
      Assert.assertNotNull(branchHead);
      Assert.assertArrayEquals(bytes, BlobHelper.getBytes(repo, branchHead, existingFile));
    } finally {
      Assert.assertTrue(Files.deleteIfExists(tempFilePath));
    }
  }

  @Test
  public void createOrphanCommit() throws IOException {
    byte[] bytes = "temp file content".getBytes();

    initRepository();
    String branch = "test_branch";
    String existingFile = "existing_file.txt";
    writeFile(existingFile);
    commitToBranch(branch);
    String newFile = "new_file.txt";
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .branch(branch)
                          .addFile(bytes, newFile)
                          .orphan(true)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit branchHead = CommitHelper.getCommit(repo, branch);
    Assert.assertNotNull(branchHead);
    Assert.assertNotNull(BlobHelper.findBlobId(repo, branchHead, existingFile));
    Assert.assertEquals(0, branchHead.getParentCount());
    Assert.assertArrayEquals(bytes, BlobHelper.getBytes(repo, branchHead, newFile));
  }

  @Test
  public void createOrphanWithNoChange() throws IOException {
    initRepository();
    String existingFile1 = "existing_file1.txt";
    writeFile(existingFile1);
    commitToMaster();
    String existingFile2 = "existing_file2.txt";
    writeFile(existingFile2);
    ObjectId headCommit = commitToMaster();
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .baseCommit(headCommit)
                          .orphan(true)
                          .call();
    Assert.assertNotNull(commitId);
    Assert.assertNotNull(BlobHelper.findBlobId(repo, commitId, existingFile1));
    Assert.assertNotNull(BlobHelper.findBlobId(repo, commitId, existingFile2));
    Assert.assertEquals(0, CommitHelper.getCommit(repo, commitId).getParentCount());
  }

  @Test
  public void createOrphanAtBranch() throws IOException {
    initRepository();
    String branch = "test_branch";
    String existingFile1 = "existing_file1.txt";
    writeFile(existingFile1);
    commitToBranch(branch);
    String existingFile2 = "existing_file2.txt";
    writeFile(existingFile2);
    commitToBranch(branch);
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .branch(branch)
                          .orphan(true)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit branchHead = CommitHelper.getCommit(repo, branch);
    Assert.assertNotNull(branchHead);
    Assert.assertNotNull(BlobHelper.findBlobId(repo, branchHead, existingFile1));
    Assert.assertNotNull(BlobHelper.findBlobId(repo, branchHead, existingFile2));
    Assert.assertEquals(0, branchHead.getParentCount());
  }



}
