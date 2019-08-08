import java.util.Map;

public class Processor implements Runnable {
    private Network network;
    private Buffer buffer;
    private Node router;

    public Processor(Network network, Buffer buffer, Node router) {
        this.network = network;
        this.buffer = buffer;
        this.router = router;
    }

    @Override
    public void run() {
        while (true) {
            buffer.doProcessingWait();

            String pkt = buffer.getProcessingPacket();
            String[] splitPkt = pkt.split("-");

            if (splitPkt.length == 0) continue;

            for (int j = 0; j < splitPkt.length; ++j) {
                String[] splitLSA = splitPkt[j].split("/");

                // router information
                String routerID = splitLSA[1];  // orig sender
                if (routerID.equals(this.router.toString())) continue;

                Node nodeOne = new Node(routerID, Integer.valueOf(splitLSA[3]));
                nodeOne = network.addNode(nodeOne);

                Map<Node, Double> neighbours = nodeOne.getNeighbours();
                // add routers' neighbours
                for (int i = 4; i < splitLSA.length; ++i) {
                    String edges = splitLSA[i];
                    String[] edgesInfo = edges.split(" ");
                    if (edgesInfo.length != 3) continue;

                    String neighbour = edgesInfo[0];
                    double cost = Double.valueOf(edgesInfo[1]);
                    int port = Integer.valueOf(edgesInfo[2]);

                    Node nodeTwo = new Node(neighbour, port);
                    nodeTwo = network.addNode(nodeTwo);
                    network.makeEdge(nodeOne, nodeTwo, cost);

                    neighbours.remove(nodeTwo);
                }

                buffer.addLSA(splitPkt[j], routerID, false);
            }
        }
    }
}
