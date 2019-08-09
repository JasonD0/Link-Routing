import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Receiver implements Runnable {
    private DatagramSocket socket;
    private Network network;
    private Buffer buffer;
    private Buffer processing_buffer;
    private Node router;
    private Map<String, Integer> nodeSequence;
    private HashMap<String, Integer> heartBeat;
    private int count;

    public Receiver(DatagramSocket socket, Network network, Buffer buffer, Node router) {
        this.socket = socket;
        this.network = network;
        this.buffer = buffer;
        this.processing_buffer = new Buffer();
        this.router = router;
        this.nodeSequence = Collections.synchronizedMap(new HashMap<>());
        this.heartBeat = new HashMap<>();
        this.count = 0;
    }

    @Override
    public void run() {
        /* generate initial LSA */
        // sender  orig sender  orig sender seq  orig sender port
        String message = router.toString() + "/" + router.toString() + "/" + 0 + "/" + router.getPort() + "/";
        Set<Map.Entry<Node, Double>> neighbours = router.getNeighbours().entrySet();
        for (Map.Entry<Node, Double> m : neighbours) {
            Node n = m.getKey();
            message += n.toString() + " " + m.getValue() + " " + n.getPort() + "/";
        }
        buffer.initLSA(router.toString(), message);

        Processor p = new Processor(network, processing_buffer, router, nodeSequence);
        new Thread(p).start();

        for (Node n : router.getNeighbours().keySet()) {
            heartBeat.put(n.toString(), 0);
        }

        receive();
    }

     void receive() {
        int neighbourCount = (router.getNeighbours().size() < 3) ? 3 : router.getNeighbours().size();
        while (true) {
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
            try {
                socket.receive(packet);

                String pkt = new String(packet.getData(), 0, packet.getLength());
                String[] splitPkt = pkt.split("-");

                if (splitPkt.length == 0) continue;

                /* get information related to the original sender of the packet */
                String[] origSenderInfo = splitPkt[0].split("/");
                String sender = origSenderInfo[0];
                String origSender = origSenderInfo[1];
                int sequence = Integer.valueOf(origSenderInfo[2]);

                // change packets here when neighbour died
                // detecting failed router neighbours
                // ignore origsender  might not be neighbour
                /*int beat = heartBeat.getOrDefault(sender, 0) + 1;
                count++;

                // failed router (sender) came back
                if (beat == 0) {
                    heartBeat.put(sender, 1);
                    synchronized (nodeSequence) {
                        nodeSequence.remove(sender);    // so when get packet from sender   process no matter what
                    }

                // sender wasn't indicated as failed, update beat
                } else {
                    heartBeat.put(sender, beat);
                }

                // fail routers not sending packets
                if (count == neighbourCount * 2) {
                    for (Map.Entry<String, Integer> m : heartBeat.entrySet()) {
                        // router died
                        if (m.getValue() == 0) {
                            network.removeNode(m.getKey());
                            buffer.removeRouter(m.getKey());
                            heartBeat.put(m.getKey(), -1);
                        }
                        else if (m.getValue() != -1) heartBeat.put(m.getKey(), 0);  // reset heartbeat
                        // if -1 stay dead
                    }
                    count = 0;
                }
*/
                // drop unchanged packet
                synchronized (nodeSequence) {
                    if (nodeSequence.getOrDefault(origSender, sequence + 1) <= sequence) continue;
                    nodeSequence.put(origSender, sequence);
                }

                buffer.addPacket(pkt, false);
                processing_buffer.addProcessingPacket(pkt);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
