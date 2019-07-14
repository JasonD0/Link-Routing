import java.util.HashSet;
import java.util.Set;

public class Network {
    private Set<Node> nodes;
    private Dijkstra alg;

    public Network() {
        this.nodes = new HashSet<>();
    }

    public void addNode(Node n) {
        this.nodes.add(n);
    }

    public Node getNode(String routerID) {
        for (Node n : nodes) {
            if (!n.toString().equals(routerID)) continue;
            return n;
        }
        return null;
    }

    public Set<Node> getNodes() {
        return this.nodes;
    }

    public void makeEdge(Node n1, Node n2, double cost) {
        n1.addEdge(n2, cost);
        n2.addEdge(n1, cost);
    }

    public void getPaths(Node src) {
        this.alg.getPaths(this, src);
    }
}
