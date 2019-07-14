import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Dijkstra {
    private HashMap<Node, Double> distance;
    private HashMap<Node, Node> predecessors;
    private HashSet<Node> processedNodes;
    private HashSet<Node> noPathTo;
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
    }

    private void minPath(Node curr) {
        for (Node n : curr.getNeighbours().keySet()) {
            if (!processedNodes.contains(n) &&
                    Double.compare(distance.get(n), Double.MAX_VALUE) != 0 &&
                    distance.get(n) + curr.getNeighbours().get(n) < distance.get(curr)) {
                distance.put(curr, distance.get(n) + curr.getNeighbours().get(n));
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

            } else if (distance.getOrDefault(n, Double.MAX_VALUE) < minDist) {
                minDist = distance.getOrDefault(n, Double.MAX_VALUE);
                minNode = n;
            }
        }

        return minNode;
    }

    public void showPath(Node dest) {
        if (predecessors.get(dest) == null) return;
        ArrayList<String> path = new ArrayList<>();
        Node pred;
        int cost = 0;
        path.add(dest.toString());

        /*while ((pred = predecessors.get(dest)) != null) {
            path.add(pred.toString());
        }*/
        for (Map.Entry<Node, Node> m : predecessors.entrySet()) {
            path.add(m.getKey().toString());
            cost += m.getKey().getNeighbours().get(m.getValue());
        }

        Collections.sort(path);
        for (String s : path) {
            System.out.println(s);
        }
        System.out.println(" and the cost is ");
    }

    public void showPaths(Node src) {
        System.out.println("I am Router " + src.toString());
        for (Node n : nodes) {
            System.out.println("Least cost path to router " + n.toString() + ": ");
            showPath(n);
        }
    }
}
