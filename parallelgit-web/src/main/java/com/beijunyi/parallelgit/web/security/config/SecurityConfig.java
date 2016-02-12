package com.beijunyi.parallelgit.web.security.config;

import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.web.DynamicConfig;
import org.apache.commons.configuration.ConfigurationException;

import static com.beijunyi.parallelgit.web.security.SecurityModule.MODULE_DIR;

public class SecurityConfig extends DynamicConfig {

  public static final String AUTHENTICATION_KEY = "authentication";
  public static final String AUTHENTICATION_FILE_KEY = AUTHENTICATION_KEY + ".file";
  public static final String AUTHENTICATION_FILE_ENABLED_KEY = AUTHENTICATION_FILE_KEY + ".enabled";
  public static final String AUTHENTICATION_LDAP_KEY = AUTHENTICATION_KEY + ".ldap";
  public static final String AUTHENTICATION_LDAP_ENABLED_KEY = AUTHENTICATION_LDAP_KEY + ".enabled";

  private static final Path MAIN_PROPERTIES = MODULE_DIR.resolve("main.properties");

  private SecurityConfig() {
  }

  @Nonnull
  public static SecurityConfig bindFile() {
    SecurityConfig ret = new SecurityConfig();
    try {
      ret.bind(MAIN_PROPERTIES);
    } catch(ConfigurationException | IOException e) {
      throw new RuntimeException(e);
    }
    return ret;
  }

  @Override
  protected void loadDefaultConfig() {
    setFileAuthenticationEnabled(true);
    setLdapAuthenticationEnabled(false);
  }

  public boolean isFileAuthenticationEnabled() {
    return getBoolean(AUTHENTICATION_FILE_ENABLED_KEY);
  }

  public void setFileAuthenticationEnabled(boolean flag) {
    setProperty(AUTHENTICATION_FILE_ENABLED_KEY, flag);
  }

  public boolean isLdapAuthenticationEnabled() {
    return getBoolean(AUTHENTICATION_LDAP_ENABLED_KEY);
  }

  public void setLdapAuthenticationEnabled(boolean flag) {
    setProperty(AUTHENTICATION_LDAP_ENABLED_KEY, flag);
  }

}
