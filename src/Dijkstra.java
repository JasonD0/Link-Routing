import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Dijkstra {
    private Map<Node, Double> distance;
    private Map<Node, Node> predecessors;
    private Set<Node> processedNodes;
    private Set<Node> noPathTo;
    private Set<Node> nodes;

    public void getPaths(Network network, Node src) {
        distance = new HashMap<>();
        predecessors = new HashMap<>();
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

        showPaths(src);
    }

    private void minPath(Node curr) {
        for (Node n : curr.getNeighbours().keySet()) {
            if (!processedNodes.contains(n) &&
                    Double.compare(distance.get(curr), Double.MAX_VALUE) != 0 &&
                    distance.get(curr) + curr.getNeighbours().get(n) < distance.get(n)) {
                distance.put(n, distance.get(curr) + curr.getNeighbours().get(n));
                noPathTo.add(n);
                predecessors.put(n, curr);
            }
        }
    }

    private Node minNodeDistance() {
        Node minNode = null;
        Double minDist = Double.MAX_VALUE;

        for (Node n : noPathTo) {
            if (minNode == null) {
                minNode = n;

            } else if (Double.compare(distance.getOrDefault(n, Double.MAX_VALUE), minDist) <= 0) {
                minDist = distance.getOrDefault(n, Double.MAX_VALUE);
                minNode = n;
            }
        }

        return minNode;
    }

    public void showPath(Node dest) {
        if (predecessors.get(dest) == null) return;

        ArrayList<String> path = new ArrayList<>();
        Node pred = dest;

        path.add(pred.toString());
        while ((pred = predecessors.get(pred)) != null) {
            path.add(pred.toString());
        }

        Collections.reverse(path);
        for (String s : path) {
            System.out.print(s);
        }
        System.out.println(" and the cost is " + distance.get(dest));
    }

    public void showPaths(Node src) {
        System.out.println("I am Router " + src.toString());
        List<Node> sortedNodes = new ArrayList<>(nodes);
        Collections.sort(sortedNodes, (Node o1, Node o2) -> {
            String n1 = o1.toString();
            String n2 = o2.toString();
            return n1.compareTo(n2);
        });

        for (Node n : sortedNodes) {
            if (n.toString().equals(src.toString())) continue;
            System.out.print("Least cost path to router " + n.toString() + ": ");
            showPath(n);
        }
    }
}
