import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import javax.swing.Timer;

public class Sender implements Runnable {
    private final static int UPDATE_INTERVAL = 1000;
    private DatagramSocket socket;
    private Network network;
    private Buffer buffer;
    private Node router;
    private int sequence;
    private boolean running;
    private Timer t;

    public Sender(DatagramSocket socket, Network network, Buffer buffer, Node router) {
        this.socket = socket;
        this.network = network;
        this.buffer = buffer;
        this.router = router;
        this.sequence = 0;
        this.t = new Timer(UPDATE_INTERVAL, e -> {
            try {
                LSA();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
    }

    public synchronized void stop() {
        this.running = false;
        t.stop();
    }

    private synchronized boolean isRunning() {
        return this.running;
    }

    @Override
    public void run() {
        this.running = true;

        /* generate initial LSA */
        // sender  orig sender  orig sender seq  orig sender port
        String message = router.toString() + "/" + router.toString() + "/" + this.sequence + "/" + router.getPort() + "/";
        for (Map.Entry<Node, Double> m : router.getNeighbours().entrySet()) {
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
        String pkt = new String(buffer.getPeriodicPacket());

        /* changes sequence number if the LSA has changed */
        String[] splitPkt = pkt.split("/");
        splitPkt[2] = (buffer.updated()) ? String.valueOf(++sequence) : String.valueOf(sequence);
        pkt = String.join("/", splitPkt);

        buffer.addPacket(pkt);
        buffer.doNotify();
    }

    /**
     * Forwarding packets
     * @throws IOException
     */
    void send() throws IOException {
        InetAddress address = InetAddress.getByName("localhost");

        while (isRunning()) {
            // while packet sender/origSender not failed    if failed -> removePacket(message)
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

            for (Node n : router.getNeighbours().keySet()) {
                // don't send packet to the sender of that packet
                if (n.toString().equals(sender) || n.toString().equals(origSender)) continue;
                DatagramPacket packet = new DatagramPacket(msg, msg.length, address, n.getPort());
                socket.send(packet);
            }
        }
        stop();
    }
}
