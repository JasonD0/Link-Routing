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
    private boolean running;
    private Timer t;

    public Sender(DatagramSocket socket, Network network, Buffer buffer, Node router) {
        this.socket = socket;
        this.network = network;
        this.buffer = buffer;
        this.router = router;
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
        String message = router.toString() + "/" + router.toString() + "/" + 0 + "/" + router.getPort() + "/";
        for (Map.Entry<Node, Double> m : router.getNeighbours().entrySet()) {
            Node n = m.getKey();
            message += n.toString() + " " + m.getValue() + " " + n.getPort() + "/";
        }
        buffer.initLSA(message);
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
        InetAddress address = InetAddress.getByName("localhost");
        for (Node n : router.getNeighbours().keySet()) {
            byte[] bytes = buffer.getPeriodicPacket().getBytes();
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, n.getPort());
            socket.send(packet);
        }
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
            String[] splitMsg = message.split("-")[0].split("/");
            String sender = splitMsg[0];
            String origSender = splitMsg[1];
            if (!sender.equals(this.router.toString())) {
                splitMsg[0] = this.router.toString();
                sender = splitMsg[0];
            }

            byte[] msg = String.join("/", splitMsg).getBytes();
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
