package com.beijunyi.parallelgit.web.security;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.web.workspace.User;

public class SecurityService {

  @Nonnull
  public User authenticate(@Nonnull String username, @Nonnull String email) {
    return new User(username, email);
  } 

}
