// © 2025 Luca Filipozzi. All rights reserved. See LICENSE.

package com.github.lucafilipozzi.dag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class APITest {
  private API api;

  @BeforeEach
  void setUp() {
    User user = User.of(Set.of("DVP", "DSC", "TSC"));
    // load prototype graph from registry service
    InputStreamReader prototypeGraphReader = new InputStreamReader(
      Objects.requireNonNull(getClass().getResourceAsStream("/prototype-graph.xml")));
    api = API.of(prototypeGraphReader, user);
  }

  @Test
  void test_SuccessDVP_SuccessDSC_Success() {
    assertEquals("DVP", api.get());
    assertEquals("continue", api.post("success"));
    assertEquals("DSC", api.get());
    assertEquals("success", api.post("success"));
    assertNull(api.get());
    assertEquals("invalid", api.post("success"));
    assertEquals("invalid", api.post("failure"));
    assertEquals("invalid", api.post("garbage"));
  }

  @Test
  void test_SuccessDVP_FailureDSC_SuccessTSC_Success() {
    assertEquals("DVP", api.get());
    assertEquals("continue", api.post("success"));
    assertEquals("DSC", api.get());
    assertEquals("continue", api.post("failure"));
    assertEquals("TSC", api.get());
    assertEquals("success", api.post("success"));
    assertNull(api.get());
    assertEquals("invalid", api.post("success"));
    assertEquals("invalid", api.post("failure"));
    assertEquals("invalid", api.post("garbage"));
  }

  @Test
  void test_SuccessDVP_FailureDSC_FailureTSC_Failure() {
    assertEquals("DVP", api.get() );
    assertEquals("continue", api.post("success"));
    assertEquals("DSC", api.get());
    assertEquals("continue", api.post("failure"));
    assertEquals("TSC", api.get());
    assertEquals("failure", api.post("failure"));
    assertNull(api.get());
    assertEquals("invalid", api.post("success"));
    assertEquals("invalid", api.post("failure"));
    assertEquals("invalid", api.post("garbage"));
  }

  @Test
  void test_FailureDVP_SuccessDSC_SuccessTSC_Success() {
    assertEquals("DVP", api.get() );
    assertEquals("continue", api.post("failure"));
    assertEquals("DSC", api.get());
    assertEquals("continue", api.post("success"));
    assertEquals("TSC", api.get());
    assertEquals("success", api.post("success"));
    assertNull(api.get());
    assertEquals("invalid", api.post("success"));
    assertEquals("invalid", api.post("failure"));
    assertEquals("invalid", api.post("garbage"));
  }

  @Test
  void test_FailureDVP_SuccessDSC_FailureTSC_Failure() {
    assertEquals("DVP", api.get() );
    assertEquals("continue", api.post("failure"));
    assertEquals("DSC", api.get());
    assertEquals("continue", api.post("success"));
    assertEquals("TSC", api.get());
    assertEquals("failure", api.post("failure"));
    assertNull(api.get());
    assertEquals("invalid", api.post("success"));
    assertEquals("invalid", api.post("failure"));
    assertEquals("invalid", api.post("garbage"));
  }

  @Test
  void test_FailureDVP_FailureDSC_Failure() {
    assertEquals("DVP", api.get() );
    assertEquals("continue", api.post("failure"));
    assertEquals("DSC", api.get());
    assertEquals("failure", api.post("failure"));
    assertNull(api.get());
    assertEquals("invalid", api.post("success"));
    assertEquals("invalid", api.post("failure"));
    assertEquals("invalid", api.post("garbage"));
  }
}
