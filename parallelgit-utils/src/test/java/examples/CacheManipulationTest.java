package examples;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.CacheUtils;
import com.beijunyi.parallelgit.utils.ObjectUtils;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CacheManipulationTest extends AbstractParallelGitTest {

  @Before
  public void prepareExample() throws IOException {
    initRepository();
    writeToCache("/existing_file.txt");
  }

  @Test
  public void insertFile() throws IOException {
    DirCache cache = DirCache.newInCore();

    byte[] fileContent = "This is an example".getBytes();
    AnyObjectId fileBlob = ObjectUtils.insertBlob(fileContent, repo);
    CacheUtils.addFile("/my_file.txt", fileBlob, cache);

    //check
    assertTrue(CacheUtils.entryExists("/my_file.txt", cache));                           // the file exists
    assertEquals(fileBlob, CacheUtils.getBlob("/my_file.txt", cache));                   // the blob is correct
  }

  @Test
  public void deleteFile() throws IOException {
    DirCache cache = DirCache.newInCore();                                               // create a new cache
    CacheUtils.addFile("/my_file.txt", someObjectId(), cache);                           // prepare "my_file.txt"

    CacheUtils.deleteFile("/my_file.txt", cache);                                        // delete the file

    //check
    assertFalse(CacheUtils.entryExists("/my_file.txt", cache));                          // the file does not exist
  }

  @Test
  public void updateFile() throws IOException {
    DirCache cache = DirCache.newInCore();
    CacheUtils.addFile("/my_file.txt", someObjectId(), cache);                           // prepare "my_file.txt"

    byte[] fileContent = "This is an example".getBytes();
    AnyObjectId fileBlob = ObjectUtils.insertBlob(fileContent, repo);
    CacheUtils.updateFileBlob("/my_file.txt", fileBlob, cache);

    //check
    assertTrue(CacheUtils.entryExists("/my_file.txt", cache));                           // the file exists
    assertEquals(fileBlob, CacheUtils.getBlob("/my_file.txt", cache));                   // the blob is correct
  }

}
