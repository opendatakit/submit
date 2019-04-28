package org.opendatakit.submit.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import org.opendatakit.consts.IntentConsts;
import org.opendatakit.fragment.AboutMenuFragment;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.submit.R;
import org.opendatakit.submit.ui.resolve.table.TableListFragment;
import org.opendatakit.submit.ui.welcome.WelcomeFragment;
import org.opendatakit.submit.util.SubmitUtil;

public class MainActivity extends SubmitBaseActivity {
  private static final String TAG = MainActivity.class.getSimpleName();

  public static final int PEER_SYNC_ACTIVITY_CODE = 1;
  public static final String FRAGMENT_TO_NAV_LABEL = "fragment";
  public static final String TABLE_LIST_FRAGMENT_TO_NAV = "tableListFragment";
  public static final String FINAL_COPY_LABEL = "finalCopy";

  public enum ScreenType {
    WELCOME_SCREEN,
    ABOUT_SCREEN,
    SETTINGS_SCREEN,
  }

  /**
   * The active screen -- retained state
   */
  ScreenType activeScreenType = ScreenType.WELCOME_SCREEN;
  public boolean finalCopy = false;

  /**
   * used to determine whether we need to change the menu (action bar)
   * because of a change in the active fragment.
   */
  private ScreenType lastMenuType = null;

  private Snackbar dbUnavailableSnackbar;
  public boolean databaseAvailable = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle extras = getIntent().getExtras();
    String fragmentToNav = null;
    if (extras != null) {
      if (extras.containsKey(FRAGMENT_TO_NAV_LABEL)) {
        fragmentToNav = extras.getString(FRAGMENT_TO_NAV_LABEL);
      }

      if (extras.containsKey(FINAL_COPY_LABEL)) {
        finalCopy = extras.getBoolean(FINAL_COPY_LABEL);
      }
    }

    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    dbUnavailableSnackbar = Snackbar.make(
        findViewById(R.id.main_content),
        R.string.database_unavailable,
        Snackbar.LENGTH_INDEFINITE
    );

    if (fragmentToNav != null && fragmentToNav.equals(TABLE_LIST_FRAGMENT_TO_NAV)) {
      startTableListFrag(null);
    } else if (savedInstanceState == null) {
      getSupportFragmentManager()
          .beginTransaction()
          .setTransition(FragmentTransaction.TRANSIT_NONE)
          .replace(R.id.main_content, new WelcomeFragment())
          .commit();
    }
  }

  @Override
  protected void onPostResume() {
    super.onPostResume();

    if (getDatabase() == null) {
      databaseAvailable = false;
      dbUnavailableSnackbar.show();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode != PEER_SYNC_ACTIVITY_CODE) {
      return;
    }

    if (resultCode == PeerTransferActivity.W_DIRECT_UNSUPPORTED) {
      Snackbar
          .make(
              findViewById(R.id.main_content),
              R.string.wifi_direct_unsupported,
              Snackbar.LENGTH_LONG
          )
          .show();
    }
  }

  @Override
  public void databaseAvailable() {
    databaseAvailable = true;
    if (dbUnavailableSnackbar != null) {
      dbUnavailableSnackbar.dismiss();
    }
  }

  @Override
  public void databaseUnavailable() {
    databaseAvailable = false;
    if (dbUnavailableSnackbar != null) {
      dbUnavailableSnackbar.show();
    }
  }


  private void changeOptionsMenu(Menu menu) {
    MenuInflater menuInflater = this.getMenuInflater();

    if (activeScreenType == ScreenType.WELCOME_SCREEN) {
      menuInflater.inflate(R.menu.main, menu);
    }
    lastMenuType = activeScreenType;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    changeOptionsMenu(menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    if (lastMenuType != activeScreenType) {
      changeOptionsMenu(menu);
    }
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    WebLogger.getLogger(getAppName()).d(TAG, "[onOptionsItemSelected] selecting an item");

    switch (item.getItemId()) {
      case R.id.menu_about:
        startAboutMenuFragment();
        return true;
      case R.id.menu_preferences:
        startPrefActivity();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }



  public void startTableListFrag(View view) {
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.main_content, new TableListFragment())
        .addToBackStack(null)
        .commit();
  }

  private void startPrefActivity() {
    Intent i = new Intent()
        .setComponent(new ComponentName(
            IntentConsts.AppProperties.APPLICATION_NAME,
            IntentConsts.AppProperties.ACTIVITY_NAME)
        )
        .setAction(Intent.ACTION_DEFAULT)
        .putExtra(IntentConsts.INTENT_KEY_APP_NAME, SubmitUtil.getSecondaryAppName(getAppName()));

    startActivity(i);
  }

  private void startPeerTransferActivity() {
    Intent i = new Intent(this, PeerTransferActivity.class);
    startActivityForResult(i, MainActivity.PEER_SYNC_ACTIVITY_CODE);
  }

  private void startAboutMenuFragment() {
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.main_content, new AboutMenuFragment())
        .addToBackStack(null)
        .commit();
  }
}
