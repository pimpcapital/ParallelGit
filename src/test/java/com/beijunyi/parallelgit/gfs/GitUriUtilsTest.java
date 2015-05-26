package com.beijunyi.parallelgit.gfs;

import java.net.URI;
import java.nio.file.ProviderMismatchException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class GitUriUtilsTest {

  @Test(expected = ProviderMismatchException.class)
  public void checkSchemeTest() {
    GitUriUtils.checkScheme(URI.create("c:/windows/path"));
  }

  @Test
  public void decodeUriParamsTest() {
    Map<String, Object> params = GitUriUtils.getParams(URI.create("gfs:///repo?revision=master&bare=true"));
    Assert.assertEquals("master", params.get(GitFileSystemProvider.REVISION_KEY));
    Assert.assertEquals("true", params.get(GitFileSystemProvider.BARE_KEY));
  }

  @Test
  public void decodeUnixUriRepoPathTest() {
    Assert.assertEquals("/unix/path", GitUriUtils.getRepoPath(URI.create("gfs:///unix/path")));
    Assert.assertEquals("/unix/path", GitUriUtils.getRepoPath(URI.create("gfs:///unix/path/")));
    Assert.assertEquals("/unix/path", GitUriUtils.getRepoPath(URI.create("gfs:///unix/path!/a.txt")));
  }

  @Test
  public void decodeWindowsUriRepoPathTest() {
    Assert.assertEquals("c:/windows/path", GitUriUtils.getRepoPath(URI.create("gfs://c:/windows/path")));
    Assert.assertEquals("c:/windows/path", GitUriUtils.getRepoPath(URI.create("gfs://c:/windows/path/")));
    Assert.assertEquals("c:/windows/path", GitUriUtils.getRepoPath(URI.create("gfs://c:/windows/path!/a.txt")));
  }

  @Test
  public void getFileInRepoTest() {
    Assert.assertEquals("/", GitUriUtils.getFileInRepo(URI.create("gfs:///repo!/")));
    Assert.assertEquals("/", GitUriUtils.getFileInRepo(URI.create("gfs:///repo!")));
    Assert.assertEquals("/a.txt", GitUriUtils.getFileInRepo(URI.create("gfs:///repo!/a.txt")));
    Assert.assertEquals("/a/b.txt", GitUriUtils.getFileInRepo(URI.create("gfs:///repo!/a/b.txt")));
    Assert.assertEquals("/a/b.txt", GitUriUtils.getFileInRepo(URI.create("gfs:///repo!a/b.txt")));
    Assert.assertEquals("/a/b", GitUriUtils.getFileInRepo(URI.create("gfs:///repo!a/b")));
    Assert.assertEquals("/a/b", GitUriUtils.getFileInRepo(URI.create("gfs:///repo!/a/b/")));
  }

  @Test
  public void createUriWithFileInRepo() {
    Assert.assertEquals("gfs:///repo!/file.txt", GitUriUtils.createUri("/repo", "/file.txt", null, null, null, null, null, null).toString());
  }

  @Test
  public void createUriWithoutFileInRepo() {
    Assert.assertEquals("gfs:///repo", GitUriUtils.createUri("/repo", null, null, null, null, null, null, null).toString());
  }

  @Test
  public void createUriWithEmptyFileInRepo() {
    Assert.assertEquals("gfs:///repo", GitUriUtils.createUri("/repo", "", null, null, null, null, null, null).toString());
  }

  @Test
  public void createUriWithRootFileInRepo() {
    Assert.assertEquals("gfs:///repo", GitUriUtils.createUri("/repo", "/", null, null, null, null, null, null).toString());
  }

  @Test
  public void createUriWithSessionParam() {
    Assert.assertEquals("gfs:///repo!/file.txt?session=session_id", GitUriUtils.createUri("/repo", "/file.txt", "session_id", null, null, null, null, null).toString());
  }

  @Test
  public void createUriWithBareParam() {
    Assert.assertEquals("gfs:///repo!/file.txt?bare=true", GitUriUtils.createUri("/repo", "/file.txt", null, true, null, null, null, null).toString());
  }

  @Test
  public void createUriWithCreateParam() {
    Assert.assertEquals("gfs:///repo!/file.txt?create=true", GitUriUtils.createUri("/repo", "/file.txt", null, null, true, null, null, null).toString());
  }

  @Test
  public void createUriWithBranchParam() {
    Assert.assertEquals("gfs:///repo!/file.txt?branch=branch_name", GitUriUtils.createUri("/repo", "/file.txt", null, null, null, "branch_name", null, null).toString());
  }

  @Test
  public void createUriWithRevisionParam() {
    Assert.assertEquals("gfs:///repo!/file.txt?revision=revision_id", GitUriUtils.createUri("/repo", "/file.txt", null, null, null, null, "revision_id", null).toString());
  }

  @Test
  public void createUriWithTreeParam() {
    Assert.assertEquals("gfs:///repo!/file.txt?tree=tree_id", GitUriUtils.createUri("/repo", "/file.txt", null, null, null, null, null, "tree_id").toString());
  }

  @Test
  public void createUriWithAllParams() {
    Assert.assertEquals("gfs:///repo!/file.txt?session=session_id&bare=false&create=false&branch=branch_name&revision=revision_id&tree=tree_id", GitUriUtils.createUri("/repo", "/file.txt", "session_id", false, false, "branch_name", "revision_id", "tree_id").toString());
  }

  @Test
  public void createUriWithParamMap() {
    Map<String, Object> paramMap = new LinkedHashMap<>();
    paramMap.put(GitFileSystemProvider.BARE_KEY, true);
    paramMap.put(GitFileSystemProvider.REVISION_KEY, "revision_id");
    Assert.assertEquals("gfs:///repo!/file.txt?bare=true&revision=revision_id", GitUriUtils.createUri("/repo", "/file.txt", paramMap).toString());
  }

  @Test
  public void createUriWithNullValueInParamMap() {
    Map<String, Object> paramMap = new LinkedHashMap<>();
    paramMap.put(GitFileSystemProvider.BARE_KEY, true);
    paramMap.put(GitFileSystemProvider.REVISION_KEY, null);
    Assert.assertEquals("gfs:///repo!/file.txt?bare=true", GitUriUtils.createUri("/repo", "/file.txt", paramMap).toString());
  }

  @Test
  public void createUriWithEmptyValueInParamMap() {
    Map<String, Object> paramMap = new LinkedHashMap<>();
    paramMap.put(GitFileSystemProvider.BARE_KEY, true);
    paramMap.put(GitFileSystemProvider.REVISION_KEY, "");
    Assert.assertEquals("gfs:///repo!/file.txt?bare=true", GitUriUtils.createUri("/repo", "/file.txt", paramMap).toString());
  }

  @Test
  public void createUriWithEmptyKeyInParamMap() {
    Map<String, Object> paramMap = new LinkedHashMap<>();
    paramMap.put(GitFileSystemProvider.BARE_KEY, true);
    paramMap.put("", "value");
    Assert.assertEquals("gfs:///repo!/file.txt?bare=true", GitUriUtils.createUri("/repo", "/file.txt", paramMap).toString());
  }

  @Test
  public void createUriWithNullKeyInParamMap() {
    Map<String, Object> paramMap = new LinkedHashMap<>();
    paramMap.put(GitFileSystemProvider.BARE_KEY, true);
    paramMap.put(null, "value");
    Assert.assertEquals("gfs:///repo!/file.txt?bare=true", GitUriUtils.createUri("/repo", "/file.txt", paramMap).toString());
  }

}
