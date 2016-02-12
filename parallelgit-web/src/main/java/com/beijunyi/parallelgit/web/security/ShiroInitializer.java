package com.beijunyi.parallelgit.web.security;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.beijunyi.parallelgit.web.config.InjectorFactory;
import org.apache.shiro.web.env.DefaultWebEnvironment;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.filter.mgt.*;

@WebListener
public class ShiroInitializer extends EnvironmentLoaderListener implements ServletContextListener {

  @Override
  public WebEnvironment createEnvironment(@Nonnull ServletContext servletContext) throws IllegalStateException {
    DefaultWebEnvironment ret = new DefaultWebEnvironment();
    ret.setFilterChainResolver(prepareFilterChainResolver());
    ret.setSecurityManager(InjectorFactory.getInstance().getInstance(ConfigurableSecurityManager.class));
    return ret;
  }

  @Nonnull
  private static FilterChainResolver prepareFilterChainResolver() {
    FilterChainManager fcManager = new DefaultFilterChainManager();
    setupAnonymousAccess(fcManager);
    setupLogout(fcManager);
    setupLoginFilter(fcManager);
    PathMatchingFilterChainResolver ret = new PathMatchingFilterChainResolver();
    ret.setFilterChainManager(fcManager);
    return ret;
  }

  private static void setupAnonymousAccess(@Nonnull FilterChainManager fcManager) {
    fcManager.addFilter("anonymous", new AnonymousFilter());
    fcManager.createChain("/lib/**", "anonymous");
    fcManager.createChain("/style-overrides/**", "anonymous");
    fcManager.createChain("/favicon.*", "anonymous");

  }

  private static void setupLogout(@Nonnull FilterChainManager fcManager) {
    fcManager.addFilter("logout", new LogoutFilter());
    fcManager.createChain("/logout", "logout");
  }

  private static void setupLoginFilter(@Nonnull FilterChainManager fcManager) {
    FormAuthenticationFilter authenticate = new FormAuthenticationFilter();
    authenticate.setLoginUrl("/login.html");
    fcManager.addFilter("authenticate", authenticate);
    fcManager.createChain("/**", "authenticate");
  }

}
