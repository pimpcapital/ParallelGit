package usecases;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class FilesNewInputStreamTest extends AbstractGitFileSystemTest {

  @Test
  public void newInputStreamOnFile_shouldBeNotNull() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();
    try(InputStream stream = Files.newInputStream(gfs.getPath("/file.txt"))) {
      assertNotNull(stream);
    }
  }
}
