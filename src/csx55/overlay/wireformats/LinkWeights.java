// package csx55.overlay.wireformats;

// import java.io.BufferedInputStream;
// import java.io.BufferedOutputStream;
// import java.io.ByteArrayInputStream;
// import java.io.ByteArrayOutputStream;
// import java.io.DataInputStream;
// import java.io.DataOutputStream;
// import java.io.IOException;
// import java.util.Random;

// import csx55.overlay.node.OverlayNode;

// public class LinkWeights implements Event {
// private int type;

// private int numberOfLinks;

// // TODO: check if it can be replaced with List<String>, COMPARE
// private String[] links;

// private boolean isWeightsSent;

// // constructor to create new weighted links between nodes
// public LinkWeights(OverlayNode[] topology) {
// this.type = Protocol.LINK_WEIGHTS;

// for (OverlayNode node : topology) {
// this.numberOfLinks += node.getNumberofPeers();
// }

// this.links = new String[numberOfLinks];

// // create links and assign weights from peers of each node of a topology
// Random random = new Random();

// final int minWeight = 1;
// final int maxWeight = 10;

// int index = 0;
// for (OverlayNode node : topology) {
// for (String peer : node.getAllPeers()) {
// links[index++] = node.getHostPortString() +
// " " + peer + " "
// + Integer.toString(random.ints(minWeight, maxWeight +
// 1).findFirst().getAsInt());
// }
// }
// }

// // constructor to unmarshall bytes array to class fields
// public LinkWeights(byte[] marshalledData) throws IOException {
// ByteArrayInputStream inputData = new ByteArrayInputStream(marshalledData);
// DataInputStream din = new DataInputStream(new
// BufferedInputStream(inputData));

// this.type = din.readInt();

// this.numberOfLinks = din.readInt();

// int linksLength = din.readInt();

// this.links = new String[linksLength];

// for (int i = 0; i < linksLength; i++) {
// int len = din.readInt();
// byte[] bytes = new byte[len];
// din.readFully(bytes);
// this.links[i] = new String(bytes);
// }

// inputData.close();
// din.close();
// }

// public void setWeightsSent(boolean weightsSent) {
// this.isWeightsSent = weightsSent;
// }

// public boolean hasWeightsSent() {
// return this.isWeightsSent;
// }

// public int getNumberOfLinks() {
// return numberOfLinks;
// }

// public String[] getLinks() {
// return links;
// }

// public int getType() {
// return type;
// }

// // get marshalled data from class fields
// public byte[] getBytes() throws IOException {
// byte[] marshalledData = null;

// ByteArrayOutputStream opStream = new ByteArrayOutputStream();
// DataOutputStream dout = new DataOutputStream(new
// BufferedOutputStream(opStream));

// dout.writeInt(type);

// dout.writeInt(numberOfLinks);

// dout.writeInt(links.length);

// for (String link : links) {
// byte[] bytes = link.getBytes();
// dout.writeInt(bytes.length);
// dout.write(bytes);
// }

// dout.flush();
// marshalledData = opStream.toByteArray();

// opStream.close();
// dout.close();
// return marshalledData;
// }

// public String toString() {
// StringBuilder stringBuilder = new StringBuilder();
// for (String link : links) {
// stringBuilder.append("\t" + link + "\n");
// }
// String msg = "There are " + Integer.toString(numberOfLinks) + " total
// links:\n";
// return msg + stringBuilder.toString();
// }

// public String getWeight(String current, String next) {
// StringBuilder sb = new StringBuilder();

// for (int i = 0; i < links.length; ++i) {
// String link = links[i];
// if (link.contains(current) && link.contains(next)) {
// int weight = Integer.parseInt(link.split("\\s+")[2]);
// sb.append("--");
// sb.append(String.format("%02d", weight));
// sb.append("--");
// break;
// }
// }

// return sb.toString();
// }
// }
