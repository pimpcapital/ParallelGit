package com.beijunyi.parallelgit.web.connection;

import java.io.IOException;
import java.util.Map;
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
      case "update":
        processUpdate(msg.getData(), session);
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
    session.getBasicRemote().sendObject(TitledMessage.ready());
  }

  private void processRequest(@Nonnull MessageData msg, @Nonnull Session session) throws EncodeException, IOException {
    DataRequest request = new DataRequest(msg);
    Object data = workspace.getData(request);
    session.getBasicRemote().sendObject(TitledMessage.resource(request.getType(), request.getRequestId(), data));
  }

  private void processUpdate(@Nonnull MessageData msg, @Nonnull Session session) throws EncodeException, IOException {
    DataUpdate update = new DataUpdate(msg);
    Map<String, Object> updated = workspace.updateData(update);
    for(Map.Entry<String, Object> entry : updated.entrySet()) {
      String type = entry.getKey();
      Object data = entry.getValue();
      session.getBasicRemote().sendObject(TitledMessage.resource(type, null, data));
    }
  }

}
