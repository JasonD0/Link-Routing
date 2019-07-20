import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Dijkstra implements Runnable {
    private final static long ROUTE_UPDATE_INTERVAL = 30000; // 25 seconds
    private boolean running;
    private Network network;
    private Node router;
    private Map<Node, Double> distance;
    private Map<Node, Node> predecessors;
    private Set<Node> processedNodes;
    private Set<Node> noPathTo;
    private List<Node> nodes;

    public Dijkstra(Network network, Node router) {
        this.network = network;
        this.router = router;
    }

    public synchronized void stop() {
        this.running = false;
    }

    private synchronized boolean isRunning() {
        return this.running;
    }

    private void getPaths() {
        distance = new HashMap<>();
        predecessors = new HashMap<>();
        processedNodes = new HashSet<>();
        noPathTo = new HashSet<>();
        nodes = network.getNodes();

        for (Node n : nodes) {
            distance.put(n, Double.MAX_VALUE);
            noPathTo.add(n);
        }

        distance.put(router, 0.0);

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
                minDist = distance.get(n);

            } else if (Double.compare(distance.getOrDefault(n, Double.MAX_VALUE), minDist) <= 0) {
                minDist = distance.getOrDefault(n, Double.MAX_VALUE);
                minNode = n;
            }
        }

        return minNode;
    }

    private void showPath(Node dest) {
        if (predecessors.get(dest) == null) return;

        ArrayList<String> path = new ArrayList<>();
        Node predecessor = dest;

        path.add(predecessor.toString());
        while ((predecessor = predecessors.get(predecessor)) != null) {
            path.add(predecessor.toString());
        }

        Collections.reverse(path);
        for (String s : path) {
            System.out.print(s);
        }
        System.out.printf(" and the cost is %.2f\n", distance.get(dest));
    }

    private void showPaths() {
        System.out.println("I am Router " + router.toString());
        List<Node> sortedNodes = new ArrayList<>(nodes);
        Collections.sort(sortedNodes, (Node o1, Node o2) -> {
            String n1 = o1.toString();
            String n2 = o2.toString();
            return n1.compareTo(n2);
        });

        for (Node n : sortedNodes) {
            if (n.toString().equals(router.toString())) continue;
            System.out.print("Least cost path to router " + n.toString() + ": ");
            showPath(n);
        }
    }

    @Override
    public void run() {
        this.running = true;

        while (isRunning()) {
            try {
                Thread.sleep(ROUTE_UPDATE_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getPaths();
            showPaths();
        }
        stop();
    }
}
