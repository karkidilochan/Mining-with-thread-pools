// package csx55.overlay.routing;

// import java.util.HashMap;
// import java.util.Map;

// import csx55.overlay.wireformats.LinkWeights;

// /**
// * RouteCache class stores routing information for overlay nodes.
// * It provides methods to retrieve routes, connections, and print the shortest
// * paths.
// */
// public class RouteCache {

// private String[] nodeConnections;
// private Map<String, String[]> nodeRoutes;
// private String routeString;

// /**
// * Constructor for RouteCache class.
// *
// * @param linkWeights LinkWeights object containing the weights between nodes
// * @param routeString Identifier of the current node
// */
// public RouteCache(LinkWeights linkWeights, String routeString) {
// this.nodeRoutes = new HashMap<>();
// this.routeString = routeString;
// // populate route paths using the shortest path algorithm
// (new Dijkstra()).buildShortestPaths(nodeRoutes, linkWeights, routeString);

// this.nodeConnections = nodeRoutes.keySet().toArray(new
// String[nodeRoutes.size()]);
// }

// /**
// * Retrieves the connection identifier at the specified index.
// *
// * @param index Index of the connection
// */
// public String getConnection(int nodeIndex)
// throws ArrayIndexOutOfBoundsException {
// return nodeConnections[nodeIndex];
// }

// /**
// * Returns the number of connections in the route cache.
// */
// public int getRouteConnectionsCount() {
// return nodeConnections.length;
// }

// /**
// * Retrieves the route for a given destination node.
// *
// * @param destinationNode Identifier of the destination node
// */
// public String[] getRoutes(String destinationNode)
// throws ClassCastException {
// return nodeRoutes.get(destinationNode);
// }

// /**
// * Prints the shortest paths for all destination nodes.
// *
// * @param linkWeights LinkWeights object containing the weights between nodes
// */
// public void printShortestPaths(LinkWeights linkWeights) {
// nodeRoutes.forEach((key, value) -> {
// StringBuilder sb = new StringBuilder();
// String current = routeString;
// sb.append(current);
// for (int i = 0; i < value.length; ++i) {
// String next = value[i];
// sb.append(linkWeights.getWeight(current, next));
// sb.append(next);
// current = next;
// }
// System.out.println(sb.toString());
// });
// System.out.println();
// }
// }
