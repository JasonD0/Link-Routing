import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.Set;
import javax.swing.Timer;

public class Sender implements Runnable {
    private final static int UPDATE_INTERVAL = 1000;
    private DatagramSocket socket;
    private Buffer buffer;
    private Node router;
    private int sequence;
    private int prevPktLength;
    private int failedCount;
    private Timer t;

    public Sender(DatagramSocket socket, Buffer buffer, Node router) {
        this.socket = socket;
        this.buffer = buffer;
        this.router = router;
        this.sequence = 0;
        this.prevPktLength = 0;
        this.failedCount = 0;
        this.t = new Timer(UPDATE_INTERVAL, e -> {
            try {
                LSA();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
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

        t.start();

        try {
            send();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Periodic Broadcast
     * @throws IOException
     */
    private void LSA() throws IOException {
        String pkt = buffer.getPeriodicPacket();
        int count = buffer.getReplacedCount();
        boolean replaced = (failedCount < count);
        boolean increasedPacket = (prevPktLength < pkt.length());

        /* changes sequence number if the LSA has changed */
        String[] splitPkt = pkt.split("/");
        splitPkt[2] = (replaced || increasedPacket) ? String.valueOf(++sequence) : String.valueOf(sequence);
        pkt = String.join("/", splitPkt);

        if (increasedPacket) prevPktLength = pkt.length();
        if (replaced) failedCount = count;

        buffer.addPacket(pkt, true);
    }

    /**
     * Forwarding packets
     * @throws IOException
     */
    void send() throws IOException {
        InetAddress address = InetAddress.getByName("localhost");

        while (true) {
            buffer.doWait();
            String message = buffer.getPacket();

            /* get information related to the immediate sender of the packet */
            String[] splitMsg = message.split("-");
            String[] m = splitMsg[0].split("/");
            String origSender = m[1];
            m[0] = this.router.toString();  // set sender to this router
            String sender = m[0];
            splitMsg[0] = String.join("/", m);
            message = String.join("-", splitMsg);

            byte[] msg = message.getBytes();

            Set<Node> s = router.getNeighbours().keySet();
            for (Node n : s) {
                // don't send packet to the sender of that packet
                if (n.toString().equals(sender) || n.toString().equals(origSender)) continue;
                DatagramPacket packet = new DatagramPacket(msg, msg.length, address, n.getPort());
                socket.send(packet);
            }
        }
    }
}
