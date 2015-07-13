package com.beijunyi.parallelgit.filesystems;

import java.io.File;
import java.net.URI;
import java.nio.file.ProviderMismatchException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class GitUriUtils {

  /**
   * Checks if the scheme of the given {@code URI} is equal (without regard to case) to {@link
   * GitFileSystemProvider#GIT_FS_SCHEME}.
   *
   * @param   uri
   *          the {@code URI} to check
   * @throws  ProviderMismatchException
   *          if the given {@code URI} is not equal to {@link GitFileSystemProvider#GIT_FS_SCHEME}
   */
  static void checkScheme(@Nonnull URI uri) throws ProviderMismatchException {
    if(!GitFileSystemProvider.GIT_FS_SCHEME.equalsIgnoreCase(uri.getScheme()))
      throw new ProviderMismatchException(uri.getScheme());
  }

  /**
   * Finds and returns the path to the repository from the given {@code URI}.
   *
   * Within the {@code GitFileSystem} {@code URI} pattern:
   *   git://[repo location]![path in repo (optional)]?[parameters (optional)]
   * this method returns the [repo location] part of the {@code URI}.
   *
   * @param   uri
   *          the {@code URI}
   * @return  the string path to the repository
   */
  @Nonnull
  static String getRepository(@Nonnull URI uri) {
    checkScheme(uri);

    String pathStr = uri.getPath();
    int rootIdx = pathStr.indexOf(GitFileSystemProvider.ROOT_SEPARATOR);
    if(rootIdx != -1)
      pathStr = pathStr.substring(0, rootIdx);
    if(pathStr.endsWith("/"))
      pathStr = pathStr.substring(0, pathStr.length() - 1);

    return pathStr;
  }

  /**
   * Finds and returns the path to the file in the repository from the given {@code URI}.
   *
   * Within the {@code GitFileSystem} {@code URI} pattern:
   *   git://[repo location]![file in repo (optional)]?[parameters (optional)]
   * this method returns the [file in repo] part of the {@code URI}. If this part is absent, the default path is "/".
   *
   * @param   uri
   *          the {@code URI}
   * @return  the string path to the file in the repository
   */
  @Nonnull
  static String getFile(@Nonnull URI uri) throws ProviderMismatchException {
    checkScheme(uri);

    String fileInRepo = "/";

    String pathStr = uri.getPath();
    int bangIndex = pathStr.indexOf(GitFileSystemProvider.ROOT_SEPARATOR);
    if(bangIndex == -1)
      return fileInRepo;

    int start = bangIndex + 1;
    if(pathStr.length() > start && pathStr.charAt(start) == '/')
      start++;
    int end = pathStr.length();
    if(pathStr.endsWith("/"))
      end--;
    if(start > end)
      return fileInRepo;

    fileInRepo += pathStr.substring(start, end);
    return fileInRepo;
  }

  @Nonnull
  static Map<String, String> parseQuery(@Nonnull String query) {
    Map<String, String> params = new HashMap<>();
    String[] keyValueStrs = query.split("&");
    for(String keyValueStr : keyValueStrs) {
      String[] keyValuePair = keyValueStr.split("=", 2);
      params.put(keyValuePair[0], keyValuePair.length > 1 ? keyValuePair[1] : null);
    }
    return params;
  }

  @Nonnull
  static URI createUri(@Nonnull String repoPath,
                       @Nullable String fileInRepo,
                       @Nullable String sessionId,
                       @Nullable Boolean bare,
                       @Nullable Boolean create,
                       @Nullable String branch,
                       @Nullable String revision,
                       @Nullable String tree) {

    Map<String, Object> paramMap = new LinkedHashMap<>();
    paramMap.put(GitFileSystemProvider.SESSION_KEY, sessionId);
    paramMap.put(GitFileSystemProvider.BRANCH_KEY, branch);
    paramMap.put(GitFileSystemProvider.REVISION_KEY, revision);
    paramMap.put(GitFileSystemProvider.TREE_KEY, tree);

    return createUri(repoPath, fileInRepo, paramMap);
  }

  @Nonnull
  static URI createUri(@Nonnull File repo,
                        @Nullable String fileInRepo,
                        @Nullable String sessionId,
                        @Nullable Boolean bare,
                        @Nullable Boolean create,
                        @Nullable String branch,
                        @Nullable String revision,
                        @Nullable String tree) {
    return createUri(getNormalizedPath(repo), fileInRepo, sessionId, bare, create, branch, revision, tree);
  }

  /**
   * Creates a {@code GitFileSystem} {@code URI}.
   *
   * @param   repo
   *          the repository directory
   * @param   fileInRepo
   *          the string path to the file in the repository
   * @param   paramMap
   *          the configuration parameters
   * @return  the result {@code URI}
   */
  @Nonnull
  static URI createUri(@Nonnull File repo, @Nullable String fileInRepo, @Nullable Map<String, Object> paramMap) {
    return createUri(getNormalizedPath(repo), fileInRepo, paramMap);
  }

  /**
   * Creates a {@code GitFileSystem} {@code URI}.
   *
   * @param   repoPath
   *          the string path to the repository directory
   * @param   fileInRepo
   *          the string path to the file in the repository
   * @param   paramMap
   *          the configuration parameters
   * @return  the result {@code URI}
   */
  @Nonnull
  static URI createUri(@Nonnull String repoPath, @Nullable String fileInRepo, @Nullable Map<String, Object> paramMap) {
    String path = GitFileSystemProvider.GIT_FS_SCHEME + "://" + repoPath;
    if(fileInRepo != null && !(fileInRepo = fileInRepo.trim()).isEmpty() && !fileInRepo.equals("/"))
      path += GitFileSystemProvider.ROOT_SEPARATOR + fileInRepo;

    StringBuilder params = new StringBuilder();
    if(paramMap != null) {
      for(Map.Entry<String, Object> param : paramMap.entrySet()) {
        String key = param.getKey();
        if(key == null || (key = key.trim()).isEmpty())
          continue;
        Object value = param.getValue();
        if(value == null || ((String) (value = value.toString().trim())).isEmpty())
          continue;
        if(params.length() > 0)
          params.append("&");
        params.append(key).append("=").append(value);
      }
    }

    if(params.length() > 0)
      path += "?" + params;

    return URI.create(path);
  }

  /**
   * Converts the given {@code File} to a {@code URI} compatible string path.
   *
   * @param   file
   *          the file to convert
   * @return  the result path string
   */
  @Nonnull
  private static String getNormalizedPath(@Nonnull File file) {
    return file.toURI().getPath();
  }

}
