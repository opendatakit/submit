package org.opendatakit.submit.ui.finalize;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class FinalizeViewModel extends ViewModel {
  private MutableLiveData<String> status;

  public LiveData<String> getStatus() {
    if (status == null) {
      status = new MutableLiveData<>();
    }

    return status;
  }

  public void setStatus(String status) {
    this.status.postValue(status);
  }
}
