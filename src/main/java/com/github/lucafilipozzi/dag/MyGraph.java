// © 2025 Luca Filipozzi. Some rights reserved. See LICENSE.
package com.github.lucafilipozzi.dag;

import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.MaskSubgraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.nio.graphml.GraphMLImporter;
import org.jgrapht.nio.json.JSONExporter;
import org.jgrapht.nio.json.JSONImporter;

public class MyGraph {
  private final Graph<Node, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
  private final Predicate<Node> vertexMask = node -> node.getStatus().equals(Node.STATUS.FAILURE);
  private final Predicate<DefaultWeightedEdge> edgeMask = edge -> false; // edgeMask not used
  private final Graph<Node, DefaultWeightedEdge> maskSubgraph = new MaskSubgraph<>(graph, vertexMask, edgeMask);
  private final DijkstraShortestPath<Node, DefaultWeightedEdge> shortestPath = new DijkstraShortestPath<>(maskSubgraph);
  private Node start;
  private Node end;
  private String currentChallenge = null;

  static class EndReachedException extends RuntimeException { /* intentionally empty */ }
  static class NoPathFoundException extends RuntimeException { /* intentionally empty */ }

  private MyGraph() {
  }

  public static MyGraph of(Reader reader, User user) {
    MyGraph myGraph = new MyGraph();

    GraphMLImporter<Node, DefaultWeightedEdge> importer = new GraphMLImporter<>();
    importer.setEdgeWeightAttributeName("weight");
    importer.setVertexFactory(Node::of);
    importer.importGraph(myGraph.graph, reader);

    myGraph.start = myGraph.graph.vertexSet().stream().filter(node -> node.getId().equals("START")).findFirst().orElseThrow();
    myGraph.end = myGraph.graph.vertexSet().stream().filter(node -> node.getId().equals("END")).findFirst().orElseThrow();

    myGraph.start.setStatus(Node.STATUS.SUCCESS);
    myGraph.end.setStatus(Node.STATUS.SUCCESS);
    myGraph.graph.vertexSet().stream()
      .filter(node -> !node.equals(myGraph.start) && !node.equals(myGraph.end))
      .forEach(node -> node.setStatus(user.getChallenges().contains(node.getChallenge()) ? Node.STATUS.UNTRIED : Node.STATUS.FAILURE));

    myGraph.currentChallenge = myGraph.getNextUntriedNode().getChallenge();

    return myGraph;
  }

  private Node getNextUntriedNode() {
    GraphPath<Node, DefaultWeightedEdge> graphPath = Optional.ofNullable(shortestPath.getPath(start, end)).orElseThrow(NoPathFoundException::new);
    return graphPath.getVertexList().stream().filter(node -> node.getStatus().equals(Node.STATUS.UNTRIED)).findFirst().orElseThrow(EndReachedException::new);
  }

  private void setNodesStatus(Node.STATUS status) {
    graph.vertexSet().stream().filter(node -> node.getChallenge().equals(currentChallenge)).forEach(node -> node.setStatus(status));
  }

  public String GET() {
    return currentChallenge;
  }

  public String POST(String status) {
    if (currentChallenge == null || !List.of("success", "failure").contains(status)) {
      return "invalid";
    }
    setNodesStatus(Node.STATUS.valueOf(status.toUpperCase()));
    try {
      currentChallenge = getNextUntriedNode().getChallenge();
      return "continue";
    } catch (NoPathFoundException e) {
      currentChallenge = null;
      return "failure";
    } catch (EndReachedException e) {
      currentChallenge = null;
      return "success";
    }
  }

  public void dump(Writer writer) {
    JSONExporter<Node, DefaultWeightedEdge> exporter = new JSONExporter<>();
    exporter.setVertexIdProvider(Node::getId);
    exporter.setVertexAttributeProvider(node -> {
      var map = new java.util.LinkedHashMap<String, org.jgrapht.nio.Attribute>();
      map.put("status", org.jgrapht.nio.DefaultAttribute.createAttribute(node.getStatus().name()));
      return map;
    });
    exporter.setEdgeAttributeProvider(edge -> {
      var map = new java.util.LinkedHashMap<String, org.jgrapht.nio.Attribute>();
      map.put("weight", org.jgrapht.nio.DefaultAttribute.createAttribute(graph.getEdgeWeight(edge)));
      return map;
    });
    exporter.exportGraph(graph, writer);
  }

  public static MyGraph load(Reader reader) {
    MyGraph myGraph = new MyGraph();

    JSONImporter<Node, DefaultWeightedEdge> importer = new JSONImporter<>();
    importer.setVertexFactory(Node::of);
    importer.addVertexAttributeConsumer((pair, attribute) -> {
      Node node = pair.getFirst();
      String key = pair.getSecond();
      if ("status".equals(key)) {
        node.setStatus(Node.STATUS.valueOf(attribute.getValue()));
      }
    });
    importer.addEdgeAttributeConsumer((pair, attribute) -> {
      DefaultWeightedEdge edge = pair.getFirst();
      String key = pair.getSecond();
      if ("weight".equals(key)) {
        myGraph.graph.setEdgeWeight(edge, Double.parseDouble(attribute.getValue()));
      }
    });

    importer.importGraph(myGraph.graph, reader);

    myGraph.start = myGraph.graph.vertexSet().stream()
      .filter(node -> node.getId().equals("START"))
      .findFirst().orElseThrow();
    myGraph.end = myGraph.graph.vertexSet().stream()
      .filter(node -> node.getId().equals("END"))
      .findFirst().orElseThrow();

    try {
      myGraph.currentChallenge = myGraph.getNextUntriedNode().getChallenge();
    } catch (NoPathFoundException | EndReachedException e) {
      myGraph.currentChallenge = null;
    }

    return myGraph;
  }
}
