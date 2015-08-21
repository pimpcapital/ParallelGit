package integration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.junit.Assert;
import org.junit.Test;

public class FilesNewInputStreamTest extends AbstractGitFileSystemTest {

  @Test
  public void newInputStreamOnFile_shouldBeNotNull() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();
    try(InputStream inputStream = Files.newInputStream(gfs.getPath("/file.txt"))) {
      Assert.assertNotNull(inputStream);
    }
  }
}
