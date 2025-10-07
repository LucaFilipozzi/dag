// © 2025 Luca Filipozzi. Some rights reserved. See LICENSE.
package com.github.lucafilipozzi.dag;

import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.MaskSubgraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.graphml.GraphMLImporter;
import org.jgrapht.nio.json.JSONExporter;
import org.jgrapht.nio.json.JSONImporter;

public class MyGraph {
  private static final String EDGE_WEIGHT_ATTRIBUTE_NAME =  "weight";
  private static final String NODE_STATUS_ATTRIBUTE_NAME =  "status";
  private final Graph<Node, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
  private final Predicate<Node> vertexMask = node -> node.getStatus().equals(Node.STATUS.FAILURE);
  private final Predicate<DefaultWeightedEdge> edgeMask = edge -> false; // edgeMask not used
  private final Graph<Node, DefaultWeightedEdge> maskSubgraph = new MaskSubgraph<>(graph, vertexMask, edgeMask);
  private final DijkstraShortestPath<Node, DefaultWeightedEdge> shortestPath = new DijkstraShortestPath<>(maskSubgraph);
  private Node start;
  private Node end;

  static class GetChallengeSuccess extends RuntimeException { /* intentionally empty */ }
  static class GetChallengeFailure extends RuntimeException { /* intentionally empty */ }
  static class SetStatusException extends RuntimeException {  /* intentionally empty */ }

  private MyGraph() { /* hide constructor */ }

  public static MyGraph of(Reader reader, User user) {
    MyGraph myGraph = new MyGraph();

    GraphMLImporter<Node, DefaultWeightedEdge> importer = new GraphMLImporter<>();
    importer.setEdgeWeightAttributeName(EDGE_WEIGHT_ATTRIBUTE_NAME);
    importer.setVertexFactory(Node::of);
    importer.importGraph(myGraph.graph, reader);

    myGraph.start = myGraph.graph.vertexSet().stream().filter(node -> node.getId().equals("START")).findFirst().orElseThrow();
    myGraph.end = myGraph.graph.vertexSet().stream().filter(node -> node.getId().equals("END")).findFirst().orElseThrow();

    myGraph.start.setStatus(Node.STATUS.SUCCESS);
    myGraph.end.setStatus(Node.STATUS.SUCCESS);
    myGraph.graph.vertexSet().stream()
      .filter(node -> !node.equals(myGraph.start) && !node.equals(myGraph.end))
      .forEach(node -> node.setStatus(user.getChallenges().contains(node.getChallenge()) ? Node.STATUS.UNTRIED : Node.STATUS.FAILURE));

    return myGraph;
  }

  public String getChallenge() {
    GraphPath<Node, DefaultWeightedEdge> graphPath = Optional.ofNullable(shortestPath.getPath(start, end)).orElseThrow(GetChallengeFailure::new);
    Node node = graphPath.getVertexList().stream().filter(x -> x.getStatus().equals(Node.STATUS.UNTRIED)).findFirst().orElseThrow(GetChallengeSuccess::new);
    return node.getChallenge(); // continue
  }

  public void setStatus(String status) {
    try {
      String challenge = getChallenge();
      graph.vertexSet().stream().filter(node -> node.getChallenge().equals(challenge)).forEach(node -> node.setStatus(Node.STATUS.valueOf(status.toUpperCase())));
    } catch (RuntimeException ignored) {
      throw new SetStatusException();
    }
  }

  public void dump(Writer writer) {
    JSONExporter<Node, DefaultWeightedEdge> exporter = new JSONExporter<>();
    exporter.setVertexIdProvider(Node::getId);
    exporter.setVertexAttributeProvider(node -> {
      Map<String, Attribute> map = new HashMap<>();
      map.put(NODE_STATUS_ATTRIBUTE_NAME, DefaultAttribute.createAttribute(node.getStatus().name()));
      return map;
    });
    exporter.setEdgeAttributeProvider(edge -> {
      Map<String, Attribute> map = new HashMap<>();
      map.put(EDGE_WEIGHT_ATTRIBUTE_NAME, DefaultAttribute.createAttribute(graph.getEdgeWeight(edge)));
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
      if (NODE_STATUS_ATTRIBUTE_NAME.equals(key)) {
        node.setStatus(Node.STATUS.valueOf(attribute.getValue()));
      }
    });
    importer.addEdgeAttributeConsumer((pair, attribute) -> {
      DefaultWeightedEdge edge = pair.getFirst();
      String key = pair.getSecond();
      if (EDGE_WEIGHT_ATTRIBUTE_NAME.equals(key)) {
        myGraph.graph.setEdgeWeight(edge, Double.parseDouble(attribute.getValue()));
      }
    });

    importer.importGraph(myGraph.graph, reader);

    myGraph.start = myGraph.graph.vertexSet().stream().filter(node -> node.getId().equals("START")).findFirst().orElseThrow();
    myGraph.end = myGraph.graph.vertexSet().stream().filter(node -> node.getId().equals("END")).findFirst().orElseThrow();

    return myGraph;
  }
}
