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

  /**
   * Creates a new MyGraph instance from a GraphML file and initializes node statuses based on user's available challenges.
   * <p>
   * This factory method imports a directed weighted graph from GraphML format, identifies START and END nodes,
   * and sets the initial status of each node based on whether the user has access to that challenge type.
   * START and END nodes are always marked as SUCCESS. Other nodes are marked as UNTRIED if the user has
   * access to their challenge type, or FAILURE otherwise.
   *
   * @param reader the Reader containing GraphML data representing the challenge graph
   * @param user the User object containing the set of challenges available to this user
   * @return a new MyGraph instance with nodes initialized according to user's challenge access
   * @throws java.util.NoSuchElementException if START or END nodes are not found in the graph
   */
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

  /**
   * Retrieves the challenge identifier of the next untried node in the shortest path
   * between the start and end nodes of the graph. If the shortest path does not exist,
   * or if there are no untried nodes along the path, exceptions are thrown to signify
   * specific cases.
   *
   * @return the challenge identifier of the first untried node in the shortest path
   * @throws GetChallengeFailure if no shortest path exists between the start and end nodes
   * @throws GetChallengeSuccess if all nodes along the shortest path are already tried or marked as success
   */
  public String getChallenge() {
    GraphPath<Node, DefaultWeightedEdge> graphPath = Optional.ofNullable(shortestPath.getPath(start, end)).orElseThrow(GetChallengeFailure::new);
    Node node = graphPath.getVertexList().stream().filter(x -> x.getStatus().equals(Node.STATUS.UNTRIED)).findFirst().orElseThrow(GetChallengeSuccess::new);
    return node.getChallenge(); // continue
  }

  /**
   * Updates the status of all nodes in the graph that match the challenge identifier of
   * the current challenge.  The status is updated to the provided value. The status value
   * is case-insensitive and must match one of the allowable statuses in {@link Node.STATUS}.
   *
   * @param status the new status to assign to the matching nodes; must be one of "SUCCESS" or "FAILURE" (case-insensitive)
   * @throws SetStatusException if the update process encounters any failure
   */
  public void setStatus(String status) {
    try {
      String challenge = getChallenge();
      graph.vertexSet().stream().filter(node -> node.getChallenge().equals(challenge)).forEach(node -> node.setStatus(Node.STATUS.valueOf(status.toUpperCase())));
    } catch (RuntimeException ignored) {
      throw new SetStatusException();
    }
  }

  /**
   * Exports the current state of the graph in JSON format to the provided Writer.
   * The exported JSON includes attributes associated with the graph's vertices and edges:
   * - Vertex attributes include the node's status.
   * - Edge attributes include the weight of the edge.
   *
   * @param writer the Writer to which the graph's JSON representation will be output
   */
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

  /**
   * Loads a graph from the provided JSON data represented by the given reader.
   * This method imports a weighted directed graph and extracts `START` and `END`
   * nodes while setting certain attributes for nodes and edges. It expects specific
   * attributes such as node statuses and edge weights to be present and properly
   * formatted within the JSON structure.
   *
   * @param reader the Reader containing JSON data for the graph
   * @return a new MyGraph instance constructed from the JSON data
   * @throws java.util.NoSuchElementException if `START` or `END` nodes are not found in the graph
   */
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
