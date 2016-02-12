package com.beijunyi.parallelgit.web.security;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.beijunyi.parallelgit.web.security.config.SecurityConfig;

public class SecurityService {

  private final SecurityConfig config;

  @Inject
  public SecurityService(@Nonnull SecurityConfig config) {
    this.config = config;
  }

  public boolean isFileAuthenticationEnabled() {
    return config.isFileAuthenticationEnabled();
  }

  public boolean isLdapAuthenticationEnabled() {
    return config.isLdapAuthenticationEnabled();
  }

}
