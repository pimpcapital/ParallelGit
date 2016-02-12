package com.beijunyi.parallelgit.web.security;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.web.security.config.LdapConfig;
import com.google.inject.Inject;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.activedirectory.ActiveDirectoryRealm;

public class DynamicLdapRealm extends ActiveDirectoryRealm {

  private final LdapConfig config;
  private final SecurityService securityService;

  @Inject
  public DynamicLdapRealm(@Nonnull LdapConfig config, @Nonnull SecurityService securityService) {
    this.config = config;
    this.securityService = securityService;
    applyConfig();
  }

  @Nullable
  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(@Nonnull AuthenticationToken token) throws AuthenticationException {
    if(!securityService.isLdapAuthenticationEnabled())
      return null;
    return super.doGetAuthenticationInfo(token);
  }

  private void applyConfig() {
    setUrl(config.getUrl());
    setSearchBase(config.getSearchBase());
  }

}
