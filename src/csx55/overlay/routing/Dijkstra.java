// package csx55.overlay.routing;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;

// import csx55.overlay.wireformats.LinkWeights;

// /**
// * Dijkstra calculates shortest paths in a network graph using Dijkstra's
// * algorithm.
// */
// public class Dijkstra {

// private int[][] adjacencyMatrix;

// private static final int NO_PARENT = -1;

// /**
// * Build shortest paths from the current node to all other nodes in the
// network
// * graph.
// *
// * @param shortestPaths A map to store the shortest paths for each node.
// * @param linkWeights Link weights representing the edges of the graph.
// * @param currentNode The current node from which to calculate the shortest
// * paths.
// */
// public void buildShortestPaths(Map<String, String[]> shortestPaths,
// LinkWeights linkWeights, String currentNode) {

// String[] links = linkWeights.getLinks();

// List<String> nodes = new ArrayList<>();

// transformLinksToNodes(nodes, links);

// int numberOfNodes = nodes.size();
// adjacencyMatrix = new int[numberOfNodes][numberOfNodes];
// for (String connection : links) {
// String[] item = connection.split("\\s+");
// addEdge(nodes.indexOf(item[0]),
// nodes.indexOf(item[1]),
// Integer.parseInt(item[2]));
// }
// int indexOfStart = nodes.indexOf(currentNode);
// int[] parents = computeShortestPath(indexOfStart);

// for (int current = 0; current < numberOfNodes; ++current) {
// if (current != indexOfStart) {
// List<String> addresses = new ArrayList<>();
// buildPath(indexOfStart, current, parents, nodes, addresses);
// shortestPaths.put(nodes.get(current),
// addresses.toArray(new String[] {}));
// }
// }
// }

// /**
// * Transforms links represented as strings into a list of nodes.
// *
// * @param nodes A list to store the unique nodes extracted from the links.
// * @param links An array of strings representing links between nodes.
// */
// private void transformLinksToNodes(List<String> nodes, String[] links) {
// for (String link : links) {
// String[] item = link.split("\\s+");
// if (!nodes.contains(item[0])) {
// nodes.add(item[0]);
// }
// if (!nodes.contains(item[1])) {
// nodes.add(item[1]);
// }
// }
// }

// /**
// * Adds an edge between two nodes in the network graph.
// *
// * @param source The index of the source node.
// * @param destination The index of the destination node.
// * @param weight The weight of the edge.
// */
// public void addEdge(int source, int destination, int weight) {
// adjacencyMatrix[source][destination] = weight;
// adjacencyMatrix[destination][source] = weight;
// }

// /**
// * Apply Dijkstra's algorithm to find the shortest paths from a source node to
// * all other nodes.
// *
// * @param source The index of the source node.
// */
// public int[] computeShortestPath(int source) {
// int nodesCount = adjacencyMatrix[0].length;

// int[] shortestPaths = new int[nodesCount];

// boolean[] visited = new boolean[nodesCount];

// for (int index = 0; index < nodesCount; index++) {
// shortestPaths[index] = Integer.MAX_VALUE;
// visited[index] = false;
// }

// int[] parents = new int[nodesCount];
// parents[source] = NO_PARENT;

// shortestPaths[source] = 0;

// for (int i = 1; i < nodesCount; i++) {
// int nearestVertex = -1;
// int minDistance = Integer.MAX_VALUE;
// for (int nodeIndex = 0; nodeIndex < nodesCount; nodeIndex++) {
// if (!visited[nodeIndex] && shortestPaths[nodeIndex] < minDistance) {
// nearestVertex = nodeIndex;
// minDistance = shortestPaths[nodeIndex];
// }
// }

// visited[nearestVertex] = true;

// for (int nodeIndex = 0; nodeIndex < nodesCount; nodeIndex++) {
// int edgeDistance = adjacencyMatrix[nearestVertex][nodeIndex];

// if (edgeDistance > 0 && ((minDistance + edgeDistance) <
// shortestPaths[nodeIndex])) {
// parents[nodeIndex] = nearestVertex;
// shortestPaths[nodeIndex] = minDistance + edgeDistance;
// }
// }
// }
// return parents;
// }

// /**
// * Recursively builds the shortest path from the current node to the source
// * node.
// *
// * @param source The index of the source node.
// * @param current The index of the current node.
// * @param parents An array representing the parents of each node in the
// shortest
// * path tree.
// * @param nodes A list of node names.
// * @param paths A list to store the nodes in the shortest path.
// */
// private void buildPath(int source, int current, int[] parents,
// List<String> nodes, List<String> paths) {
// if (current == NO_PARENT) {
// return;
// }
// buildPath(source, parents[current], parents, nodes, paths);
// if (current != source) {
// paths.add(nodes.get(current));
// }
// }
// }
