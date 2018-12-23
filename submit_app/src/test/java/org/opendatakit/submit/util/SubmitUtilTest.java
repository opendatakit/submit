package org.opendatakit.submit.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.junit.Assert.*;

public class SubmitUtilTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void getSecondaryAppName() {
    assertEquals("submit_default", SubmitUtil.getSecondaryAppName("default"));
    assertEquals("submit_my_app_name", SubmitUtil.getSecondaryAppName("my_app_name"));
    assertEquals("submit_  ", SubmitUtil.getSecondaryAppName("  "));
  }

  @Test
  public void getSecondaryAppName_empty() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(endsWith("cannot be empty"));

    SubmitUtil.getSecondaryAppName("");
  }
}