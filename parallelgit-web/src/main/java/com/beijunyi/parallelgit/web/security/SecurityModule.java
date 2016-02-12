package com.beijunyi.parallelgit.web.security;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import com.beijunyi.parallelgit.web.AppConstants;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.apache.shiro.realm.Realm;

import static com.beijunyi.parallelgit.web.AppConstants.APP_HOME;

public class SecurityModule extends AbstractModule {

  public static final Path MODULE_DIR = APP_HOME.resolve("security");

  private static final Collection<Class<? extends DynamicRealm>> REALMS = Arrays.<Class<? extends DynamicRealm>>asList(
    DynamicPropertiesRealm.class,
    DynamicActiveDirectoryRealm.class
  );

  @Override
  protected void configure() {
    Multibinder<Realm> binder = Multibinder.newSetBinder(binder(), Realm.class);
    for(Class<? extends Realm> realm : REALMS)
      binder.addBinding().to(realm);
  }

}
