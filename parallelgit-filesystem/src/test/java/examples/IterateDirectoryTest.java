package examples;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.utils.GitFileSystems;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IterateDirectoryTest extends AbstractParallelGitTest {

  @Before
  public void prepareExample() throws IOException {
    initRepository();
    writeFilesToCache("/dir/file1.txt");
    writeFilesToCache("/dir/file2.txt");
    commitToBranch("my_branch");
  }

  @Test
  public void iterateDirectory() throws IOException {
    try(GitFileSystem gfs = GitFileSystems.forRevision("my_branch", repo)) {             // open git file system
      Path dir = gfs.getPath("/dir");                                                    // convert string to nio path
      try(DirectoryStream<Path> children = Files.newDirectoryStream(dir)) {              // open directory stream

        // check
        Iterator<Path> childrenIterator = children.iterator();                           // prepare to iterate children
        assertEquals("/dir/file1.txt", childrenIterator.next().toString());              // the first child is "file1.txt"
        assertEquals("/dir/file2.txt", childrenIterator.next().toString());              // the second child is "file2.txt"
        assertFalse(childrenIterator.hasNext());                                         // there is no more child
      }
    }
  }

}
