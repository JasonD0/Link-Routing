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

                String pkt = new String(packet.getData(), 0, packet.getLength());
                String[] splitPkt = pkt.split("-");
                if (splitPkt.length == 0) continue;

                /* get information related to the original sender of the packet */
                String[] origSenderInfo = splitPkt[0].split("/");
                String origSender = origSenderInfo[1];
                int sequence = Integer.valueOf(origSenderInfo[2]);

                // ignore unchanged packet
                if (nodeSequence.getOrDefault(origSender, sequence+1) <= sequence) continue;
                nodeSequence.put(origSender, sequence);

                buffer.addPacket(pkt);
                buffer.doNotify();

                /* add each router's information in the packet to the topology */
                for (int j = 0; j < splitPkt.length; ++j) {
                    String[] splitLSA = splitPkt[j].split("/");

                    // router information
                    Node nodeOne = new Node(splitLSA[1], Integer.valueOf(splitLSA[3]));
                    nodeOne = network.addNode(nodeOne);

                    if (!splitLSA[1].equals(this.router.toString())) buffer.addPacket(splitPkt[j], splitLSA[1]);

                    for (int i = 3; i < splitLSA.length; ++i) {
                        // adding to topology
                        String edges = splitLSA[i];
                        String[] edgesInfo = edges.split(" ");
                        if (edgesInfo.length != 3) continue;

                        String routerID = edgesInfo[0];
                        double cost = Double.valueOf(edgesInfo[1]);
                        int port = Integer.valueOf(edgesInfo[2]);

                        Node nodeTwo = new Node(routerID, port);
                        nodeTwo = network.addNode(nodeTwo);
                        network.makeEdge(nodeOne, nodeTwo, cost);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        stop();
    }
}
