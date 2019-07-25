package org.opendatakit.submit.util;

import androidx.annotation.NonNull;

import org.opendatakit.submit.consts.SubmitConsts;

public class SubmitUtil {
  /**
   * Produces an appName for use with Submit using the primary appName
   * @param primaryAppName Primary appName
   * @return Submit's appName
   * @throws IllegalArgumentException When primaryAppName is empty
   */
  @NonNull
  public static String getSecondaryAppName(@NonNull String primaryAppName) {
    if (primaryAppName.isEmpty()) {
      throw new IllegalArgumentException("primaryAppName cannot be empty");
    }

    return SubmitConsts.SECONDARY_APP_NAME_PREFIX + primaryAppName;
  }
}
