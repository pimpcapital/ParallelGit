package com.beijunyi.parallelgit.web.security.config;

import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.web.DynamicConfig;
import org.apache.commons.configuration.ConfigurationException;

import static com.beijunyi.parallelgit.web.security.SecurityModule.MODULE_DIR;

public class LdapConfig extends DynamicConfig {

  public static final String URL_KEY = "url";
  public static final String SEARCH_BASE_KEY = "searchBase";

  private static final Path CONFIG_FILE = MODULE_DIR.resolve("ldap-authentication.properties");

  private LdapConfig() {
  }

  @Nonnull
  public static LdapConfig bindFile() {
    LdapConfig ret = new LdapConfig();
    try {
      ret.bind(CONFIG_FILE);
    } catch(ConfigurationException | IOException e) {
      throw new RuntimeException(e);
    }
    return ret;
  }

  @Override
  protected void loadDefaultConfig() {
    setUrl("ldap://");
    setSearchBase("CN=,DC=");
  }

  @Nonnull
  public String getUrl() {
    String ret = getString(URL_KEY);
    if(ret == null)
      throw new IllegalStateException();
    return ret;
  }

  public void setUrl(@Nonnull String url) {
    setProperty(URL_KEY, url);
  }

  @Nonnull
  public String getSearchBase() {
    String ret = getString(SEARCH_BASE_KEY);
    if(ret == null)
      throw new IllegalStateException();
    return ret;
  }

  public void setSearchBase(@Nonnull String domain) {
    setProperty(SEARCH_BASE_KEY, domain);
  }

}
