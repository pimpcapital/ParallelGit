package com.beijunyi.parallelgit.web.protocol;

import java.util.*;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.web.workspace.Workspace;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestDelegate {

  private static final Logger LOG = LoggerFactory.getLogger(RequestDelegate.class);

  private final Map<String, RequestHandler> lookup;

  @Inject
  public RequestDelegate(@Nonnull Set<RequestHandler> handlers) {
    lookup = indexHandlers(handlers);
  }

  @Nonnull
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull Workspace workspace) {
    RequestHandler ret = lookup.get(request.getType());
    if(ret == null)
      return request.respond().error("Unsupported operation \"" + request.getType() + "\"");
    try {
      return ret.handle(request, workspace);
    } catch(Throwable e) {
      LOG.error("Unknown error", e);
      return request.respond().error(e.getMessage());
    }
  }

  @Nonnull
  private static Map<String, RequestHandler> indexHandlers(@Nonnull Set<RequestHandler> handlers) {
    Map<String, RequestHandler> ret = new HashMap<>();
    for(RequestHandler handler : handlers)
      ret.put(handler.getType(), handler);
    return Collections.unmodifiableMap(ret);
  }

}
