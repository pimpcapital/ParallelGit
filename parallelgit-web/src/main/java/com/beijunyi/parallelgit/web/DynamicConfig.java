package com.beijunyi.parallelgit.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import com.google.common.io.Files;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

public abstract class DynamicConfig extends PropertiesConfiguration {

  public void bind(@Nonnull File properties) throws IOException, ConfigurationException {
    setFile(properties);
    if(!properties.exists()) {
      loadDefaultConfig();
      Files.createParentDirs(properties);
      save();
    } else
      load();

    setReloadingStrategy(new FileChangedReloadingStrategy());
    setAutoSave(true);
  }

  public void bind(@Nonnull Path properties) throws IOException, ConfigurationException {
    bind(properties.toFile());
  }

  protected abstract void loadDefaultConfig();


}
