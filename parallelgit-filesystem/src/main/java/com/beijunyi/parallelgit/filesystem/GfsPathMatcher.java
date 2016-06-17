package com.beijunyi.parallelgit.filesystem;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.utils.GitGlobs;

public class GfsPathMatcher implements PathMatcher {

  private static final String GLOB_SYNTAX = "glob";
  private static final String REGEX_SYNTAX = "regex";

  private final Pattern pattern;

  private GfsPathMatcher(Pattern pattern) {
    this.pattern = pattern;
  }

  @Nonnull
  public static GfsPathMatcher newMatcher(Pattern pattern) {
    return new GfsPathMatcher(pattern);
  }

  @Nonnull
  public static GfsPathMatcher newMatcher(String syntax, String pattern) {
    String expr;
    if(syntax.equals(GLOB_SYNTAX)) {
      expr = GitGlobs.toRegexPattern(pattern);
    } else {
      if(syntax.equals(REGEX_SYNTAX)) expr = pattern;
      else throw new UnsupportedOperationException("Syntax '" + syntax + "' not recognized");
    }
    return newMatcher(Pattern.compile(expr));
  }

  @Nonnull
  public static GfsPathMatcher newMatcher(String syntaxPattern) {
    int pos = syntaxPattern.indexOf(':');
    if(pos <= 0 || pos == syntaxPattern.length()) throw new IllegalArgumentException(syntaxPattern);
    String syntax = syntaxPattern.substring(0, pos);
    String pattern = syntaxPattern.substring(pos + 1);
    return newMatcher(syntax, pattern);
  }

  @Override
  public boolean matches(Path path) {
    return pattern.matcher(path.toString()).matches();
  }

}
