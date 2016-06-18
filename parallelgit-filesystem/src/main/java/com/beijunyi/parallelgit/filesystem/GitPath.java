package com.beijunyi.parallelgit.filesystem;

import java.io.File;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.utils.GfsUriBuilder;

import static org.eclipse.jgit.lib.Constants.CHARSET;
import static org.eclipse.jgit.util.RawParseUtils.decode;

public class GitPath implements Path {

  private static ThreadLocal<SoftReference<CharsetEncoder>> encoder = new ThreadLocal<>();

  private final GitFileSystem gfs;
  private final byte[] path;

  private volatile int[] offsets;
  private volatile String stringValue;

  GitPath(GitFileSystem gfs, byte[] path) {
    this.gfs = gfs;
    this.path = path;
  }

  GitPath(GitFileSystem gfs, String input) {
    this(gfs, encode(normalizeAndCheck(input)));
  }

  @Nonnull
  private static String normalizeAndCheck(String input) {
    int n = input.length();
    char prevChar = 0;
    for(int i = 0; i < n; i++) {
      char c = input.charAt(i);
      if((c == '/') && (prevChar == '/'))
        return normalize(input, n, i - 1);
      checkNotNul(input, c);
      prevChar = c;
    }
    if(prevChar == '/')
      return normalize(input, n, n - 1);
    return input;
  }

  private static void checkNotNul(String input, char c) {
    if(c == '\u0000')
      throw new InvalidPathException(input, "Nul character not allowed");
  }

  @Nonnull
  private static String normalize(String input, int len, int off) {
    if(len == 0)
      return input;
    int n = len;
    while((n > 0) && (input.charAt(n - 1) == '/')) 
      n--;
    if(n == 0)
      return "/";
    StringBuilder sb = new StringBuilder(input.length());
    if(off > 0)
      sb.append(input.substring(0, off));
    char prevChar = 0;
    for(int i=off; i < n; i++) {
      char c = input.charAt(i);
      if((c == '/') && (prevChar == '/'))
        continue;
      checkNotNul(input, c);
      sb.append(c);
      prevChar = c;
    }
    return sb.toString();
  }

  @Nonnull
  private static byte[] encode(String input) {
    SoftReference<CharsetEncoder> ref = encoder.get();
    CharsetEncoder ce = (ref != null) ? ref.get() : null;
    if(ce == null) {
      ce = Charset.defaultCharset()
             .newEncoder()
             .onMalformedInput(CodingErrorAction.REPORT)
             .onUnmappableCharacter(CodingErrorAction.REPORT);
      encoder.set(new SoftReference<>(ce));
    }

    char[] ca = input.toCharArray();

    // size output buffer for worse-case size
    byte[] ba = new byte[(int)(ca.length * (double)ce.maxBytesPerChar())];

    // encode
    ByteBuffer bb = ByteBuffer.wrap(ba);
    CharBuffer cb = CharBuffer.wrap(ca);
    ce.reset();
    CoderResult cr = ce.encode(cb, bb, true);
    boolean error;
    if(!cr.isUnderflow()) 
      error = true;
    else {
      cr = ce.flush(bb);
      error = !cr.isUnderflow();
    }
    if(error)
      throw new InvalidPathException(input, "Malformed input or input contains unmappable chacraters");

    // trim result to actual length if required
    int len = bb.position();
    if(len != ba.length)
      ba = Arrays.copyOf(ba, len);

    return ba;
  }

  @Nonnull
  @Override
  public GitFileSystem getFileSystem() {
    return gfs;
  }

  @Nonnull
  public GfsFileStore getFileStore() {
    return gfs.getFileStore();
  }

  @Override
  public boolean isAbsolute() {
    return path.length > 0 && path[0] == '/';
  }

  @Nonnull
  @Override
  public GitPath getRoot() {
    return getFileSystem().getRootPath();
  }

  @Nullable
  @Override
  public GitPath getFileName() {
    initOffsets();
    int count = offsets.length;

    if(count == 0)
      return null;

    // one name element and no root component
    if(count == 1 && path.length > 0 && path[0] != '/')
      return this;

    int lastOffset = offsets[count - 1];
    int len = path.length - lastOffset;
    byte[] result = new byte[len];
    System.arraycopy(path, lastOffset, result, 0, len);
    return new GitPath(getFileSystem(), result);
  }

  @Nullable
  @Override
  public GitPath getParent() {
    initOffsets();

    int count = offsets.length;
    if(count == 0)
      return null;

    int len = offsets[count - 1] - 1;
    if(len < 0)
      return null;
    if(len == 0)
      return gfs.getRootPath();

    byte[] result = new byte[len];
    System.arraycopy(path, 0, result, 0, len);
    return new GitPath(gfs, result);
  }

  @Override
  public int getNameCount() {
    initOffsets();
    return offsets.length;
  }

  @Nonnull
  @Override
  public GitPath getName(int index) {
    initOffsets();
    if(index < 0)
      throw new IllegalArgumentException();
    if(index >= offsets.length)
      throw new IllegalArgumentException();

    int begin = offsets[index];
    int len;
    if(index == (offsets.length-1)) 
      len = path.length - begin;
    else 
      len = offsets[index+1] - begin - 1;

    // construct result
    byte[] result = new byte[len];
    System.arraycopy(path, begin, result, 0, len);
    return new GitPath(getFileSystem(), result);
  }

  @Nonnull
  @Override
  public GitPath subpath(int beginIndex, int endIndex) {
    initOffsets();

    if(beginIndex < 0)
      throw new IllegalArgumentException();
    if(beginIndex > offsets.length)
      throw new IllegalArgumentException();
    if(endIndex > offsets.length)
      throw new IllegalArgumentException();
    if(beginIndex > endIndex)
      throw new IllegalArgumentException();
    if(beginIndex == endIndex)
      return emptyPath();

    // starting offset and length
    int begin = offsets[beginIndex];
    int len;
    if(endIndex == offsets.length) {
      len = path.length - begin;
    } else {
      len = offsets[endIndex] - begin - 1;
    }

    // construct result
    byte[] result = new byte[len];
    System.arraycopy(path, begin, result, 0, len);
    return new GitPath(getFileSystem(), result);
  }

  @Override
  public boolean startsWith(Path other) {
    GitPath that = (GitPath) other;

    // other path is longer
    if(that.path.length > path.length)
      return false;

    int thisOffsetCount = getNameCount();
    int thatOffsetCount = that.getNameCount();

    // other path has no name elements
    if(thatOffsetCount == 0 && this.isAbsolute())
      return !that.isEmpty();

    // given path has more elements that this path
    if(thatOffsetCount > thisOffsetCount)
      return false;

    // same number of elements so must be exact match
    if((thatOffsetCount == thisOffsetCount) && (path.length != that.path.length))
      return false;

    // check offsets of elements match
    for (int i=0; i<thatOffsetCount; i++) {
      Integer o1 = offsets[i];
      Integer o2 = that.offsets[i];
      if(!o1.equals(o2))
        return false;
    }

    // offsets match so need to compare bytes
    int i=0;
    while (i < that.path.length) {
      if(this.path[i] != that.path[i])
        return false;
      i++;
    }

    // final check that match is on name boundary
    return !(i < path.length && this.path[i] != '/');

  }

  @Override
  public boolean startsWith(String other) {
    return startsWith(getFileSystem().getPath(other));
  }

  @Override
  public boolean endsWith(Path other) {
    GitPath that = (GitPath) other;

    int thisLen = path.length;
    int thatLen = that.path.length;

    // other path is longer
    if(thatLen > thisLen)
      return false;

    // other path is the empty path
    if(thisLen > 0 && thatLen == 0)
      return false;

    // other path is absolute so this path must be absolute
    if(that.isAbsolute() && !this.isAbsolute())
      return false;

    int thisOffsetCount = getNameCount();
    int thatOffsetCount = that.getNameCount();

    // given path has more elements that this path
    if(thatOffsetCount > thisOffsetCount)
      return false;
    else {
      // same number of elements
      if(thatOffsetCount == thisOffsetCount) {
        if(thisOffsetCount == 0)
          return true;
        int expectedLen = thisLen;
        if(this.isAbsolute() && !that.isAbsolute())
          expectedLen--;
        if(thatLen != expectedLen)
          return false;
      } else {
        // this path has more elements so given path must be relative
        if(that.isAbsolute())
          return false;
      }
    }

    // compare bytes
    int thisPos = offsets[thisOffsetCount - thatOffsetCount];
    int thatPos = that.offsets[0];
    if((thatLen - thatPos) != (thisLen - thisPos))
      return false;
    while (thatPos < thatLen) {
      if(this.path[thisPos++] != that.path[thatPos++])
        return false;
    }

    return true;
  }

  @Override
  public boolean endsWith(String other) {
    return endsWith(getFileSystem().getPath(other));
  }

  @Nonnull
  @Override
  public GitPath normalize() {
    final int count = getNameCount();
    if(count == 0)
      return this;

    boolean[] ignore = new boolean[count];      // true => ignore name
    int[] size = new int[count];                // length of name
    int remaining = count;                      // number of names remaining
    boolean hasDotDot = false;                  // has at least one ..
    boolean isAbsolute = isAbsolute();

    // first pass:
    //   1. compute length of names
    //   2. mark all occurrences of "." to ignore
    //   3. and look for any occurrences of ".."
    for(int i = 0; i < count; i++) {
      int begin = offsets[i];
      int len;
      if(i == (offsets.length - 1))
        len = path.length - begin;
      else
        len = offsets[i+1] - begin - 1;

      size[i] = len;

      if(path[begin] == '.') {
        if(len == 1) {
          ignore[i] = true;  // ignore  "."
          remaining--;
        }
        else {
          if(path[begin+1] == '.')   // ".." found
            hasDotDot = true;
        }
      }
    }

    // multiple passes to eliminate all occurrences of name/..
    if(hasDotDot) {
      int prevRemaining;
      do {
        prevRemaining = remaining;
        int prevName = -1;
        for (int i=0; i<count; i++) {
          if(ignore[i])
            continue;

          // not a ".."
          if(size[i] != 2) {
            prevName = i;
            continue;
          }

          int begin = offsets[i];
          if(path[begin] != '.' || path[begin + 1] != '.') {
            prevName = i;
            continue;
          }

          // ".." found
          if(prevName >= 0) {
            // name/<ignored>/.. found so mark name and ".." to be
            // ignored
            ignore[prevName] = true;
            ignore[i] = true;
            remaining = remaining - 2;
            prevName = -1;
          } else {
            // Case: /<ignored>/.. so mark ".." as ignored
            if(isAbsolute) {
              boolean hasPrevious = false;
              for (int j=0; j<i; j++) {
                if(!ignore[j]) {
                  hasPrevious = true;
                  break;
                }
              }
              if(!hasPrevious) {
                // all proceeding names are ignored
                ignore[i] = true;
                remaining--;
              }
            }
          }
        }
      } while (prevRemaining > remaining);
    }

    // no redundant names
    if(remaining == count)
      return this;

    // corner case - all names removed
    if(remaining == 0)
      return isAbsolute ? getFileSystem().getRootPath() : emptyPath();

    // compute length of result
    int len = remaining - 1;
    if(isAbsolute)
      len++;

    for (int i=0; i<count; i++) {
      if(!ignore[i])
        len += size[i];
    }
    byte[] result = new byte[len];

    // copy names into result
    int pos = 0;
    if(isAbsolute)
      result[pos++] = '/';
    for (int i=0; i<count; i++) {
      if(!ignore[i]) {
        System.arraycopy(path, offsets[i], result, pos, size[i]);
        pos += size[i];
        if(--remaining > 0) {
          result[pos++] = '/';
        }
      }
    }
    return new GitPath(getFileSystem(), result);
  }

  @Nonnull
  private static byte[] resolve(byte[] base, byte[] child) {
    int baseLength = base.length;
    int childLength = child.length;
    if(childLength == 0)
      return base;
    if(baseLength == 0 || child[0] == '/')
      return child;
    byte[] result;
    if(baseLength == 1 && base[0] == '/') {
      result = new byte[childLength + 1];
      result[0] = '/';
      System.arraycopy(child, 0, result, 1, childLength);
    } else {
      result = new byte[baseLength + 1 + childLength];
      System.arraycopy(base, 0, result, 0, baseLength);
      result[base.length] = '/';
      System.arraycopy(child, 0, result, baseLength+1, childLength);
    }
    return result;
  }

  @Nonnull
  @Override
  public GitPath resolve(Path path) {
    GitPath gitPath = (GitPath) path;
    byte[] other = gitPath.path;
    if(other.length > 0 && other[0] == '/')
      return gitPath;
    byte[] result = resolve(this.path, other);
    return new GitPath(getFileSystem(), result);
  }

  @Override
  public GitPath resolve(String pathStr) {
    return resolve(getFileSystem().getPath(pathStr));
  }

  @Override
  public GitPath resolveSibling(Path path) {
    GitPath other = (GitPath) path;
    GitPath parent = getParent();
    return (parent == null) ? other : parent.resolve(other);
  }

  @Override
  public GitPath resolveSibling(String path) {
    return resolveSibling(getFileSystem().getPath(path));
  }

  @Nonnull
  @Override
  public GitPath relativize(Path path) throws IllegalArgumentException {
    GitPath other = (GitPath) path;
    if(other.equals(this))
      return emptyPath();

    if(isAbsolute() != other.isAbsolute())
      throw new IllegalArgumentException("The path to be relativize is different type of path: " + other);

    if(isEmpty())
      return other;

    int bn = getNameCount();
    int cn = other.getNameCount();

    // skip matching names
    int n = (bn > cn) ? cn : bn;
    int i = 0;
    while(i < n) {
      if(!this.getName(i).equals(other.getName(i)))
        break;
      i++;
    }

    int dotdots = bn - i;
    if(i < cn) {
      // remaining name components in other
      GitPath remainder = other.subpath(i, cn);
      if(dotdots == 0)
        return remainder;

      // result is a  "../" for each remaining name in base
      // followed by the remaining names in other. If the remainder is
      // the empty path then we don't add the final trailing slash.
      int len = dotdots * 3 + remainder.path.length;
      byte[] result = new byte[len];
      int pos = 0;
      while (dotdots > 0) {
        result[pos++] = (byte)'.';
        result[pos++] = (byte)'.';
        result[pos++] = (byte)'/';
        dotdots--;
      }
      System.arraycopy(remainder.path, 0, result, pos, remainder.path.length);
      return new GitPath(getFileSystem(), result);
    } else {
      // no remaining names in other so result is simply a sequence of ".."
      byte[] result = new byte[dotdots * 3 - 1];
      int pos = 0;
      while (dotdots > 0) {
        result[pos++] = (byte)'.';
        result[pos++] = (byte)'.';
        // no tailing slash at the end
        if(dotdots > 1)
          result[pos++] = (byte)'/';
        dotdots--;
      }
      return new GitPath(getFileSystem(), result);
    }
  }

  @Nonnull
  @Override
  public URI toUri() {
    return GfsUriBuilder.fromFileSystem(gfs).file(this).build();
  }

  @Nonnull
  @Override
  public GitPath toAbsolutePath() {
    if(isAbsolute())
      return this;
    return getRoot().resolve(this);
  }

  @Nonnull
  @Override
  public GitPath toRealPath(@Nullable LinkOption... options) {
    return toAbsolutePath().normalize();
  }

  @Nullable
  @Override
  public File toFile() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public WatchKey register(@Nullable WatchService watcher, @Nullable WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public WatchKey register(@Nullable WatchService watcher, WatchEvent.Kind<?>... events) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public Iterator<Path> iterator() {
    return new Iterator<Path>() {
      private int i = 0;
      @Override
      public boolean hasNext() {
        return i < getNameCount();
      }
      @Override
      public Path next() {
        if(i < getNameCount()) {
          Path result = getName(i);
          i++;
          return result;
        } else
          throw new NoSuchElementException();
      }
      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public int compareTo(Path other) {
    GitPath that = (GitPath) other;

    int len1 = path.length;
    int len2 = that.path.length;

    int n = Math.min(len1, len2);
    byte v1[] = path;
    byte v2[] = that.path;

    int k = 0;
    while (k < n) {
      int c1 = v1[k] & 0xff;
      int c2 = v2[k] & 0xff;
      if(c1 != c2)
        return c1 - c2;
      k++;
    }
    return len1 - len2;
  }

  @Nonnull
  @Override
  public String toString() {
    if(stringValue == null)stringValue = decode(CHARSET, path);
    return stringValue;
  }

  private void initOffsets() {
    if(offsets == null) {
      int count, index;

      // count names
      count = 0;
      index = 0;
      if(!isEmpty()) {
        while (index < path.length) {
          byte c = path[index++];
          if(c != '/') {
            count++;
            while (index < path.length && path[index] != '/')
              index++;
          }
        }
      }

      // populate offsets
      int[] result = new int[count];
      count = 0;
      index = 0;
      while (index < path.length) {
        byte c = path[index];
        if(c == '/')
          index++;
        else {
          result[count++] = index++;
          while(index < path.length && path[index] != '/')
            index++;
        }
      }
      synchronized (this) {
        if(offsets == null)
          offsets = result;
      }
    }
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if(this == obj)
      return true;

    if(obj == null || getClass() != obj.getClass())
      return false;

    GitPath gitPath = (GitPath)obj;

    return gfs.equals(gitPath.gfs) && Arrays.equals(path, gitPath.path);
  }

  @Override
  public int hashCode() {
    return 31 * gfs.hashCode() + Arrays.hashCode(path);
  }

  public boolean isEmpty() {
    return path.length == 0;
  }

  public boolean isRoot() {
    return path.length == 1 && path[0] == '/';
  }

  @Nonnull
  private GitPath emptyPath() {
    return new GitPath(gfs, new byte[0]);
  }

}
