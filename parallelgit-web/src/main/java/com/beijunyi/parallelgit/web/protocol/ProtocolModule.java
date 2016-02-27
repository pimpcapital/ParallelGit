package com.beijunyi.parallelgit.web.protocol;

import java.util.Arrays;
import java.util.Collection;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class ProtocolModule extends AbstractModule {

  private static final Collection<Class<? extends RequestHandler>> HANDLERS = Arrays.asList(
    CheckoutHandler.class,
    CopyFileHandler.class,
    CreateFileHandler.class,
    CreateDirectoryHandler.class,
    DeleteFileHandler.class,
    ReadBlobHandler.class,
    GetFileAttributesHandler.class,
    GetStatusHandler.class,
    ListBranchesHandler.class,
    ListFilesHandler.class,
    LoginHandler.class,
    MoveFileHandler.class,
    PreviewCommitHandler.class,
    ReadFileHandler.class,
    RenameFileHandler.class,
    WriteFileHandler.class
  );

  @Override
  protected void configure() {
    Multibinder<RequestHandler> binder = Multibinder.newSetBinder(binder(), RequestHandler.class);
    for(Class<? extends RequestHandler> handler : HANDLERS)
      binder.addBinding().to(handler);
  }
}
