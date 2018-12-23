package org.opendatakit.submit.ui.resolve.table;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ConflictingTable {
  private final String id;
  private int conflicts;
  private String lastSync;

  public String getId() {
    return id;
  }

  public int getConflicts() {
    return conflicts;
  }

  public void setConflicts(int conflicts) {
    this.conflicts = conflicts;
  }

  public String getLastSync() {
    return lastSync;
  }

  public void setLastSync(String lastSync) {
    this.lastSync = lastSync;
  }

  public ConflictingTable(@NonNull String id, int conflicts, @Nullable String lastSync) {
    this.id = id;
    this.conflicts = conflicts;
    this.lastSync = lastSync;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ConflictingTable that = (ConflictingTable) o;

    if (getConflicts() != that.getConflicts()) return false;
    if (!getId().equals(that.getId())) return false;
    return getLastSync() != null ? getLastSync().equals(that.getLastSync()) : that.getLastSync() == null;
  }

  @Override
  public int hashCode() {
    int result = getId().hashCode();
    result = 31 * result + getConflicts();
    result = 31 * result + (getLastSync() != null ? getLastSync().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ConflictingTable{" +
        "id='" + id + '\'' +
        ", conflicts=" + conflicts +
        ", lastSync='" + lastSync + '\'' +
        '}';
  }
}
