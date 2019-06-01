package org.opendatakit.submit.matcher;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.opendatakit.aggregate.odktables.rest.ElementDataType;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinitionResource;
import org.opendatakit.submit.consts.SubmitColumns;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;

public class TableDefinitionResourceMatchers {
  public static Matcher<TableDefinitionResource> hasColumn(final Column column) {
    return new BaseMatcher<TableDefinitionResource>() {
      @Override
      public boolean matches(Object item) {
        if (!(item instanceof TableDefinitionResource) || column == null) {
          return false;
        }

        for (Column col : ((TableDefinitionResource) item).getColumns()) {
          if (col.equals(column)) {
            return true;
          }
        }

        return false;
      }

      @Override
      public void describeTo(Description description) {
        description
            .appendText("Column list should contain ")
            .appendValue(column);
      }
    };
  }

  public static Matcher<TableDefinitionResource> hasNColumns(final int n) {
    return new BaseMatcher<TableDefinitionResource>() {
      @Override
      public boolean matches(Object item) {
        if (!(item instanceof TableDefinitionResource)) {
          return false;
        }

        return ((TableDefinitionResource) item).getColumns().size() == n;
      }

      @Override
      public void describeTo(Description description) {
        description
            .appendText("There should be ")
            .appendValue(n)
            .appendText(" columns");
      }
    };
  }

  public static Matcher<TableDefinitionResource> hasSubmitColumns() {
    List<Matcher<? super TableDefinitionResource>> matchers = new ArrayList<>();

    for (String submitColumn : SubmitColumns.SUBMIT_COLUMNS) {
      matchers.add(hasColumn(new Column(
          submitColumn,
          submitColumn,
          ElementDataType.string.name(),
          null
      )));
    }

    return allOf(matchers);
  }
}
