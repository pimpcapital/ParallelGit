package com.beijunyi.parallelgit.filesystem.utils;

import java.util.regex.PatternSyntaxException;
import javax.annotation.Nonnull;

public final class GitGlobs {

  private static final String REGEX_META_CHARS = ".^$+{[]|()";
  private static final String GLOB_META_CHARS = "\\*?[{";
  private static final char EOL = 0;

  private static boolean isRegexMeta(char c) {
    return REGEX_META_CHARS.indexOf(c) != -1;
  }
  private static boolean isGlobMeta(char c) {
    return GLOB_META_CHARS.indexOf(c) != -1;
  }

  private static char charAt(String glob, int i) {
    return i < glob.length() ? glob.charAt(i) : EOL;
  }

  @Nonnull
  public static String toRegexPattern(String globPattern) {
    boolean inGroup = false;
    StringBuilder regex = new StringBuilder("^");

    int i = 0;
    while(i < globPattern.length()) {
      char c = globPattern.charAt(i++);
      switch(c) {
        case '\\':
          // escape special characters
          if(i == globPattern.length()) throw new PatternSyntaxException("No character to escape", globPattern, i - 1);
          char next = globPattern.charAt(i++);
          if(isGlobMeta(next) || isRegexMeta(next)) regex.append('\\');
          regex.append(next);
          break;
        case '/':
          regex.append(c);
          break;
        case '[':
          regex.append("[[^/]&&[");
          if(charAt(globPattern, i) == '^') {
            // escape the regex negation char if it appears
            regex.append("\\^");
            i++;
          } else {
            // negation
            if(charAt(globPattern, i) == '!') {
              regex.append('^');
              i++;
            }
            // hyphen allowed at start
            if(charAt(globPattern, i) == '-') {
              regex.append('-');
              i++;
            }
          }
          boolean hasRangeStart = false;
          char last = 0;
          while(i < globPattern.length()) {
            c = globPattern.charAt(i++);
            if(c == ']') break;
            if(c == '/') throw new PatternSyntaxException("Explicit 'name separator' in class", globPattern, i - 1);
            // TBD: how to specify ']' in a class?
            if(c == '\\' || c == '[' || c == '&' && charAt(globPattern, i) == '&') {
              // escape '\', '[' or "&&" for regex class
              regex.append('\\');
            }
            regex.append(c);

            if(c == '-') {
              if(!hasRangeStart) throw new PatternSyntaxException("Invalid range", globPattern, i - 1);
              if((c = charAt(globPattern, i++)) == EOL || c == ']') break;
              if(c < last) throw new PatternSyntaxException("Invalid range", globPattern, i - 3);
              regex.append(c);
              hasRangeStart = false;
            } else {
              hasRangeStart = true;
              last = c;
            }
          }
          if(c != ']') throw new PatternSyntaxException("Missing ']", globPattern, i - 1);
          regex.append("]]");
          break;
        case '{':
          if(inGroup) throw new PatternSyntaxException("Cannot nest groups", globPattern, i - 1);
          regex.append("(?:(?:");
          inGroup = true;
          break;
        case '}':
          if(inGroup) {
            regex.append("))");
            inGroup = false;
          } else {
            regex.append('}');
          }
          break;
        case ',':
          if(inGroup) regex.append(")|(?:");
          else regex.append(',');
          break;
        case '*':
          if(charAt(globPattern, i) == '*') { // crosses directory boundaries
            regex.append(".*");
            i++;
          } else { // within directory boundary
            regex.append("[^/]*");
          }
          break;
        case '?':
          regex.append("[^/]");
          break;
        default:
          if(isRegexMeta(c)) regex.append('\\');
          regex.append(c);
      }
    }
    if(inGroup) throw new PatternSyntaxException("Missing '}", globPattern, i - 1);
    return regex.append('$').toString();
  }

}
