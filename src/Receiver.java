import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

public class Receiver implements Runnable {
    private DatagramSocket socket;
    private Network network;
    private Buffer buffer;
    private Node router;
    private boolean running;
    private HashMap<String, Integer> nodeSequence;

    public Receiver(DatagramSocket socket, Network network, Buffer buffer, Node router) {
        this.socket = socket;
        this.network = network;
        this.buffer = buffer;
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
        receive();
    }

     void receive() {
        while (isRunning()) {
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
            try {
                socket.receive(packet);

                // process pkt and add to network
                String msg = new String(packet.getData(), 0, packet.getLength());
                String[] splitMsg = msg.split("/");

                String router = splitMsg[0];
                int sequence = Integer.valueOf(splitMsg[1]);
                int port = Integer.valueOf(splitMsg[2]);

                // ignore unchanged packet
                if (nodeSequence.getOrDefault(router, sequence+1) <= sequence) continue;
                nodeSequence.put(router, sequence);

                Node sender = new Node(router, port);
                sender = network.addNode(sender);

                buffer.addPacket(msg);

                // Get the node's neighbours and costs
                for (int i = 3; i < splitMsg.length; ++i) {
                    String edges = splitMsg[i];
                    String[] edgesInfo = edges.split(" ");
                    if (edgesInfo.length != 3) continue;

                    String routerID = edgesInfo[0];
                    double cost = Double.valueOf(edgesInfo[1]);
                    port = Integer.valueOf(edgesInfo[2]);

                    Node neighbour = new Node(routerID, port);
                    neighbour = network.addNode(neighbour);
                    network.makeEdge(neighbour, sender, cost);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        stop();
    }
}
