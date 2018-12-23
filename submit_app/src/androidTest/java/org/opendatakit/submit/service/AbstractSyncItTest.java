package org.opendatakit.submit.service;

import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.opendatakit.consts.IntentConsts;
import org.opendatakit.properties.CommonToolProperties;
import org.opendatakit.properties.PropertiesSingleton;
import org.opendatakit.submit.service.peer.PeerSyncServerService;
import org.opendatakit.submit.service.peer.server.PeerSyncServer;
import org.opendatakit.sync.service.OdkSyncServiceInterface;

import java.util.Collections;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public abstract class AbstractSyncItTest extends AbstractAidlSynchronizerTest {
  private static final String TAG = AbstractSyncItTest.class.getSimpleName();

  @Rule
  public final ServiceTestRule syncServiceRule = new ServiceTestRule();

  @Rule
  public final ServiceTestRule syncServerRule = new ServiceTestRule();

  private OdkSyncServiceInterface syncServiceInterface;
  private PeerSyncServer peerSyncServer;

  protected OdkSyncServiceInterface getSyncService() {
    return syncServiceInterface;
  }

  protected PeerSyncServer getPeerSyncServer() {
    return peerSyncServer;
  }

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();

    Log.e(TAG, "setUp: ");
    bindToSync();
    bindToSyncServer();
  }

  @After
  @Override
  public void tearDown() {
    super.tearDown();

    getPeerSyncServer().stop();
  }

  protected void bindToSync() throws TimeoutException {
    IBinder binder = syncServiceRule.bindService(new Intent()
        .setClassName(
            IntentConsts.Sync.APPLICATION_NAME,
            IntentConsts.Sync.SYNC_SERVICE_CLASS
        ));

    syncServiceInterface = OdkSyncServiceInterface.Stub.asInterface(binder);
    assertNotNull(syncServiceInterface);
  }

  protected void bindToSyncServer() throws TimeoutException {
    IBinder binder = syncServerRule.bindService(new Intent()
        .setClass(InstrumentationRegistry.getTargetContext(), PeerSyncServerService.class));

    peerSyncServer = ((PeerSyncServerService.PeerSyncServerBinder) binder).getServer();
    assertNotNull(peerSyncServer);
  }

  protected void setSyncUrl(String appName, String url) {
    PropertiesSingleton propertiesSingleton = CommonToolProperties.get(
        InstrumentationRegistry.getTargetContext(),
        appName
    );

    // TODO: this should probably be set some where else
    propertiesSingleton.setProperties(Collections.singletonMap(
        CommonToolProperties.KEY_FIRST_LAUNCH,
        Boolean.toString(false)
    ));

    propertiesSingleton.setProperties(Collections.singletonMap(
            CommonToolProperties.KEY_SYNC_SERVER_URL,
            url
        ));
  }
}
