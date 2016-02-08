package com.beijunyi.parallelgit.web.workspace;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.web.connection.MessageData;

public class User {

  private final String username;
  private final String email;

  public User(@Nonnull String username, @Nonnull String email) {
    this.username = username;
    this.email = email;
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
