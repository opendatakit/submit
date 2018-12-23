package org.opendatakit.submit.util;

import android.content.ContentValues;

import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;
import org.opendatakit.database.data.TypedRow;
import org.opendatakit.provider.DataTableColumns;

public class RowUtil {
  public static RowFilterScope getRowFilterScope(TypedRow row) {
    return RowFilterScope.asRowFilter(
        row.getRawStringByKey(DataTableColumns.DEFAULT_ACCESS),
        row.getRawStringByKey(DataTableColumns.ROW_OWNER),
        row.getRawStringByKey(DataTableColumns.GROUP_READ_ONLY),
        row.getRawStringByKey(DataTableColumns.GROUP_MODIFY),
        row.getRawStringByKey(DataTableColumns.GROUP_PRIVILEGED)
    );
  }

  public static ContentValues getRowFilterScope(Row row) {
    RowFilterScope scope = row.getRowFilterScope();
    ContentValues cv = new ContentValues();

    cv.put(DataTableColumns.DEFAULT_ACCESS, scope.getDefaultAccess().toString());
    cv.put(DataTableColumns.GROUP_MODIFY, scope.getGroupModify());
    cv.put(DataTableColumns.GROUP_PRIVILEGED, scope.getGroupPrivileged());
    cv.put(DataTableColumns.GROUP_READ_ONLY, scope.getGroupReadOnly());
    cv.put(DataTableColumns.ROW_OWNER, scope.getRowOwner());

    return cv;
  }
}
