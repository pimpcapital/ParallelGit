package integration;

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
  public void sizeOfDirectory_shouldReturnTheSumOfDescendantFileSizes() throws IOException {
    initRepository();
    writeFile("/dir/file1.txt", bytesOfSize(1));
    writeFile("/dir/file2.txt", bytesOfSize(3));
    writeFile("/dir/subdir/file3.txt", bytesOfSize(5));
    commitToMaster();
    initGitFileSystem();
    Assert.assertEquals(1 + 3 + 5, Files.size(gfs.getPath("/dir")));
  }

  @Test
  public void sizeOfFile_shouldReturnTheLengthOfItsByteArray() throws IOException {
    initRepository();
    writeFile("/file.txt", bytesOfSize(15));
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
