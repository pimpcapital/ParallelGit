package com.beijunyi.parallelgit.web.connection;

import javax.annotation.Nonnull;

public class TitledMessage {

  private static final TitledMessage READY = new TitledMessage("ready");

  private String title;
  private MessageData data;

  public TitledMessage() {
  }

  public TitledMessage(@Nonnull String title) {
    this.title = title;
  }

  @Nonnull
  public static TitledMessage ready() {
    return READY;
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
