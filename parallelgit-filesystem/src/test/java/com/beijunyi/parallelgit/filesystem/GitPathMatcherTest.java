package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.util.regex.PatternSyntaxException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GitPathMatcherTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFs() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void basicPathMatchingTest() {
    assertTrue(gfs.getPathMatcher("glob:foo.html").matches(gfs.getPath("foo.html")));
    assertFalse(gfs.getPathMatcher("glob:foo.htm").matches(gfs.getPath("foo.html")));
    assertFalse(gfs.getPathMatcher("glob:bar.html").matches(gfs.getPath("foo.html")));
  }

  @Test
  public void matchZeroOrMoreCharacterTest() {
    assertTrue(gfs.getPathMatcher("glob:f*").matches(gfs.getPath("foo.html")));
    assertTrue(gfs.getPathMatcher("glob:*.html").matches(gfs.getPath("foo.html")));
    assertTrue(gfs.getPathMatcher("glob:foo.html*").matches(gfs.getPath("foo.html")));
    assertTrue(gfs.getPathMatcher("glob:*foo.html").matches(gfs.getPath("foo.html")));
    assertTrue(gfs.getPathMatcher("glob:*foo.html*").matches(gfs.getPath("foo.html")));
    assertFalse(gfs.getPathMatcher("glob:*.htm").matches(gfs.getPath("foo.html")));
    assertFalse(gfs.getPathMatcher("glob:f.*").matches(gfs.getPath("foo.html")));
  }

  @Test
  public void matchOneCharacterTest() {
    assertTrue(gfs.getPathMatcher("glob:??o.html").matches(gfs.getPath("foo.html")));
    assertTrue(gfs.getPathMatcher("glob:??o.html").matches(gfs.getPath("foo.html")));
    assertTrue(gfs.getPathMatcher("glob:???.html").matches(gfs.getPath("foo.html")));
    assertTrue(gfs.getPathMatcher("glob:???.htm?").matches(gfs.getPath("foo.html")));
    assertFalse(gfs.getPathMatcher("glob:foo.???").matches(gfs.getPath("foo.html")));
  }

  @Test
  public void groupOfSubPatternsTest() {
    assertTrue(gfs.getPathMatcher("glob:foo{.html,.class}").matches(gfs.getPath("foo.html")));
    assertTrue(gfs.getPathMatcher("glob:foo.{class,html}").matches(gfs.getPath("foo.html")));
    assertFalse(gfs.getPathMatcher("glob:foo{.htm,.class}").matches(gfs.getPath("foo.html")));
  }

  @Test
  public void bracketExpressionsTest() {
    assertTrue(gfs.getPathMatcher("glob:[f]oo.html").matches(gfs.getPath("foo.html")));
    assertTrue(gfs.getPathMatcher("glob:[e-g]oo.html").matches(gfs.getPath("foo.html")));
    assertTrue(gfs.getPathMatcher("glob:[abcde-g]oo.html").matches(gfs.getPath("foo.html")));
    assertTrue(gfs.getPathMatcher("glob:[abcdefx-z]oo.html").matches(gfs.getPath("foo.html")));
    assertTrue(gfs.getPathMatcher("glob:[!a]oo.html").matches(gfs.getPath("foo.html")));
    assertTrue(gfs.getPathMatcher("glob:[!a-e]oo.html").matches(gfs.getPath("foo.html")));
    assertTrue(gfs.getPathMatcher("glob:foo[-a-z]bar").matches(gfs.getPath("foo-bar")));
    assertTrue(gfs.getPathMatcher("glob:foo[!-]html").matches(gfs.getPath("foo.html")));
  }

  @Test
  public void groupsOfSubPatternWithBracketExpressionsTest() {
    assertTrue(gfs.getPathMatcher("glob:[f]oo.{[h]tml,class}").matches(gfs.getPath("foo.html")));
    assertTrue(gfs.getPathMatcher("glob:foo.{[a-z]tml,class}").matches(gfs.getPath("foo.html")));
    assertTrue(gfs.getPathMatcher("glob:foo.{[!a-e]tml,.class}").matches(gfs.getPath("foo.html")));
  }

  @Test
  public void specialCharactersTest() {
    assertTrue(gfs.getPathMatcher("glob:\\{foo*").matches(gfs.getPath("{foo}.html")));
    assertTrue(gfs.getPathMatcher("glob:*\\}.html").matches(gfs.getPath("{foo}.html")));
    assertTrue(gfs.getPathMatcher("glob:\\[foo*").matches(gfs.getPath("[foo].html")));
    assertTrue(gfs.getPathMatcher("glob:*\\].html").matches(gfs.getPath("[foo].html")));
  }

  @Test(expected = PatternSyntaxException.class)
  public void badPatternTest() {
    assertTrue(gfs.getPathMatcher("glob:*[a--z]").matches(gfs.getPath("{foo}.html")));
  }

  @Test
  public void absolutePathTest() {
    assertTrue(gfs.getPathMatcher("glob:/tmp/*").matches(gfs.getPath("/tmp/foo")));
    assertTrue(gfs.getPathMatcher("glob:/tmp/**").matches(gfs.getPath("/tmp/foo/bar")));
  }

  @Test
  public void regexTest() {
    assertTrue(gfs.getPathMatcher("regex:.*\\.html").matches(gfs.getPath("foo.html")));
    assertTrue(gfs.getPathMatcher("regex:foo\\d+").matches(gfs.getPath("foo012")));
    assertTrue(gfs.getPathMatcher("regex:fo\\so").matches(gfs.getPath("fo o")));
    assertTrue(gfs.getPathMatcher("regex:\\w+").matches(gfs.getPath("foo")));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void invalidSyntaxTest() {
    gfs.getPathMatcher("grep:foo");
  }

  @Test(expected = IllegalArgumentException.class)
  public void missingSyntaxTest() {
    gfs.getPathMatcher("foo");
  }
}
