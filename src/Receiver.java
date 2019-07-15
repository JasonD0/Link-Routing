import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

public class Receiver implements Runnable {
    private DatagramSocket socket;
    private Network network;
    private Node router;
    private boolean running;
    private HashMap<Node, Integer> nodeSequence;

    public Receiver(DatagramSocket socket, Network network, Node router) {
        this.socket = socket;
        this.network = network;
        this.router = router;
        this.nodeSequence = new HashMap<>();
    }

    public synchronized void stop() {
        this.running = false;
    }

    private synchronized boolean isRunning() {
        return this.running;
    }

    @Override
    public void run() {
        this.running = true;
        stop();
    }

     /*void receive() throws IOException {
        while (isRunning()) {
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
            socket.receive(packet);

            // process pkt and add to network
            String msg = new String(packet.getData(), 0, packet.getLength());
            String[] splitMsg = msg.split("/");

            String node = splitMsg[0];
            int sequence = Integer.valueOf(splitMsg[1]);

            // received unchanged packet
            if (nodeSequence.containsKey(node)) {
                if (nodeSequence.get(node) <= sequence) continue;
            } else {
                nodeSequence.put(node, sequence);
            }

            // Get the node's neighbours and costs
            for (int i = 2; i < splitMsg.length - 1; ++i) {
                String edges = splitMsg[i];
                String[] edgesInfo = edges.split(" ");
                String routerID = edgesInfo[0];
                double cost = Double.valueOf(edgesInfo[1]);
                int port = Integer.valueOf(edgesInfo[2]);

                Node neighbour = new Node(routerID, port);
                network.addNode(neighbour);
                network.makeEdge(neighbour, this.router, cost);
            }
        }
    }*/
}
