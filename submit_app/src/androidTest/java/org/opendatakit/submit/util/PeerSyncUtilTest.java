package org.opendatakit.submit.util;

import android.net.Uri;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PeerSyncUtilTest {
  private static final String TAG = PeerSyncUtilTest.class.getSimpleName();

  @Test
  public void buildPeerSyncUrl() {
    Uri uri = PeerSyncUtil.buildPeerSyncUrl("//127.0.0.1", "submit_default");

    assertEquals("peer://127.0.0.1/submit_default", uri.toString());
  }
}
