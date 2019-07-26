package org.opendatakit.submit.ui.finalize;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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
