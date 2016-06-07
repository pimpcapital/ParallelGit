package com.beijunyi.parallelgit.utils;

import org.junit.Assert;
import org.junit.Test;

public class TreeUtilsNormalizeNodePathTest {

  @Test
  public void normalizeAbsolutePath_shouldRemoveItsLeadingSlash() {
    String origin = "/a/b/c";
    Assert.assertEquals("a/b/c", TreeUtils.normalizeNodePath(origin));
  }

  @Test
  public void normalizeDirectoryPath_shouldRemoveItsTrailingSlash() {
    String origin = "a/b/c/";
    Assert.assertEquals("a/b/c", TreeUtils.normalizeNodePath(origin));
  }

}
