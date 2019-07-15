import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Network {
    private Set<Node> nodes;

    public Network() {
        this.nodes = Collections.synchronizedSet(new HashSet<>());
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
