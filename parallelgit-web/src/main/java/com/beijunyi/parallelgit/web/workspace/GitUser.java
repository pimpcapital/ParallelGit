package com.beijunyi.parallelgit.web.workspace;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.web.connection.MessageData;

public class GitUser {

  private final String username;
  private final String email;

  public GitUser(@Nonnull String username, @Nonnull String email) {
    this.username = username;
    this.email = email;
  }

  public GitUser(@Nonnull MessageData msg) {
    this(msg.getString("user"), msg.getString("email"));
  }

  @Nonnull
  public String getUsername() {
    return username;
  }

  @Nonnull
  public String getEmail() {
    return email;
  }
}
