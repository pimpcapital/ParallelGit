package com.beijunyi.parallelgit.web.connection;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import com.beijunyi.parallelgit.web.workspace.GitUser;
import com.beijunyi.parallelgit.web.workspace.Workspace;
import com.beijunyi.parallelgit.web.workspace.WorkspaceManager;

@ServerEndpoint(value = "/ws", decoders = JsonDecoder.class, encoders = JsonEncoder.class, configurator = WebSocketEndPointConfigurator.class)
public class WebSocketEndPoint {

  private final WorkspaceManager workspaces;

  private String id;
  private Workspace workspace;

  @Inject
  public WebSocketEndPoint(@Nonnull WorkspaceManager workspaces) {
    this.workspaces = workspaces;
  }

  @OnOpen
  public void handleConnection(@Nonnull Session session) {
    id = session.getId();
  }

  @OnMessage
  public void handleRequest(@Nonnull TitledMessage msg, @Nonnull Session session) throws EncodeException, IOException {
    switch(msg.getTitle()) {
      case "login":
        processLogin(msg.getData(), session);
        break;
      default:
        throw new UnsupportedOperationException();
    }
  }

  @OnClose
  public void handleClose() {
    workspaces.destroyWorkspace(id);
  }

  private void processLogin(@Nonnull MessageData data, @Nonnull Session session) throws EncodeException, IOException {
    GitUser user = new GitUser(data);
    workspace = workspaces.prepareWorkspace(id, user);
    session.getBasicRemote().sendObject(TitledMessage.ready());
  }

}
