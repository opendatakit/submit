package org.opendatakit.submit.ui.common;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import org.opendatakit.application.CommonApplication;
import org.opendatakit.database.service.UserDbInterface;

public class AbsBaseFragment extends Fragment {
  protected SharedViewModel sharedViewModel;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    this.sharedViewModel = ViewModelProviders
        .of(requireActivity())
        .get(SharedViewModel.class);
  }

  public String getAppName() {
    return sharedViewModel.getAppName();
  }

  @Nullable
  public UserDbInterface getDatabase() {
    return ((CommonApplication) requireActivity().getApplication()).getDatabase();
  }
}
