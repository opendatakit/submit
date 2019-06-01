package org.opendatakit.submit.matcher;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.opendatakit.aggregate.odktables.rest.entity.DataKeyValue;

public class DataKeyValueMatchers {
  public static Matcher<DataKeyValue> hasDkvWithColumn(final String column) {
    return new BaseMatcher<DataKeyValue>() {
      @Override
      public boolean matches(Object item) {
        if (!(item instanceof DataKeyValue) || column == null) {
          return false;
        }

        return ((DataKeyValue) item).column.equals(column) && ((DataKeyValue) item).value != null;
      }

      @Override
      public void describeTo(Description description) {
        description
            .appendText("Expects a DataKeyValue with column ")
            .appendValue(column);
      }
    };
  }
}
