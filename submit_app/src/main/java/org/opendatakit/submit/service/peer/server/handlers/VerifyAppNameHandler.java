package org.opendatakit.submit.service.peer.server.handlers;

import org.opendatakit.submit.consts.SubmitConsts;
import org.opendatakit.submit.service.peer.server.handlers.GetHandler;
import org.opendatakit.utilities.ODKFileUtils;

import java.io.File;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

public class VerifyAppNameHandler extends GetHandler {
  @Override
  public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
    String appName = getAppName(session);

    if (appName != null && appName.startsWith(SubmitConsts.SECONDARY_APP_NAME_PREFIX)) {
      File appDir = new File(ODKFileUtils.getAppFolder(getAppName(session)));

      if (appDir.exists() && appDir.isDirectory()) {
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "OK");
      }
    }

    return errorResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "invalid appName");
  }
}
