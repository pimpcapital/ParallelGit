package com.beijunyi.parallelgit.utils;

import java.util.Comparator;
import java.util.Date;
import javax.annotation.Nonnull;

import org.eclipse.jgit.revwalk.RevTag;

public class RevTagTimestampComparator implements Comparator<RevTag> {

  private final boolean ascending;

  public RevTagTimestampComparator(boolean ascending) {
    this.ascending = ascending;
  }

  public RevTagTimestampComparator() {
    this(false);
  }

  @Override
  public int compare(@Nonnull RevTag tag1, @Nonnull RevTag tag2) {
    Date timestamp1 = tag1.getTaggerIdent().getWhen();
    Date timestamp2 = tag2.getTaggerIdent().getWhen();
    return ascending ? timestamp1.compareTo(timestamp2) : timestamp2.compareTo(timestamp1);
  }

}
