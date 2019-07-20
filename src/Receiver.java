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

    // orig sender  and sender of copy  so dont send a rerouted pkt back    eg A-> B   B-> C    stop C -> B   and C->A
     void receive() {
        while (isRunning()) {
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
            try {
                socket.receive(packet);

                String msg = new String(packet.getData(), 0, packet.getLength());
                String[] splitMsg = msg.split("/");

                String origSender = splitMsg[0];
                //String sender = splitMsg[1];
                int sequence = Integer.valueOf(splitMsg[2]);
                int port = Integer.valueOf(splitMsg[3]);

                // ignore unchanged packet
                if (nodeSequence.getOrDefault(origSender, sequence+1) <= sequence) continue;
                nodeSequence.put(origSender, sequence);

                Node senderNode = new Node(origSender, port);
                senderNode = network.addNode(senderNode);

                buffer.addPacket(msg);

                // Get the original sender's neighbours and costs
                for (int i = 3; i < splitMsg.length; ++i) {
                    String edges = splitMsg[i];
                    String[] edgesInfo = edges.split(" ");
                    if (edgesInfo.length != 3) continue;

                    String routerID = edgesInfo[0];
                    double cost = Double.valueOf(edgesInfo[1]);
                    port = Integer.valueOf(edgesInfo[2]);

                    Node neighbour = new Node(routerID, port);
                    neighbour = network.addNode(neighbour);
                    network.makeEdge(neighbour, senderNode, cost);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        stop();
    }
}
