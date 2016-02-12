package com.beijunyi.parallelgit.web.security;

import java.util.Set;

import javax.annotation.Nonnull;

import com.google.inject.Inject;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

public class ConfigurableSecurityManager extends DefaultWebSecurityManager {

  @Inject
  public ConfigurableSecurityManager(@Nonnull Set<Realm> realms) {
    super(realms);
  }

}
