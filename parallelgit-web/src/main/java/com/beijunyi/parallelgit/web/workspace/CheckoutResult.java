package com.beijunyi.parallelgit.web.workspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.commands.GfsCheckoutCommand;

public class CheckoutResult {

  private final boolean successful;
  private final List<String> conflicts;

  private CheckoutResult(boolean successful, @Nullable List<String> conflicts) {
    this.successful = successful;
    this.conflicts = conflicts;
  }

  @Nonnull
  public static CheckoutResult success() {
    return new CheckoutResult(true, null);
  }

  @Nonnull
  public static CheckoutResult checkoutConflicts(@Nonnull List<String> conflicts) {
    return new CheckoutResult(true, conflicts);
  }

  @Nonnull
  public static CheckoutResult wrap(@Nonnull GfsCheckoutCommand.Result result) {
    if(result.isSuccessful())
      return success();
    List<String> conflicts = new ArrayList<>(result.getConflicts().keySet());
    Collections.sort(conflicts);
    return checkoutConflicts(conflicts);
  }

  public boolean isSuccessful() {
    return successful;
  }

  @Nullable
  public List<String> getConflicts() {
    return conflicts;
  }

}
