package usecase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.junit.Assert;
import org.junit.Test;

public class FilesSizeTest extends AbstractGitFileSystemTest {

  @Nonnull
  private byte[] bytesOfSize(int size) {
    byte[] data = new byte[size];
    Arrays.fill(data, (byte) '.');
    return data;
  }

  @Test
  public void sizeOfDirectory_shouldReturnZero() throws IOException {
    initRepository();
    writeToCache("/dir/file.txt");
    commitToMaster();
    initGitFileSystem();
    Assert.assertEquals(0, Files.size(gfs.getPath("/dir")));
  }

  @Test
  public void sizeOfFile_shouldReturnTheLengthOfItsByteArray() throws IOException {
    initRepository();
    writeToCache("/file.txt", bytesOfSize(15));
    commitToMaster();
    initGitFileSystem();
    Assert.assertEquals(15, Files.size(gfs.getPath("/file.txt")));
  }

  @Test(expected = NoSuchFileException.class)
  public void sizeOfNonExistentFile_shouldThrowNoSuchFileException() throws IOException {
    initGitFileSystem();
    Files.size(gfs.getPath("/non_existent_file.txt"));
  }

}
