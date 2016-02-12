package com.beijunyi.parallelgit.web.security;

import java.nio.file.Path;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.web.security.config.SecurityConfig;
import com.beijunyi.parallelgit.web.utils.ConfigUtils;

import static com.beijunyi.parallelgit.web.security.SecurityModule.MODULE_DIR;

public class SecurityService {

  public static final Path MAIN_CONFIG_FILE = MODULE_DIR.resolve("main.properties");

  private final SecurityConfig mainConfig;

  public SecurityService() {
    this.mainConfig = ConfigUtils.bindConfig(SecurityConfig.class, MAIN_CONFIG_FILE);
  }

  @Nonnull
  public SecurityConfig getMainConfig() {
    return mainConfig;
  }

}
