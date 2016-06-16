package com.beijunyi.parallelgit.filesystem.merge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

import org.eclipse.jgit.diff.Sequence;
import org.eclipse.jgit.merge.MergeFormatter;
import org.eclipse.jgit.merge.MergeResult;
import org.eclipse.jgit.merge.ResolveMerger;

import static com.beijunyi.parallelgit.filesystem.utils.GfsPathUtils.toAbsolutePath;
import static java.util.Collections.unmodifiableMap;
import static org.eclipse.jgit.lib.Constants.CHARSET;

public class MergeConflict {

  private final MergeResult<? extends Sequence> result;
  private final String[] names;

  private MergeConflict(MergeResult<? extends Sequence> result, String[] names) {
    this.result = result;
    this.names = names;
  }

  @Nonnull
  public static Map<String, MergeConflict> readConflicts(ResolveMerger merger) {
    Map<String, MergeConflict> ret = new HashMap<>();
    for(Map.Entry<String, MergeResult<? extends Sequence>> conflict : merger.getMergeResults().entrySet())
      ret.put(toAbsolutePath(conflict.getKey()), new MergeConflict(conflict.getValue(), merger.getCommitNames()));
    return unmodifiableMap(ret);
  }

  @Nonnull
  public byte[] format(MergeFormatter formatter) {
    try(ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
      formatter.formatMerge(stream, result, names[0], names[1], names[2], CHARSET.name());
      return stream.toByteArray();
    } catch(IOException e) {
      throw new IllegalStateException(e);
    }
  }

}
