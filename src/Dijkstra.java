import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Dijkstra {
    private HashMap<Node, Double> distance;
    private HashMap<String, String> pred;
    private HashSet<Node> processedNodes;
    private HashSet<Node> noPathTo;
    private Set<Node> nodes;

    public void getPaths(Network network, Node src) {
        distance = new HashMap<>();
        pred = new HashMap<>();
        processedNodes = new HashSet<>();
        noPathTo = new HashSet<>();
        nodes = network.getNodes();

        for (Node n : nodes) {
            distance.put(n, Double.MAX_VALUE);
            noPathTo.add(n);
        }

        distance.put(src, 0.0);

        while (!noPathTo.isEmpty()) {
            Node curr = minNodeDistance();
            processedNodes.add(curr);
            noPathTo.remove(curr);
            minPath(curr);
        }
    }

    private void minPath(Node curr) {
        for (Node n : curr.getNeighbours().keySet()) {
            if (!processedNodes.contains(n) &&
                    Double.compare(distance.get(n), Double.MAX_VALUE) != 0 &&
                    Double.compare(distance.get(curr), Double.MAX_VALUE) != 0 &&
                    distance.get(n) + curr.getNeighbours().get(n) < distance.get(curr)) {
                distance.put(curr, distance.get(n) + curr.getNeighbours().get(n));
                noPathTo.add(n);
                pred.put(n.toString(), curr.toString());
            }
        }
    }

    private Node minNodeDistance() {
        Node minNode = null;
        Double minDist = Double.MAX_VALUE;

        for (Node n : noPathTo) {
            if (minNode == null) {
                minNode = n;

            } else if (distance.getOrDefault(n, Double.MAX_VALUE) < minDist) {
                minDist = distance.getOrDefault(n, Double.MAX_VALUE);
                minNode = n;
            }
        }

        return minNode;
    }

    public void showPaths(Node src) {

    }
}
