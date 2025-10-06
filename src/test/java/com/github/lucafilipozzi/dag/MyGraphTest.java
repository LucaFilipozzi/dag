// © 2025 Luca Filipozzi. Some rights reserved. See LICENSE.
package com.github.lucafilipozzi.dag;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MyGraphTest {
  private static final String GRAPH = """
      <graphml xmlns="http://graphml.graphdrawing.org/xmlns">
        <key id="d0" for="edge" attr.name="weight" attr.type="long" />
        <graph edgedefault="directed">
          <node id="START" />
          <node id="DSC-1" />
          <node id="DVP-1" />
          <node id="DSC-2" />
          <node id="DVP-2" />
          <node id="ERC-2" />
          <node id="TSC-2" />
          <node id="END" />
          <edge source="START" target="DVP-1">
            <data key="d0">1</data>
          </edge>
          <edge source="START" target="DSC-1">
            <data key="d0">2</data>
          </edge>
          <edge source="DVP-1" target="DSC-2">
            <data key="d0">2</data>
          </edge>
          <edge source="DVP-1" target="ERC-2">
            <data key="d0">10</data>
          </edge>
          <edge source="DVP-1" target="TSC-2">
            <data key="d0">20</data>
          </edge>
          <edge source="DSC-1" target="DVP-2">
            <data key="d0">1</data>
          </edge>
          <edge source="DSC-1" target="TSC-2">
            <data key="d0">20</data>
          </edge>
          <edge source="DSC-1" target="ERC-2">
            <data key="d0">10</data>
          </edge>
          <edge source="DSC-2" target="END" />
          <edge source="DVP-2" target="END" />
          <edge source="ERC-2" target="END" />
          <edge source="TSC-2" target="END" />
        </graph>
      </graphml>
      """;

  private MyGraph myGraph;

  @BeforeEach
  void setUp() {
    User user = User.of(Set.of("DVP", "DSC", "TSC"));
    myGraph = MyGraph.of(new StringReader(GRAPH), user);
  }

  @Test
  void test_dump_and_load() {
    assertEquals("DVP", myGraph.GET());
    assertEquals("continue", myGraph.POST("success"));
    StringWriter stringWriter = new StringWriter();
    myGraph.dump(stringWriter);
    MyGraph newGraph = MyGraph.load(new StringReader(stringWriter.toString()));
    assertEquals("DSC", newGraph.GET());
    assertEquals("success", newGraph.POST("success"));
  }

  @Test
  void test_SuccessDVP_SuccessDSC_Success() {
    assertEquals("DVP", myGraph.GET());
    assertEquals("continue", myGraph.POST("success"));
    assertEquals("DSC", myGraph.GET());
    assertEquals("success", myGraph.POST("success"));
    assertNull(myGraph.GET());
    assertEquals("invalid", myGraph.POST("success"));
    assertEquals("invalid", myGraph.POST("failure"));
    assertEquals("invalid", myGraph.POST("garbage"));
  }

  @Test
  void test_SuccessDVP_FailureDSC_SuccessTSC_Success() {
    assertEquals("DVP", myGraph.GET());
    assertEquals("continue", myGraph.POST("success"));
    assertEquals("DSC", myGraph.GET());
    assertEquals("continue", myGraph.POST("failure"));
    assertEquals("TSC", myGraph.GET());
    assertEquals("success", myGraph.POST("success"));
    assertNull(myGraph.GET());
    assertEquals("invalid", myGraph.POST("success"));
    assertEquals("invalid", myGraph.POST("failure"));
    assertEquals("invalid", myGraph.POST("garbage"));
  }

  @Test
  void test_SuccessDVP_FailureDSC_FailureTSC_Failure() {
    assertEquals("DVP", myGraph.GET() );
    assertEquals("continue", myGraph.POST("success"));
    assertEquals("DSC", myGraph.GET());
    assertEquals("continue", myGraph.POST("failure"));
    assertEquals("TSC", myGraph.GET());
    assertEquals("failure", myGraph.POST("failure"));
    assertNull(myGraph.GET());
    assertEquals("invalid", myGraph.POST("success"));
    assertEquals("invalid", myGraph.POST("failure"));
    assertEquals("invalid", myGraph.POST("garbage"));
  }

  @Test
  void test_FailureDVP_SuccessDSC_SuccessTSC_Success() {
    assertEquals("DVP", myGraph.GET() );
    assertEquals("continue", myGraph.POST("failure"));
    assertEquals("DSC", myGraph.GET());
    assertEquals("continue", myGraph.POST("success"));
    assertEquals("TSC", myGraph.GET());
    assertEquals("success", myGraph.POST("success"));
    assertNull(myGraph.GET());
    assertEquals("invalid", myGraph.POST("success"));
    assertEquals("invalid", myGraph.POST("failure"));
    assertEquals("invalid", myGraph.POST("garbage"));
  }

  @Test
  void test_FailureDVP_SuccessDSC_FailureTSC_Failure() {
    assertEquals("DVP", myGraph.GET() );
    assertEquals("continue", myGraph.POST("failure"));
    assertEquals("DSC", myGraph.GET());
    assertEquals("continue", myGraph.POST("success"));
    assertEquals("TSC", myGraph.GET());
    assertEquals("failure", myGraph.POST("failure"));
    assertNull(myGraph.GET());
    assertEquals("invalid", myGraph.POST("success"));
    assertEquals("invalid", myGraph.POST("failure"));
    assertEquals("invalid", myGraph.POST("garbage"));
  }

  @Test
  void test_FailureDVP_FailureDSC_Failure() {
    assertEquals("DVP", myGraph.GET() );
    assertEquals("continue", myGraph.POST("failure"));
    assertEquals("DSC", myGraph.GET());
    assertEquals("failure", myGraph.POST("failure"));
    assertNull(myGraph.GET());
    assertEquals("invalid", myGraph.POST("success"));
    assertEquals("invalid", myGraph.POST("failure"));
    assertEquals("invalid", myGraph.POST("garbage"));
  }
}
