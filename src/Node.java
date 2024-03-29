import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Node {
    private String routerID;
    private int port;
    private Map<Node, Double> neighbours;

    public Node(String routerID, int port) {
        this.routerID = routerID;
        this.port = port;
        this.neighbours = Collections.synchronizedMap(new HashMap<>());
    }

    public String getRouterID() {
        return this.routerID;
    }

    public int getPort() {
        return this.port;
    }

    public Map<Node, Double> getNeighbours() {
        synchronized (neighbours) {
            return new HashMap<>(neighbours);
        }
    }

    public void addNeighbour(Node n, double cost) {
        synchronized (neighbours) {
            neighbours.put(n, cost);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (o instanceof Node) {
            Node n = (Node) o;
            return (this.routerID.equals(n.toString()) && this.port == n.getPort());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.routerID, this.port);
    }

    @Override
    public String toString() {
        return this.routerID;
    }
}
