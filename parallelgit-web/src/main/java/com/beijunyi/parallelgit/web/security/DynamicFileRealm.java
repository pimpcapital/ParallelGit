package com.beijunyi.parallelgit.web.security;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.google.common.io.Resources;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.text.PropertiesRealm;

import static com.beijunyi.parallelgit.web.security.SecurityModule.MODULE_DIR;

public class DynamicFileRealm extends PropertiesRealm {

  private static final String DEFAULT_FILE_AUTHENTICATION_PROPERTIES = ("default-file-authentication.properties");
  private static final Path FILE_AUTHENTICATION_PROPERTIES = MODULE_DIR.resolve("file-authentication.properties");

  private final SecurityService securityService;

  @Inject
  public DynamicFileRealm(@Nonnull SecurityService securityService) throws IOException {
    this.securityService = securityService;
    setResourcePath(prepareAuthenticationFile());
    setReloadIntervalSeconds(5);
    startReloadThread();
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    if(!securityService.isFileAuthenticationEnabled())
      return null;
    return super.doGetAuthenticationInfo(token);
  }

  @Nonnull
  private static String prepareAuthenticationFile() throws IOException {
    if(!Files.exists(FILE_AUTHENTICATION_PROPERTIES)) {
      URL defaultConfig = Resources.getResource(DEFAULT_FILE_AUTHENTICATION_PROPERTIES);
      Files.createDirectories(FILE_AUTHENTICATION_PROPERTIES.getParent());
      Files.write(FILE_AUTHENTICATION_PROPERTIES, Resources.toByteArray(defaultConfig));
    }
    return FILE_AUTHENTICATION_PROPERTIES.toUri().toString();
  }
}
