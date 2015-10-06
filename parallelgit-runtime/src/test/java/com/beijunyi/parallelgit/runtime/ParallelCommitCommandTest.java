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
    AnyObjectId treeId = CacheUtils.writeTree(cache, repo);
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
                             .withTree(treeId)
                             .call();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitUtils.getCommit(commitId, repo);
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
    DirCache cache = CacheUtils.forRevision(repo, commitId);
    Assert.assertEquals(1, cache.getEntryCount());
    Assert.assertEquals(blobId, cache.getEntry(testFile).getObjectId());
  }

  @Test
  public void createEmptyCommitTest() throws IOException {
    AnyObjectId currentHeadId = initRepository();
    RevCommit currentHead = CommitUtils.getCommit(currentHeadId, repo);
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
                             .withTree(currentHead.getTree())
                             .parents(currentHead)
                             .call();
    Assert.assertNull(commitId);
  }

  @Test
  public void createEmptyCommitWithAllowEmptyOptionTest() throws IOException {
    AnyObjectId currentHeadId = initRepository();
    RevCommit currentHead = CommitUtils.getCommit(currentHeadId, repo);
    AnyObjectId treeId = currentHead.getTree();
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
                             .withTree(currentHead.getTree())
                             .allowEmptyCommit(true)
                             .parents(currentHead)
                             .call();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitUtils.getCommit(commitId, repo);
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
    RevCommit commit = CommitUtils.getCommit(commitId, repo);
    Assert.assertEquals(author, commit.getAuthorIdent());
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
    RevCommit commit = CommitUtils.getCommit(commitId, repo);
    Assert.assertEquals(committer, commit.getCommitterIdent());
    Assert.assertEquals(committer, commit.getAuthorIdent());
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
    RevCommit commit = CommitUtils.getCommit(commitId, repo);
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
    RevCommit commit = CommitUtils.getCommit(commitId, repo);
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
    RevCommit commit = CommitUtils.getCommit(commitId, repo);
    Assert.assertEquals(commitMessage, commit.getFullMessage());
  }

  @Test
  public void createCommitAmendBranchHeadMessageTest() throws IOException {
    initRepository();
    writeSomeFileToCache();
    String branch = "test_branch";
    AnyObjectId treeId = CommitUtils.getCommit(commitToBranch(branch), repo).getTree();
    String amendedMessage = "amended message";
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
                             .branch(branch)
                             .amend(true)
                             .message(amendedMessage)
                             .call();
    Assert.assertNotNull(commitId);
    RevCommit branchHead = CommitUtils.getCommit(branch, repo);
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
    String previousMessage = CommitUtils.getCommit(commitToBranch(branch), repo).getFullMessage();
    String newFile = "new_file.txt";
    AnyObjectId commitId = ParallelCommitCommand.prepare(repo)
                             .branch(branch)
                             .addFile(bytes, newFile)
                             .amend(true)
                             .call();
    Assert.assertNotNull(commitId);
    RevCommit branchHead = CommitUtils.getCommit(branch, repo);
    Assert.assertNotNull(branchHead);
    Assert.assertEquals(previousMessage, branchHead.getFullMessage());
    Assert.assertNotNull(ObjectUtils.findObject(existingFile, branchHead, repo));
    Assert.assertArrayEquals(bytes, GitFileUtils.readFile(newFile, branchHead, repo));
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
    RevCommit branchHead = CommitUtils.getCommit(branch, repo);
    Assert.assertNotNull(branchHead);
    Assert.assertEquals(existingFileBlob, ObjectUtils.findObject(existingFile, branchHead, repo));
    Assert.assertArrayEquals(bytes, GitFileUtils.readFile(newFile, branchHead, repo));
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
    RevCommit branchHead = CommitUtils.getCommit(branch, repo);
    Assert.assertNotNull(branchHead);
    Assert.assertEquals(existingFileBlob, ObjectUtils.findObject(existingFile, branchHead, repo));
    Assert.assertArrayEquals(Constants.encode(content), GitFileUtils.readFile(newFile, branchHead, repo));
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
    RevCommit branchHead = CommitUtils.getCommit(branch, repo);
    Assert.assertNotNull(branchHead);
    Assert.assertEquals(existingFileBlob, ObjectUtils.findObject(existingFile, branchHead, repo));
    Assert.assertArrayEquals(bytes, GitFileUtils.readFile(newFile, branchHead, repo));
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
      RevCommit branchHead = CommitUtils.getCommit(branch, repo);
      Assert.assertNotNull(branchHead);
      Assert.assertEquals(existingFileBlob, ObjectUtils.findObject(existingFile, branchHead, repo));
      Assert.assertArrayEquals(bytes, GitFileUtils.readFile(newFile, branchHead, repo));
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
      RevCommit branchHead = CommitUtils.getCommit(branch, repo);
      Assert.assertNotNull(branchHead);
      Assert.assertEquals(existingFileBlob, ObjectUtils.findObject(existingFile, branchHead, repo));
      Assert.assertArrayEquals(bytes, GitFileUtils.readFile(newFile, branchHead, repo));
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
    RevCommit branchHead = CommitUtils.getCommit(branch, repo);
    Assert.assertNotNull(branchHead);
    Assert.assertNotNull(ObjectUtils.findObject(existingFile1, branchHead, repo));
    Assert.assertNull(ObjectUtils.findObject(existingFile2, branchHead, repo));
    Assert.assertNotNull(ObjectUtils.findObject(existingFile3, branchHead, repo));
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
      RevCommit branchHead = CommitUtils.getCommit(branch, repo);
      Assert.assertNotNull(branchHead);
      Assert.assertEquals(existingFileBlob, ObjectUtils.findObject(existingFile, branchHead, repo));
      Assert.assertArrayEquals(tempBytes1, GitFileUtils.readFile(newDirectory + "/" + tempFile1, branchHead, repo));
      Assert.assertArrayEquals(tempBytes2, GitFileUtils.readFile(newDirectory + "/" + tempFile2, branchHead, repo));
      Assert.assertArrayEquals(tempBytes3, GitFileUtils.readFile(newDirectory + "/" + tempFile3, branchHead, repo));
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
      RevCommit branchHead = CommitUtils.getCommit(branch, repo);
      Assert.assertNotNull(branchHead);
      Assert.assertEquals(existingFileBlob, ObjectUtils.findObject(existingFile, branchHead, repo));
      Assert.assertArrayEquals(tempBytes1, GitFileUtils.readFile(newDirectory + "/" + tempFile1, branchHead, repo));
      Assert.assertArrayEquals(tempBytes2, GitFileUtils.readFile(newDirectory + "/" + tempFile2, branchHead, repo));
      Assert.assertArrayEquals(tempBytes3, GitFileUtils.readFile(newDirectory + "/" + tempFile3, branchHead, repo));
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
      RevCommit branchHead = CommitUtils.getCommit(branch, repo);
      Assert.assertNotNull(branchHead);
      Assert.assertEquals(existingFileBlob, ObjectUtils.findObject(existingFile, branchHead, repo));
      Assert.assertArrayEquals(tempBytes1, GitFileUtils.readFile(newDirectory + "/" + tempFile1, branchHead, repo));
      Assert.assertArrayEquals(tempBytes2, GitFileUtils.readFile(newDirectory + "/" + tempFile2, branchHead, repo));
      Assert.assertArrayEquals(tempBytes3, GitFileUtils.readFile(newDirectory + "/" + tempFile3, branchHead, repo));
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
    RevCommit branchHead = CommitUtils.getCommit(branch, repo);
    Assert.assertNotNull(branchHead);
    Assert.assertNotNull(ObjectUtils.findObject(file1, branchHead, repo));
    Assert.assertNull(ObjectUtils.findObject(file2, branchHead, repo));
    Assert.assertNull(ObjectUtils.findObject(file3, branchHead, repo));
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
    RevCommit branchHead = CommitUtils.getCommit(branch, repo);
    Assert.assertNotNull(branchHead);
    Assert.assertArrayEquals(bytes, GitFileUtils.readFile(existingFile, branchHead, repo));
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
    RevCommit branchHead = CommitUtils.getCommit(branch, repo);
    Assert.assertNotNull(branchHead);
    Assert.assertArrayEquals(Constants.encode(content), GitFileUtils.readFile(existingFile, branchHead, repo));
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
    RevCommit branchHead = CommitUtils.getCommit(branch, repo);
    Assert.assertNotNull(branchHead);
    Assert.assertArrayEquals(bytes, GitFileUtils.readFile(existingFile, branchHead, repo));
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
      RevCommit branchHead = CommitUtils.getCommit(branch, repo);
      Assert.assertNotNull(branchHead);
      Assert.assertArrayEquals(bytes, GitFileUtils.readFile(existingFile, branchHead, repo));
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
      RevCommit branchHead = CommitUtils.getCommit(branch, repo);
      Assert.assertNotNull(branchHead);
      Assert.assertArrayEquals(bytes, GitFileUtils.readFile(existingFile, branchHead, repo));
    } finally {
      Assert.assertTrue(Files.deleteIfExists(tempFilePath));
    }
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
    RevCommit branchHead = CommitUtils.getCommit(branch, repo);
    Assert.assertNotNull(branchHead);
    Assert.assertEquals(message, branchHead.getFullMessage());
    Assert.assertArrayEquals(bytes, GitFileUtils.readFile(newFile, branchHead, repo));
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
    RevCommit newCommit = CommitUtils.getCommit(commitId, repo);
    Assert.assertNotNull(ObjectUtils.findObject(existingFile, newCommit, repo));
    Assert.assertEquals(parentRevision, newCommit.getParent(0));
    Assert.assertArrayEquals(bytes, GitFileUtils.readFile(newFile, newCommit, repo));
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
    RevCommit newCommit = CommitUtils.getCommit(commitId, repo);
    Assert.assertNotNull(ObjectUtils.findObject(existingFile, newCommit, repo));
    Assert.assertEquals(parentRevision, newCommit.getParent(0));
    Assert.assertArrayEquals(bytes, GitFileUtils.readFile(newFile, newCommit, repo));
  }

}
