package com.beijunyi.parallelgit.filesystem.merge;

import java.io.IOException;
import java.util.Map;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.io.GfsDefaultCheckout;
import org.eclipse.jgit.merge.MergeFormatter;

import static org.eclipse.jgit.lib.FileMode.REGULAR_FILE;

public class GfsMergeCheckout extends GfsDefaultCheckout {

  private Map<String, MergeConflict> conflicts;
  private MergeFormatter formatter;

  private GfsMergeCheckout(GitFileSystem gfs) {
    super(gfs);
  }

  public static GfsMergeCheckout merge(GitFileSystem gfs) {
    return new GfsMergeCheckout(gfs);
  }

  @Nonnull
  public GfsMergeCheckout handleConflicts(Map<String, MergeConflict> conflicts) {
    this.conflicts = conflicts;
    return this;
  }

  @Nonnull
  public GfsMergeCheckout withFormatter(MergeFormatter formatter) {
    this.formatter = formatter;
    return this;
  }

  @Override
  protected boolean skips(String path) {
    return super.skips(path) || (conflicts != null && conflicts.containsKey(path));
  }

  @Override
  protected void applyChanges() throws IOException {
    addFormattedConflicts();
    super.applyChanges();
  }

  private void addFormattedConflicts() throws IOException {
    if(conflicts != null) {
      for(Map.Entry<String, MergeConflict> conflict : conflicts.entrySet()) {
        String path = conflict.getKey();
        byte[] formatted = conflict.getValue().format(formatter);
        changes.addChange(path, formatted, REGULAR_FILE);
      }
    }
  }

}
