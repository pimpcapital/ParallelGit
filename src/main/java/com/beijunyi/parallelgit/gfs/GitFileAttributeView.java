package com.beijunyi.parallelgit.gfs;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GitFileAttributeView implements BasicFileAttributeView {

  public static final String GIT_FILE_ATTRIBUTE_VIEW_TYPE = "basic";

  public static final FileTime EPOCH = FileTime.fromMillis(0);

  public static final String SIZE_NAME = "size";
  public static final String CREATION_TIME_NAME = "creationTime";
  public static final String LAST_ACCESS_TIME_NAME = "lastAccessTime";
  public static final String LAST_MODIFIED_TIME_NAME = "lastModifiedTime";
  public static final String FILE_KEY_NAME = "fileKey";
  public static final String IS_DIRECTORY_NAME = "isDirectory";
  public static final String IS_REGULAR_FILE_NAME = "isRegularFile";
  public static final String IS_SYMBOLIC_LINK_NAME = "isSymbolicLink";
  public static final String IS_OTHER_NAME = "isOther";

  private static final String[] ALL_NAMES = new String[] {
                                                           SIZE_NAME,
                                                           CREATION_TIME_NAME,
                                                           LAST_ACCESS_TIME_NAME,
                                                           LAST_MODIFIED_TIME_NAME,
                                                           FILE_KEY_NAME,
                                                           IS_DIRECTORY_NAME,
                                                           IS_REGULAR_FILE_NAME,
                                                           IS_SYMBOLIC_LINK_NAME,
                                                           IS_OTHER_NAME
  };

  private final GitFileStore store;
  private final String pathStr;

  public GitFileAttributeView(@Nonnull GitFileStore store, @Nonnull String pathStr) {
    this.store = store;
    this.pathStr = pathStr;
  }

  @Override
  public String name() {
    return GIT_FILE_ATTRIBUTE_VIEW_TYPE;
  }

  /**
   * This method always throws {@link UnsupportedOperationException} as setting time is not supported with the current
   * version.
   *
   * @param lastModifiedTime ignored argument
   * @param lastAccessTime ignored argument
   * @param createTime ignored argument
   * @throws UnsupportedOperationException
   */
  @Override
  public void setTimes(@Nullable FileTime lastModifiedTime, @Nullable FileTime lastAccessTime, @Nullable FileTime createTime) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public GitFileAttributes readAttributes() throws IOException {
    return new GitFileAttributes(readAttributes(ALL_NAMES));
  }

  @Nonnull
  private Map<String, Object> readAttributes(@Nonnull String[] attributes) throws IOException, IllegalArgumentException {
    boolean isRegularFile = store.isRegularFile(pathStr);
    boolean isDirectory = !isRegularFile && store.isDirectory(pathStr);
    if(!isRegularFile && !isDirectory)
      throw new NoSuchFileException(pathStr);

    Map<String, Object> result = new HashMap<>();
    for(String attributeName : attributes) {
      switch(attributeName) {
        case SIZE_NAME:
          result.put(attributeName, store.getFileSize(pathStr));
          break;
        case CREATION_TIME_NAME:
          result.put(attributeName, EPOCH);
          break;
        case LAST_ACCESS_TIME_NAME:
          result.put(attributeName, EPOCH);
          break;
        case LAST_MODIFIED_TIME_NAME:
          result.put(attributeName, EPOCH);
          break;
        case FILE_KEY_NAME:
          result.put(attributeName, null);
          break;
        case IS_DIRECTORY_NAME:
          result.put(attributeName, isDirectory);
          break;
        case IS_REGULAR_FILE_NAME:
          result.put(attributeName, isRegularFile);
          break;
        case IS_SYMBOLIC_LINK_NAME:
          result.put(attributeName, false);
          break;
        case IS_OTHER_NAME:
          result.put(attributeName, false);
          break;
        default:
          throw new IllegalArgumentException("Attribute \"" + attributeName + "\" is not supported");
      }
    }
    return result;
  }

  @Nonnull
  public Map<String, Object> readAttributes(@Nonnull String attributes) throws IOException, IllegalArgumentException {
    return readAttributes(attributes.split(","));
  }
}
