package org.opendatakit.submit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;

import org.opendatakit.submit.R;
import org.opendatakit.submit.ui.common.NavigationListener;
import org.opendatakit.submit.ui.welcome.WelcomeFragment;

public class MainActivity extends SubmitBaseActivity {
  private static final String TAG = MainActivity.class.getSimpleName();

  public static final int PEER_SYNC_ACTIVITY_CODE = 1;

  private Snackbar dbUnavailableSnackbar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this,
        drawer,
        toolbar,
        R.string.navigation_drawer_open,
        R.string.navigation_drawer_close
    );
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(new NavigationListener(this));

    dbUnavailableSnackbar = Snackbar.make(
        findViewById(R.id.main_content),
        R.string.database_unavailable,
        Snackbar.LENGTH_INDEFINITE
    );

    if (savedInstanceState == null) {
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
      dbUnavailableSnackbar.show();
    }
  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
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
    if (dbUnavailableSnackbar != null) {
      dbUnavailableSnackbar.dismiss();
    }
  }

  @Override
  public void databaseUnavailable() {
    dbUnavailableSnackbar.show();
  }
}
