package com.beijunyi.parallelgit.web.protocol;

import java.util.Arrays;
import java.util.Collection;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public final class ProtocolModule extends AbstractModule {

  private static final Collection<Class<? extends RequestHandler>> HANDLERS = Arrays.asList(
    CheckoutHandler.class,
    CreateBranchHandler.class,
    CreateDirectoryHandler.class,
    CopyFileHandler.class,
    CreateFileHandler.class,
    DeleteBranchHandler.class,
    DeleteFileHandler.class,
    GetBranchHeadHandler.class,
    ReadBlobHandler.class,
    GetFileAttributesHandler.class,
    GetFileRevisionsHandler.class,
    GetHeadHandler.class,
    ListBranchesHandler.class,
    ListFilesHandler.class,
    LoginHandler.class,
    MoveFileHandler.class,
    DiffDirectoryHandler.class,
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
