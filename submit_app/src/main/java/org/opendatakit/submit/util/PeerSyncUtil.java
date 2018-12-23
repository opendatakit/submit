package org.opendatakit.submit.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.provider.Settings;

import org.opendatakit.aggregate.odktables.rest.entity.DataKeyValue;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.submit.consts.SubmitColumns;

import java.util.ArrayList;
import java.util.Map;

public class PeerSyncUtil {
  public static Uri buildPeerSyncUrl(String address, String appName) {
    return Uri.parse(address)
        .buildUpon()
        .scheme("peer")
        .appendPath(appName)
        .build();
  }

  public static ArrayList<DataKeyValue> tagDeviceId(ArrayList<DataKeyValue> dkvl, Context context) {
    if (shouldUpdateDeviceId(dkvl)) {
      Map<String, String> values = Row.convertToMap(dkvl);

      // TODO: get this from PropertyManager instead
      values.put(SubmitColumns.DEVICE_ID, getAndroidId(context));
      return Row.convertFromMap(values);
    }

    return dkvl;
  }

  public static boolean shouldUpdateDeviceId(ArrayList<DataKeyValue> dkvl) {
    Map<String, String> valueMap = Row.convertToMap(dkvl);

    // device id being null means that either this row was created locally
    // or the row was modified on the device
    // the device id is null because the row was copied over from the primary
    // database
    return valueMap.get(SubmitColumns.DEVICE_ID) == null ||
        valueMap.get(SubmitColumns.DEVICE_ID).isEmpty();
  }

  @SuppressLint("HardwareIds")
  public static String getAndroidId(Context context) {
    return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
  }
}
