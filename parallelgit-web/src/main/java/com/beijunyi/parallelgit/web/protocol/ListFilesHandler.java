package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.web.protocol.model.FileAttributes;
import com.beijunyi.parallelgit.web.workspace.Workspace;

public class ListFilesHandler extends AbstractGfsRequestHandler {

  @Override
  public String getType() {
    return "list-files";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull GitFileSystem gfs) throws IOException {
    List<FileAttributes> ret = new ArrayList<>();
    try(DirectoryStream<Path> stream = Files.newDirectoryStream(gfs.getPath(request.getString("path")))) {
      for(Path child : stream) {
        ret.add(FileAttributes.read(child));
      }
    }
    return request.respond().ok(ret);
  }
}
