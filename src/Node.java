import java.util.HashMap;

public class Node {
    private String routerID;
    private int port;
    private HashMap<Node, Double> neighbours;

    public Node(String routerID, int port) {
        this.routerID = routerID;
        this.port = port;
        this.neighbours = new HashMap<>();
    }

    public String getRouterID() {
        return this.routerID;
    }

    public int getPort() {
        return this.port;
    }

    public HashMap<Node, Double> getNeighbours() {
        return this.neighbours;
    }

    public void addEdge(Node n, double cost) {
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
