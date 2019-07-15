import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
        return new HashMap<>(this.neighbours);
    }

    public void addNeighbour(Node n, double cost) {
        neighbours.put(n, cost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;

        Node n = (Node) o;
        return (this.routerID.equals(n.routerID) && this.port == n.port);
    }

    @Override
    public String toString() {
        return this.routerID;
    }
}
