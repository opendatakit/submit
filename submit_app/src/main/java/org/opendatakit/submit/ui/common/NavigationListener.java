package org.opendatakit.submit.ui.common;

import android.content.ComponentName;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import org.opendatakit.consts.IntentConsts;
import org.opendatakit.fragment.AboutMenuFragment;
import org.opendatakit.submit.R;
import org.opendatakit.submit.activities.SubmitBaseActivity;
import org.opendatakit.submit.activities.MainActivity;
import org.opendatakit.submit.activities.PeerTransferActivity;
import org.opendatakit.submit.ui.resolve.table.TableListFragment;
import org.opendatakit.submit.util.SubmitUtil;

public class NavigationListener implements NavigationView.OnNavigationItemSelectedListener {
  private final SubmitBaseActivity activity;

  public NavigationListener(@NonNull SubmitBaseActivity activity) {
    this.activity = activity;
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.nav_finalize:
        startTableListFrag();
        break;
      case R.id.nav_peer_sync:
        startPeerTransferActivity();
        break;
      case R.id.nav_settings:
        startPrefActivity();
        break;
      case R.id.nav_about:
        startAboutMenuFragment();
        break;
    }

    activity.<DrawerLayout>findViewById(R.id.drawer_layout).closeDrawer(GravityCompat.START);

    return item.getGroupId() != R.id.nav_group_external;
  }

  private void startTableListFrag() {
    activity.getSupportFragmentManager()
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
        .putExtra(IntentConsts.INTENT_KEY_APP_NAME, SubmitUtil.getSecondaryAppName(activity.getAppName()));

    activity.startActivity(i);
  }

  private void startPeerTransferActivity() {
    Intent i = new Intent(activity, PeerTransferActivity.class);
    activity.startActivityForResult(i, MainActivity.PEER_SYNC_ACTIVITY_CODE);
  }

  private void startAboutMenuFragment() {
    activity.getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.main_content, new AboutMenuFragment())
        .addToBackStack(null)
        .commit();
  }
}
