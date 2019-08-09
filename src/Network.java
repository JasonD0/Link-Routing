import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Network {
    private static Set<Node> nodes;
    private static Map<Node, Boolean> failedNodes;

    public Network() {
        nodes = Collections.synchronizedSet(new HashSet<>());
        failedNodes = Collections.synchronizedMap(new HashMap<>());
    }

    public Node addNode(Node n) {
        synchronized (nodes) {
            nodes.add(n);
        }
        synchronized (failedNodes) {
            failedNodes.put(n, false);
        }
        return getNode(n.toString());
    }

    public boolean isFailedNode(Node n) {
        synchronized (failedNodes) {
            return failedNodes.getOrDefault(n, true);
        }
    }

    private Node getNode(String routerID) {
        Set<Node> nodesCpy;
        synchronized (nodes) {
            nodesCpy = new HashSet<>(nodes);
        }
        for (Node n : nodesCpy) {
            if (!n.toString().equals(routerID)) continue;
            return n;
        }
        return null;
    }

    public void removeNode(String routerID) {
        Node n = getNode(routerID);
        synchronized (nodes) {
            nodes.remove(n);
        }
        synchronized (failedNodes) {
            failedNodes.put(n, true);
        }
    }

    public void removeNode(Node n) {
        synchronized (nodes) {
            nodes.remove(n);
        }
        synchronized (failedNodes) {
            failedNodes.put(n, true);
        }
    }

    public List<Node> getNodes() {
        synchronized (nodes) {
            return new ArrayList<>(nodes);
        }
    }

    public void makeEdge(Node n1, Node n2, double cost) {
        n1.addNeighbour(n2, cost);
        //n2.addNeighbour(n1, cost);
    }
}
