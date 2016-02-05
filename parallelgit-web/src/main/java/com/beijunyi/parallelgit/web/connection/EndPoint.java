package com.beijunyi.parallelgit.web.connection;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import com.beijunyi.parallelgit.web.workspace.*;

@ServerEndpoint(value = "/ws", decoders = JsonDecoder.class, encoders = JsonEncoder.class, configurator = EndPointConfigurator.class)
public class EndPoint {

  private final WorkspaceManager workspaces;

  private String id;
  private Workspace workspace;

  @Inject
  public EndPoint(@Nonnull WorkspaceManager workspaces) {
    this.workspaces = workspaces;
  }

  @OnOpen
  public void handleConnection(@Nonnull Session session) {
    id = session.getId();
    session.setMaxTextMessageBufferSize(64 * 1024);
  }

  @OnMessage
  public void handleRequest(@Nonnull TitledMessage msg, @Nonnull Session session) throws EncodeException, IOException {
    switch(msg.getTitle()) {
      case "login":
        processLogin(msg.getData(), session);
        break;
      case "request":
        processRequest(msg.getData(), session);
        break;
      default:
        throw new UnsupportedOperationException();
    }
  }

  @OnClose
  public void handleClose() throws IOException {
    workspaces.destroyWorkspace(id);
  }

  private void processLogin(@Nonnull MessageData data, @Nonnull Session session) throws EncodeException, IOException {
    GitUser user = new GitUser(data);
    workspace = workspaces.prepareWorkspace(id, user);
    session.getBasicRemote().sendObject(TitledMessage.ready(user));
  }

  private void processRequest(@Nonnull MessageData msg, @Nonnull Session session) throws EncodeException, IOException {
    WorkspaceRequest request = new WorkspaceRequest(msg);
    Object data = workspace.processRequest(request);
    session.getBasicRemote().sendObject(TitledMessage.response(request.getType(), request.getRequestId(), request.getTarget(), data));
  }

}
