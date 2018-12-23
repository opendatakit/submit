package org.opendatakit.submit.util;

import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PeerSyncUtilTest {
  private static final String TAG = PeerSyncUtilTest.class.getSimpleName();

  @Test
  public void buildPeerSyncUrl() {
    Uri uri = PeerSyncUtil.buildPeerSyncUrl("//127.0.0.1", "submit_default");

    assertEquals("peer://127.0.0.1/submit_default", uri.toString());
  }
}
