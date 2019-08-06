import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Network {
    private Set<Node> nodes;
    private Map<Node, Boolean> failedNodes;

    public Network() {
        this.nodes = Collections.synchronizedSet(new HashSet<>());
        this.failedNodes = Collections.synchronizedMap(new HashMap<>());
    }

    public Node addNode(Node n) {
        this.nodes.add(n);
        this.failedNodes.put(n, false);
        return getNode(n.toString());
    }

    public boolean isFailedNode(String routerID) {
        Node n = getNode(routerID);
        return failedNodes.getOrDefault(n, true);
    }

    public boolean isFailedNode(Node n) {
        return failedNodes.getOrDefault(n, true);
    }

    public Set<Node> getFailedNodes() {
        Set<Node> s = new HashSet<>();
        for (Node n : failedNodes.keySet()) {
            if (!failedNodes.get(n)) continue;
            s.add(n);
        }
        return s;
    }

    private Node getNode(String routerID) {
        for (Node n : nodes) {
            if (!n.toString().equals(routerID)) continue;
            return n;
        }
        return null;
    }

    public void removeNode(String routerID) {
        Node n = getNode(routerID);
        this.nodes.remove(n);
        this.failedNodes.put(n, true);
    }

    public void removeNode(Node n) {
        this.nodes.remove(n);
        this.failedNodes.put(n, true);
    }

    public List<Node> getNodes() {
        return new ArrayList<>(this.nodes);
    }

    public void makeEdge(Node n1, Node n2, double cost) {
        n1.addNeighbour(n2, cost);
        n2.addNeighbour(n1, cost);
    }

    public int getSize() {
        return this.nodes.size();
    }
}
