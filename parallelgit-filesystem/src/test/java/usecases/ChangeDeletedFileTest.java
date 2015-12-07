package usecases;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.GitPath;
import com.beijunyi.parallelgit.filesystem.io.Node;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class ChangeDeletedFileTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void changeDeletedFile_shouldNotAffectItsParentDirectory() throws IOException {
    writeSomeFileToCache();
    writeToCache("/dir/test_file.txt");
    RevCommit commit = commit(null);

    try(GitFileSystem gfs = Gfs.newFileSystem(repo).commit(commit).build()) {
      GitPath file = gfs.getPath("/dir/test_file.txt");
      OutputStream out = Files.newOutputStream(file);
      Files.delete(file);
      gfs.flush();
      out.write("some new data".getBytes());
      out.close();
      Node node = gfs.getFileStore().getRoot().getChild("dir");
      assert node != null;
      assertFalse(node.isDirty());
    }
  }

}
