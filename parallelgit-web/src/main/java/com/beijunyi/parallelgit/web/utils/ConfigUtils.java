package com.beijunyi.parallelgit.web.utils;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.context.filesprovider.ConfigFilesProvider;
import org.cfg4j.source.files.FilesConfigurationSource;
import org.cfg4j.source.reload.strategy.PeriodicalReloadStrategy;

public class ConfigUtils {

  @Nonnull
  public static <T> T bindConfig(@Nonnull Class<T> type, @Nonnull Path file) {
    ConfigurationProvider provider = new ConfigurationProviderBuilder()
                                       .withConfigurationSource(new SingletonConfigFileSource(file))
                                       .withReloadStrategy(new PeriodicalReloadStrategy(5, TimeUnit.SECONDS))
                                       .build();
    return provider.bind("", type);
  }

  private static class SingletonConfigFileProvider implements ConfigFilesProvider {
    private final Path file;

    public SingletonConfigFileProvider(@Nonnull Path file) {
      this.file = file;
    }

    @Nonnull
    @Override
    public Iterable<Path> getConfigFiles() {
      return file;
    }
  }

  private static class SingletonConfigFileSource extends FilesConfigurationSource {
    public SingletonConfigFileSource(@Nonnull Path file) {
      super(new SingletonConfigFileProvider(file));
    }
  }


}
