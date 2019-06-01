package org.opendatakit.submit.ui.common;

import android.view.View;

public interface OnClickListenerHolder<T> {
  View.OnClickListener getListener(T item);
}
