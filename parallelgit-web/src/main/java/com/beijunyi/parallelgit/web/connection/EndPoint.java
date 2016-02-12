package com.beijunyi.parallelgit.web.connection;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import com.beijunyi.parallelgit.web.protocol.ClientRequest;
import com.beijunyi.parallelgit.web.protocol.RequestDelegate;
import com.beijunyi.parallelgit.web.protocol.ServerResponse;
import com.beijunyi.parallelgit.web.workspace.Workspace;
import com.beijunyi.parallelgit.web.workspace.WorkspaceManager;

@ServerEndpoint(value = "/api/ws", decoders = JsonDecoder.class, encoders = JsonEncoder.class, configurator = EndPointConfigurator.class)
public class EndPoint {

  public static final String SUBJECT_KEY = "subject";

  private final RequestDelegate delegate;
  private final Workspace workspace;

  @Inject
  public EndPoint(@Nonnull RequestDelegate delegate, @Nonnull WorkspaceManager workspaceManager) {
    this.delegate = delegate;
    this.workspace = workspaceManager.prepareWorkspace();
  }

  @OnOpen
  public void handleConnection(@Nonnull Session session) {
    session.setMaxTextMessageBufferSize(64 * 1024);
  }

  @OnMessage
  public void handleRequest(@Nonnull ClientRequest request, @Nonnull Session session) throws EncodeException, IOException {
    ServerResponse response = delegate.handle(request, workspace);
    session.getBasicRemote().sendObject(response);
  }

  @OnClose
  public void handleClose() throws IOException {
    workspace.destroy();
  }

}
