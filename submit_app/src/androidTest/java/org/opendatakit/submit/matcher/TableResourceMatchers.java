package org.opendatakit.submit.matcher;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;

public class TableResourceMatchers {
  public static Matcher<TableResource> hasTableId(final String tableId) {
    return new BaseMatcher<TableResource>() {
      @Override
      public boolean matches(Object item) {
        return item instanceof TableResource &&
            tableId != null &&
            tableId.equals(((TableResource) item).getTableId());
      }

      @Override
      public void describeTo(Description description) {
        description
            .appendText("tableId should be ")
            .appendValue(tableId);
      }
    };
  }

  public static Matcher<TableResource> hasDefinitionUri(final String uri) {
    return new BaseMatcher<TableResource>() {
      @Override
      public boolean matches(Object item) {
        return item instanceof TableResource &&
            uri != null &&
            uri.equals(((TableResource) item).getDefinitionUri());
      }

      @Override
      public void describeTo(Description description) {
        description
            .appendText("definitionUri should be ")
            .appendValue(uri);
      }
    };
  }

  public static Matcher<TableResource> hasSchemaETag(final String schemaETag) {
    return new BaseMatcher<TableResource>() {
      @Override
      public boolean matches(Object item) {
        return item instanceof TableResource &&
            schemaETag != null &&
            schemaETag.equals(((TableResource) item).getSchemaETag());
      }

      @Override
      public void describeTo(Description description) {
        description
            .appendText("schemaETag should be ")
            .appendValue(schemaETag);
      }
    };
  }
}
