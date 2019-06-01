package org.opendatakit.submit.ui.resolve.detail;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ColumnValuePair {
  private final String column;
  private final String value;

  public String getColumn() {
    return column;
  }

  public String getValue() {
    return value;
  }

  public ColumnValuePair(@NonNull String column, @Nullable String value) {
    this.column = column;
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ColumnValuePair that = (ColumnValuePair) o;

    if (!getColumn().equals(that.getColumn())) return false;
    return getValue() != null ? getValue().equals(that.getValue()) : that.getValue() == null;
  }

  @Override
  public int hashCode() {
    int result = getColumn().hashCode();
    result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ColumnValuePair{" +
        "column='" + column + '\'' +
        ", value='" + value + '\'' +
        '}';
  }
}
