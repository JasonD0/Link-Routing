import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

public class Receiver implements Runnable {
    private DatagramSocket socket;
    private Network network;
    private Buffer buffer;
    private Buffer processing_buffer;
    private Node router;
    private HashMap<String, Integer> nodeSequence;
    private HashMap<String, Integer> heartBeat;
    private int count;

    public Receiver(DatagramSocket socket, Network network, Buffer buffer, Buffer pb, Node router) {
        this.socket = socket;
        this.network = network;
        this.buffer = buffer;
        this.processing_buffer = pb;
        this.router = router;
        this.nodeSequence = new HashMap<>();
        this.heartBeat = new HashMap<>();
        this.count = 0;
    }

    @Override
    public void run() {
        Processor p = new Processor(network, processing_buffer, router);
        new Thread(p).start();

        for (Node n : router.getNeighbours().keySet()) {
            heartBeat.put(n.toString(), 0);
        }

        receive();
    }

     void receive() {
        int neighbourCount = router.getNeighbours().size();
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
                    nodeSequence.put(sender, -1);

                // indicate sender alive
                } else {
                    heartBeat.put(sender, beat);
                }

                // fail routers not sending packets
                if (count == neighbourCount + 1) {
                    for (Map.Entry<String, Integer> m : heartBeat.entrySet()) {
                        if (m.getValue() == 0) {
                            network.removeNode(m.getKey());
                            buffer.removeRouter(m.getKey());
                            heartBeat.put(m.getKey(), -1);
                            nodeSequence.put(m.getKey(), -1);
                        }
                        else heartBeat.put(m.getKey(), 0);
                    }
                    count = 0;
                }*/

                // ignore unchanged packet
                if (nodeSequence.getOrDefault(origSender, sequence+1) <= sequence) continue;
                nodeSequence.put(origSender, sequence);

                buffer.addPacket(pkt);
                processing_buffer.addProcessingPacket(pkt);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
