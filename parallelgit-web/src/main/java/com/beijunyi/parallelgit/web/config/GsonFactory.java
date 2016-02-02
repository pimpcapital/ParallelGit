package com.beijunyi.parallelgit.web.config;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.io.GfsFileAttributeView;
import com.google.gson.*;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

import static com.beijunyi.parallelgit.filesystem.io.GfsFileAttributeView.*;

public class GsonFactory {

  @Nonnull
  public static Gson getInstance() {
    return GsonHolder.INSTANCE;
  }

  private static class GsonHolder {
    private static final Gson INSTANCE =
      new GsonBuilder()
        .registerTypeAdapter(AnyObjectId.class, GitObjectSerializer.class)
        .create();
  }

  private static class GitObjectSerializer implements JsonSerializer<AnyObjectId> {

    @Nonnull
    @Override
    public JsonElement serialize(@Nonnull AnyObjectId src, @Nonnull Type typeOfSrc, @Nonnull JsonSerializationContext context) {
      return new JsonPrimitive(src.getName());
    }

  }

  private static class FileModeSerializer implements JsonSerializer<FileMode> {

    @Nonnull
    @Override
    public JsonElement serialize(@Nonnull FileMode src, @Nonnull Type typeOfSrc, @Nonnull JsonSerializationContext context) {
      if(FileMode.REGULAR_FILE.equals(src) || FileMode.EXECUTABLE_FILE.equals(src))
        return new JsonPrimitive("FILE");
      if(FileMode.TREE.equals(src))
        return new JsonPrimitive("DIRECTORY");
      throw new UnsupportedOperationException(src.toString());
    }
  }

  private static class GitFileAttributeViewSerializer implements JsonSerializer<GfsFileAttributeView.Git> {

    private static final Collection<String> KEYS =
      Arrays.asList(
        IS_NEW,
        IS_MODIFIED,
        OBJECT_ID,
        FILE_MODE
      );

    @Override
    public JsonElement serialize(@Nonnull GfsFileAttributeView.Git src, @Nonnull Type typeOfSrc, @Nonnull JsonSerializationContext context) {
      try {
        return context.serialize(src.readAttributes(KEYS));
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

}
