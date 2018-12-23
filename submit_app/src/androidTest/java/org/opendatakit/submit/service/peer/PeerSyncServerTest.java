package org.opendatakit.submit.service.peer;

import android.Manifest;
import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.aggregate.odktables.rest.entity.TableResourceList;
import org.opendatakit.submit.consts.PeerServerConsts;
import org.opendatakit.submit.service.peer.server.PeerSyncServer;
import org.opendatakit.sync.service.entity.ParcelableTableResourceList;
import org.opendatakit.utilities.ODKFileUtils;

import java.io.File;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class PeerSyncServerTest {
  private static final String TAG = PeerSyncServerTest.class.getSimpleName();

  private static final String TEST_SUBMIT_APP_NAME = "submit_androidTest_" + PeerSyncServerTest.class.getSimpleName();
  private static final String SERVER_URL = "http://localhost:8080";

  @Rule
  public final ServiceTestRule serviceRule = new ServiceTestRule();

  @Rule
  public final GrantPermissionRule grantPermissionRule = GrantPermissionRule.grant(
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.WRITE_EXTERNAL_STORAGE
  );

  private PeerSyncServer server;

  private OkHttpClient httpClient;
  private HttpUrl address;

  @Before
  public void setUp() throws Exception {
    ODKFileUtils.assertDirectoryStructure(TEST_SUBMIT_APP_NAME);

    IBinder binder = serviceRule.bindService(new Intent()
        .setClass(InstrumentationRegistry.getTargetContext(), PeerSyncServerService.class));

    this.server = ((PeerSyncServerService.PeerSyncServerBinder) binder).getServer();
    assertNotNull(this.server);

    server.start();

    this.httpClient = new OkHttpClient.Builder().build();
    this.address = HttpUrl.parse(SERVER_URL);
  }

  @After
  public void tearDown() throws Exception {
    FileUtils.deleteDirectory(new File(ODKFileUtils.getAppFolder(TEST_SUBMIT_APP_NAME)));

    if (server != null) {
      server.stop();
    }
  }

  @Test
  public void verifyServerSupportsAppName_valid() throws Exception {
    HttpUrl url = address
        .newBuilder()
        .addPathSegment(PeerServerConsts.VERIFY_SERVER_APP_NAME_PATH)
        .addQueryParameter(PeerServerConsts.APP_NAME_QUERY, TEST_SUBMIT_APP_NAME)
        .build();

    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = null;
    try {
      response = httpClient
          .newCall(request)
          .execute();

      assertTrue(response.isSuccessful());
      assertNotNull(response.body());
      assertEquals("OK", response.body().string());
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  @Test
  public void verifyServerSupportsAppName_prefixValid() throws Exception {
    HttpUrl url = address
        .newBuilder()
        .addPathSegment(PeerServerConsts.VERIFY_SERVER_APP_NAME_PATH)
        .addQueryParameter(PeerServerConsts.APP_NAME_QUERY, "submit_app_does_not_exist")
        .build();

    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = null;
    try {
      response = httpClient
          .newCall(request)
          .execute();

      assertFalse(response.isSuccessful());
      assertEquals(400, response.code());
      assertNotNull(response.body());
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  @Test
  public void verifyServerSupportsAppName_invalid() throws Exception {
    HttpUrl url = address
        .newBuilder()
        .addPathSegment(PeerServerConsts.VERIFY_SERVER_APP_NAME_PATH)
        .addQueryParameter(PeerServerConsts.APP_NAME_QUERY, "default")
        .build();

    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = null;
    try {
      response = httpClient
          .newCall(request)
          .execute();

      assertFalse(response.isSuccessful());
      assertEquals(400, response.code());
      assertNotNull(response.body());
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  @Test
  public void verifyServerSupportsAppName_multiple() throws Exception {
    HttpUrl url = address
        .newBuilder()
        .addPathSegment(PeerServerConsts.VERIFY_SERVER_APP_NAME_PATH)
        .addQueryParameter(PeerServerConsts.APP_NAME_QUERY, "submit_1")
        .addQueryParameter(PeerServerConsts.APP_NAME_QUERY, "default")
        .addQueryParameter(PeerServerConsts.APP_NAME_QUERY, "submit_2")
        .build();

    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = null;
    try {
      response = httpClient
          .newCall(request)
          .execute();

      assertFalse(response.isSuccessful());
      assertEquals(400, response.code());
      assertNotNull(response.body());
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  @Test
  public void verifyServerSupportsAppName_missing() throws Exception {
    HttpUrl url = address
        .newBuilder()
        .addPathSegment(PeerServerConsts.VERIFY_SERVER_APP_NAME_PATH)
        .build();

    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = null;
    try {
      response = httpClient
          .newCall(request)
          .execute();

      assertFalse(response.isSuccessful());
      assertEquals(400, response.code());
      assertNotNull(response.body());
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  @Test
  public void getTables() throws Exception {
    HttpUrl url = address
        .newBuilder()
        .addPathSegment(PeerServerConsts.GET_TABLES_PATH)
        .addQueryParameter(PeerServerConsts.APP_NAME_QUERY, "default")
        .build();

    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = null;
    try {
      response = httpClient
          .newCall(request)
          .execute();

      assertTrue(response.isSuccessful());
      assertNotNull(response.body());

      byte[] body = response.body().bytes();

      assertEquals(
          new ObjectMapper().readValue(body, TableResourceList.class),
          new ObjectMapper().readValue(body, ParcelableTableResourceList.class)
      );
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }
}
