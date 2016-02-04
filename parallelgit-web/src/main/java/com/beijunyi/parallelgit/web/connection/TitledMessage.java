package com.beijunyi.parallelgit.web.connection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TitledMessage {

  private static final TitledMessage READY = new TitledMessage("ready");

  private String title;
  private MessageData data;

  public TitledMessage() {
  }

  public TitledMessage(@Nonnull String title) {
    this.title = title;
  }

  public TitledMessage(@Nonnull String title, @Nonnull MessageData data) {
    this.title = title;
    this.data = data;
  }

  @Nonnull
  public static TitledMessage ready() {
    return READY;
  }

  @Nonnull
  public static TitledMessage response(@Nonnull String type, @Nonnull String rid, @Nullable String target, @Nonnull Object data) {
    return new TitledMessage(type, new MessageData(rid, target, data));
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public MessageData getData() {
    return data;
  }

  public void setData(MessageData data) {
    this.data = data;
  }
}
