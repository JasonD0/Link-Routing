import java.util.Vector;

public class Network {
    private Vector<Node> nodes;
    private Dijkstra alg;

    public Network() {
        this.nodes = new Vector<>();
    }

    public void addNode(Node n) {
        this.nodes.add(n);
    }

    public Node getNode(String routerID) {
        for (Node n : nodes) {
            if (!n.getRouterID().equals(routerID)) continue;
            return n;
        }
        return null;
    }

    public void getPaths() {
        this.alg.getPaths();
    }
}
