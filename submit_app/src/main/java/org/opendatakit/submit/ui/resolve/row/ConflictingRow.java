package org.opendatakit.submit.ui.resolve.row;

import androidx.annotation.NonNull;

public class ConflictingRow {
  private final String peerId;

  public String getPeerId() {
    return peerId;
  }

  public ConflictingRow(@NonNull String peerId) {
    this.peerId = peerId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ConflictingRow that = (ConflictingRow) o;

    return getPeerId().equals(that.getPeerId());
  }

  @Override
  public int hashCode() {
    return getPeerId().hashCode();
  }

  @Override
  public String toString() {
    return "ConflictingRow{" +
        "peerId='" + peerId + '\'' +
        '}';
  }
}
