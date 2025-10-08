// © 2025 Luca Filipozzi. Some rights reserved. See LICENSE.
package com.github.lucafilipozzi.dag;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MyGraphTest {
  MyGraph myGraph;

  @BeforeEach
  void setUp() {
    User user = User.of(Set.of("DVP", "DSC", "TSC"));
    InputStreamReader prototypeGraphReader = new InputStreamReader(
      Objects.requireNonNull(getClass().getResourceAsStream("/prototype-graph.xml")));
    myGraph = MyGraph.of(prototypeGraphReader, user);
  }

  @Test
  void test_getChallenge() {
    assertEquals("DVP", myGraph.getChallenge() );
  }

  @Test
  void test_getChallenges() {
    List<String> challenges = myGraph.getChallenges();
    assertEquals(2, challenges.size());
    assertTrue(challenges.contains("DVP"));
    assertTrue(challenges.contains("DSC"));
    assertDoesNotThrow(() -> myGraph.setStatus("success"));
    challenges = myGraph.getChallenges();
    assertEquals(1, challenges.size());
    assertTrue(challenges.contains("DSC"));
  }

  @Test
  void test_dump_and_load() {
    assertEquals("DVP", myGraph.getChallenge());
    assertDoesNotThrow(() -> myGraph.setStatus("success"));
    StringWriter stringWriter = new StringWriter();
    myGraph.dump(stringWriter);
    MyGraph newGraph = MyGraph.load(new StringReader(stringWriter.toString()));
    assertEquals("DSC", newGraph.getChallenge());
    assertDoesNotThrow(() -> myGraph.setStatus("success"));
  }

  @Test
  void test_SuccessDVP_SuccessDSC_Success() {
    assertEquals("DVP", myGraph.getChallenge() );
    assertDoesNotThrow(() -> myGraph.setStatus("success"));
    assertEquals("DSC", myGraph.getChallenge());
    assertDoesNotThrow(() -> myGraph.setStatus("success"));
    assertThrows(MyGraph.GetChallengeSuccess.class, () -> myGraph.getChallenge());
    assertThrows(MyGraph.SetStatusException.class, () -> myGraph.setStatus("success"));
    assertThrows(MyGraph.SetStatusException.class, () -> myGraph.setStatus("failure"));
    assertThrows(MyGraph.SetStatusException.class, () -> myGraph.setStatus("garbage"));
  }

  @Test
  void test_SuccessDVP_FailureDSC_SuccessTSC_Success() {
    assertEquals("DVP", myGraph.getChallenge() );
    assertDoesNotThrow(() -> myGraph.setStatus("success"));
    assertEquals("DSC", myGraph.getChallenge());
    assertDoesNotThrow(() -> myGraph.setStatus("failure"));
    assertEquals("TSC", myGraph.getChallenge());
    assertDoesNotThrow(() -> myGraph.setStatus("success"));
    assertThrows(MyGraph.GetChallengeSuccess.class, () -> myGraph.getChallenge());
    assertThrows(MyGraph.SetStatusException.class, () -> myGraph.setStatus("success"));
    assertThrows(MyGraph.SetStatusException.class, () -> myGraph.setStatus("failure"));
    assertThrows(MyGraph.SetStatusException.class, () -> myGraph.setStatus("garbage"));
  }

  @Test
  void test_SuccessDVP_FailureDSC_FailureTSC_Failure() {
    assertEquals("DVP", myGraph.getChallenge() );
    assertDoesNotThrow(() -> myGraph.setStatus("success"));
    assertEquals("DSC", myGraph.getChallenge());
    assertDoesNotThrow(() -> myGraph.setStatus("failure"));
    assertEquals("TSC", myGraph.getChallenge());
    assertDoesNotThrow(() -> myGraph.setStatus("failure"));
    assertThrows(MyGraph.GetChallengeFailure.class, () -> myGraph.getChallenge());
    assertThrows(MyGraph.SetStatusException.class, () -> myGraph.setStatus("success"));
    assertThrows(MyGraph.SetStatusException.class, () -> myGraph.setStatus("failure"));
    assertThrows(MyGraph.SetStatusException.class, () -> myGraph.setStatus("garbage"));
  }

  @Test
  void test_FailureDVP_SuccessDSC_SuccessTSC_Success() {
    assertEquals("DVP", myGraph.getChallenge() );
    assertDoesNotThrow(() -> myGraph.setStatus("failure"));
    assertEquals("DSC", myGraph.getChallenge());
    assertDoesNotThrow(() -> myGraph.setStatus("success"));
    assertEquals("TSC", myGraph.getChallenge());
    assertDoesNotThrow(() -> myGraph.setStatus("success"));
    assertThrows(MyGraph.GetChallengeSuccess.class, () -> myGraph.getChallenge());
    assertThrows(MyGraph.SetStatusException.class, () -> myGraph.setStatus("success"));
    assertThrows(MyGraph.SetStatusException.class, () -> myGraph.setStatus("failure"));
    assertThrows(MyGraph.SetStatusException.class, () -> myGraph.setStatus("garbage"));
  }

  @Test
  void test_FailureDVP_SuccessDSC_FailureTSC_Failure() {
    assertEquals("DVP", myGraph.getChallenge() );
    assertDoesNotThrow(() -> myGraph.setStatus("failure"));
    assertEquals("DSC", myGraph.getChallenge());
    assertDoesNotThrow(() -> myGraph.setStatus("success"));
    assertEquals("TSC", myGraph.getChallenge());
    assertDoesNotThrow(() -> myGraph.setStatus("failure"));
    assertThrows(MyGraph.GetChallengeFailure.class, () -> myGraph.getChallenge());
    assertThrows(MyGraph.SetStatusException.class, () -> myGraph.setStatus("success"));
    assertThrows(MyGraph.SetStatusException.class, () -> myGraph.setStatus("failure"));
    assertThrows(MyGraph.SetStatusException.class, () -> myGraph.setStatus("garbage"));
  }

  @Test
  void test_FailureDVP_FailureDSC_Failure() {
    assertEquals("DVP", myGraph.getChallenge() );
    assertDoesNotThrow(() -> myGraph.setStatus("failure"));
    assertEquals("DSC", myGraph.getChallenge());
    assertDoesNotThrow(() -> myGraph.setStatus("failure"));
    assertThrows(MyGraph.GetChallengeFailure.class, () -> myGraph.getChallenge());
    assertThrows(MyGraph.SetStatusException.class, () -> myGraph.setStatus("success"));
    assertThrows(MyGraph.SetStatusException.class, () -> myGraph.setStatus("failure"));
    assertThrows(MyGraph.SetStatusException.class, () -> myGraph.setStatus("garbage"));
  }
}
