package org.opendatakit.submit.service.peer.server.handlers;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

public abstract class GetHandler extends BaseHandler implements RouterNanoHTTPD.UriResponder {
  @Override
  public NanoHTTPD.Response put(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
    return errorResponse(NanoHTTPD.Response.Status.BAD_REQUEST, null);
  }

  @Override
  public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
    return errorResponse(NanoHTTPD.Response.Status.BAD_REQUEST, null);
  }

  @Override
  public NanoHTTPD.Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
    return errorResponse(NanoHTTPD.Response.Status.BAD_REQUEST, null);
  }

  @Override
  public NanoHTTPD.Response other(String method, RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
    return errorResponse(NanoHTTPD.Response.Status.BAD_REQUEST, null);
  }
}
