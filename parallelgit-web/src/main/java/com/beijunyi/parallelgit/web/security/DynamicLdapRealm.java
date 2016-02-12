package com.beijunyi.parallelgit.web.security;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.inject.Inject;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.activedirectory.ActiveDirectoryRealm;

public class DynamicLdapRealm extends ActiveDirectoryRealm {

  private final SecurityService securityService;

  @Inject
  public DynamicLdapRealm(@Nonnull SecurityService securityService) {
    this.securityService = securityService;
  }

  @Nullable
  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(@Nonnull AuthenticationToken token) throws AuthenticationException {
    if(!securityService.isLdapAuthenticationEnabled())
      return null;
    return super.doGetAuthenticationInfo(token);
  }

}
