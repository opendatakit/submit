package org.opendatakit.submit.ui.common;

import android.app.Application;
import androidx.annotation.NonNull;

public class SharedViewModel extends AppAwareViewModel {
  public SharedViewModel(@NonNull Application application) {
    super(application);
  }
}
