package com.beijunyi.parallelgit.runtime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.*;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Test;

public class ParallelCommitCommandTest extends AbstractParallelGitTest {

  @Test
  public void createCommitWithTreeTest() throws IOException {
    initRepository();
    writeSomeFileToCache();
    AnyObjectId treeId = CacheHelper.writeTree(repo, cache);
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
    AnyObjectId blobId = writeToCache(testFile);
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
                             .fromCache(cache)
                             .call();
    Assert.assertNotNull(commitId);
    DirCache cache = CacheHelper.forRevision(repo, commitId);
    Assert.assertEquals(1, cache.getEntryCount());
    Assert.assertEquals(blobId, cache.getEntry(testFile).getObjectId());
  }

  @Test
  public void createEmptyCommitTest() throws IOException {
    AnyObjectId currentHeadId = initRepository();
    RevCommit currentHead = CommitHelper.getCommit(repo, currentHeadId);
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
                             .withTree(currentHead.getTree())
                             .parents(currentHead)
                             .call();
    Assert.assertNull(commitId);
  }

  @Test
  public void createEmptyCommitWithAllowEmptyOptionTest() throws IOException {
    AnyObjectId currentHeadId = initRepository();
    RevCommit currentHead = CommitHelper.getCommit(repo, currentHeadId);
    AnyObjectId treeId = currentHead.getTree();
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
    writeSomeFileToCache();
    PersonIdent author = new PersonIdent("testuser", "testuser@email.com");
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
    writeSomeFileToCache();
    String authorName = "testuser";
    String authorEmail = "testuser@email.com";
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
    writeSomeFileToCache();
    PersonIdent committer = new PersonIdent("testuser", "testuser@email.com");
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
    writeSomeFileToCache();
    String committerName = "testuser";
    String committerEmail = "testuser@email.com";
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
    writeSomeFileToCache();
    PersonIdent author = new PersonIdent("author", "author@email.com");
    PersonIdent committer = new PersonIdent("committer", "committer@email.com");
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
    writeSomeFileToCache();
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
    writeSomeFileToCache();
    String commitMessage = "test message";
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
    writeSomeFileToCache();
    String branch = "test_branch";
    AnyObjectId treeId = RevTreeHelper.getRootTree(repo, commitToBranch(branch));
    String amendedMessage = "amended message";
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
  public void createCommitAmendBranchHeadTreeTest() throws IOException {
    byte[] bytes = "temp file content".getBytes();

    initRepository();
    String existingFile = "existing_file.txt";
    writeToCache(existingFile);
    String branch = "test_branch";
    String previousMessage = CommitHelper.getCommit(repo, commitToBranch(branch)).getFullMessage();
    String newFile = "new_file.txt";
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
                             .branch(branch)
                             .addFile(bytes, newFile)
                             .amend(true)
                             .call();
    Assert.assertNotNull(commitId);
    RevCommit branchHead = CommitHelper.getCommit(repo, branch);
    Assert.assertNotNull(branchHead);
    Assert.assertEquals(previousMessage, branchHead.getFullMessage());
    Assert.assertNotNull(BlobHelper.findBlobId(repo, branchHead, existingFile));
    Assert.assertArrayEquals(bytes, BlobHelper.getBytes(repo, branchHead, newFile));
  }

  @Test
  public void addFileFromByteArrayTest() throws IOException {
    byte[] bytes = "temp file content".getBytes();

    initRepository();
    String branch = "test_branch";
    String existingFile = "existing_file.txt";
    AnyObjectId existingFileBlob = writeToCache(existingFile);
    commitToBranch(branch);
    String newFile = "new_file.txt";
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
    AnyObjectId existingFileBlob = writeToCache(existingFile);
    commitToBranch(branch);
    String newFile = "new_file.txt";
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
    InputStream inputStream = new ByteArrayInputStream(bytes);

    initRepository();
    String branch = "test_branch";
    String existingFile = "existing_file.txt";
    AnyObjectId existingFileBlob = writeToCache(existingFile);
    commitToBranch(branch);
    String newFile = "new_file.txt";
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
      AnyObjectId existingFileBlob = writeToCache(existingFile);
      commitToBranch(branch);
      String newFile = "new_file.txt";
      AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
      AnyObjectId existingFileBlob = writeToCache(existingFile);
      commitToBranch(branch);
      String newFile = "new_file.txt";
      AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
    writeToCache(existingFile1);
    String existingFile2 = "existing_file2.txt";
    writeToCache(existingFile2);
    String existingFile3 = "existing_file3.txt";
    writeToCache(existingFile3);
    commitToBranch(branch);
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
      AnyObjectId existingFileBlob = writeToCache(existingFile);
      commitToBranch(branch);
      String newDirectory = "new_directory";
      AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
      AnyObjectId existingFileBlob = writeToCache(existingFile);
      commitToBranch(branch);
      String newDirectory = "new_directory";
      AnyObjectId commitId;
      try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(tempDirectoryPath)) {
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
      AnyObjectId existingFileBlob = writeToCache(existingFile);
      commitToBranch(branch);
      String newDirectory = "new_directory";
      AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
    writeToCache(file1);
    String file2 = "dir/subdir/2.txt";
    writeToCache(file2);
    String file3 = "dir/subdir/3.txt";
    writeToCache(file3);
    commitToBranch(branch);
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
    writeToCache(existingFile);
    commitToBranch(branch);
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
    writeToCache(existingFile);
    commitToBranch(branch);
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
    InputStream inputStream = new ByteArrayInputStream(bytes);

    initRepository();
    String branch = "test_branch";
    String existingFile = "existing_file.txt";
    writeToCache(existingFile);
    commitToBranch(branch);
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
                             .branch(branch)
                             .updateFile(existingFile, inputStream)
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
      writeToCache(existingFile);
      commitToBranch(branch);
      AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
                               .branch(branch)
                               .updateFile(existingFile, tempFilePath)
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
      writeToCache(existingFile);
      commitToBranch(branch);
      AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
                               .branch(branch)
                               .updateFile(existingFile, tempFilePath.toFile())
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
  public void createOrphanCommitTest() throws IOException {
    byte[] bytes = "temp file content".getBytes();

    initRepository();
    String branch = "test_branch";
    String existingFile = "existing_file.txt";
    writeToCache(existingFile);
    commitToBranch(branch);
    String newFile = "new_file.txt";
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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
  public void createOrphanWithNoChangeTest() throws IOException {
    initRepository();
    String existingFile1 = "existing_file1.txt";
    writeToCache(existingFile1);
    commitToMaster();
    String existingFile2 = "existing_file2.txt";
    writeToCache(existingFile2);
    AnyObjectId headCommit = commitToMaster();
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
                             .baseCommit(headCommit)
                             .orphan(true)
                             .call();
    Assert.assertNotNull(commitId);
    Assert.assertNotNull(BlobHelper.findBlobId(repo, commitId, existingFile1));
    Assert.assertNotNull(BlobHelper.findBlobId(repo, commitId, existingFile2));
    Assert.assertEquals(0, CommitHelper.getCommit(repo, commitId).getParentCount());
  }

  @Test
  public void createOrphanAtBranchTest() throws IOException {
    initRepository();
    String branch = "test_branch";
    String existingFile1 = "existing_file1.txt";
    writeToCache(existingFile1);
    commitToBranch(branch);
    String existingFile2 = "existing_file2.txt";
    writeToCache(existingFile2);
    commitToBranch(branch);
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
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

  @Test
  public void createNewBranchTest() throws IOException {
    byte[] bytes = "temp file content".getBytes();

    initRepository();
    String branch = "test_branch";
    String newFile = "new_file.txt";
    String message = "create new branch";
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
                             .branch(branch)
                             .addFile(bytes, newFile)
                             .message(message)
                             .call();
    Assert.assertNotNull(commitId);
    RevCommit branchHead = CommitHelper.getCommit(repo, branch);
    Assert.assertNotNull(branchHead);
    Assert.assertEquals(message, branchHead.getFullMessage());
    Assert.assertArrayEquals(bytes, BlobHelper.getBytes(repo, branchHead, newFile));
  }

  @Test
  public void createCommitFromRevisionIdTest() throws IOException {
    byte[] bytes = "temp file content".getBytes();

    initRepository();
    String existingFile = "existing_file.txt";
    writeToCache(existingFile);
    AnyObjectId parentRevision = commitToMaster();
    String newFile = "new_file.txt";
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
                             .revision(parentRevision)
                             .addFile(bytes, newFile)
                             .call();
    Assert.assertNotNull(commitId);
    RevCommit newCommit = CommitHelper.getCommit(repo, commitId);
    Assert.assertNotNull(BlobHelper.findBlobId(repo, newCommit, existingFile));
    Assert.assertEquals(parentRevision, newCommit.getParent(0));
    Assert.assertArrayEquals(bytes, BlobHelper.getBytes(repo, newCommit, newFile));
  }

  @Test
  public void createCommitFromRevisionIdStrTest() throws IOException {
    byte[] bytes = "temp file content".getBytes();

    initRepository();
    String existingFile = "existing_file.txt";
    writeToCache(existingFile);
    AnyObjectId parentRevision = commitToMaster();
    String newFile = "new_file.txt";
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
                             .revision(parentRevision.getName())
                             .addFile(bytes, newFile)
                             .call();
    Assert.assertNotNull(commitId);
    RevCommit newCommit = CommitHelper.getCommit(repo, commitId);
    Assert.assertNotNull(BlobHelper.findBlobId(repo, newCommit, existingFile));
    Assert.assertEquals(parentRevision, newCommit.getParent(0));
    Assert.assertArrayEquals(bytes, BlobHelper.getBytes(repo, newCommit, newFile));
  }

}
