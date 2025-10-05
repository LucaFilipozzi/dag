// © 2025 Luca Filipozzi. Some rights reserved. See LICENSE.
package com.github.lucafilipozzi.dag;

import java.io.Reader;
import java.util.Optional;
import java.util.function.Predicate;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.MaskSubgraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.nio.graphml.GraphMLImporter;

public class MyGraph {
  private final Graph<Node, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
  private final Predicate<Node> vertexMask = node -> node.getStatus().equals(Node.STATUS.FAILURE);
  private final Predicate<DefaultWeightedEdge> edgeMask = edge -> false; // edgeMask not used
  private final Graph<Node, DefaultWeightedEdge> maskSubgraph = new MaskSubgraph<>(graph, vertexMask, edgeMask);
  private final DijkstraShortestPath<Node, DefaultWeightedEdge> shortestPath = new DijkstraShortestPath<>(maskSubgraph);
  private final Node start;
  private final Node end;
  private String currentChallenge = null;

  static class EndReachedException extends RuntimeException { /* intentionally empty */ }
  static class NoPathFoundException extends RuntimeException { /* intentionally empty */ }

  MyGraph(Reader reader, User user) {
    GraphMLImporter<Node, DefaultWeightedEdge> importer = new GraphMLImporter<>();
    importer.setEdgeWeightAttributeName("weight");
    importer.setVertexFactory(id -> Node.builder().id(id).build());
    importer.importGraph(graph, reader);

    start = graph.vertexSet().stream().filter(node -> node.getId().equals("START")).findFirst().orElseThrow();
    end = graph.vertexSet().stream().filter(node -> node.getId().equals("END")).findFirst().orElseThrow();

    start.setStatus(Node.STATUS.SUCCESS);
    end.setStatus(Node.STATUS.SUCCESS);
    graph.vertexSet().stream()
        .filter(node -> !node.equals(start) && !node.equals(end))
        .forEach(node -> node.setStatus(user.getChallenges().contains(node.getChallenge()) ? Node.STATUS.UNTRIED : Node.STATUS.FAILURE));

    currentChallenge = getNextUntriedNode().getChallenge();
  }

  private Node getNextUntriedNode() {
    GraphPath<Node, DefaultWeightedEdge> graphPath = Optional.ofNullable(shortestPath.getPath(start, end)).orElseThrow(NoPathFoundException::new);
    return graphPath.getVertexList().stream().filter(node -> node.getStatus().equals(Node.STATUS.UNTRIED)).findFirst().orElseThrow(EndReachedException::new);
  }

  private void setNodesStatus(String challenge, Node.STATUS status) {
    graph.vertexSet().stream().filter(node -> node.getChallenge().equals(challenge)).forEach(node -> node.setStatus(status));
  }

  public String GET() {
    return currentChallenge;
  }

  public String POST(String status) {
    if (currentChallenge == null) {
      return "invalid";
    }
    setNodesStatus(currentChallenge, Node.STATUS.valueOf(status.toUpperCase()));
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
}
