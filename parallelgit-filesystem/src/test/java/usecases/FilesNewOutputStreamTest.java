package usecases;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class FilesNewOutputStreamTest extends AbstractGitFileSystemTest {

  @Test
  public void newOutputStreamOnFile_shouldBeNotNull() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();
    try(OutputStream outputStream = Files.newOutputStream(gfs.getPath("/file.txt"))) {
      assertNotNull(outputStream);
    }
  }
}
