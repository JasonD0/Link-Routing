import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

public class Sender implements Runnable {
    private final static long UPDATE_INTERVAL = 1000;
    private DatagramSocket socket;
    private Network network;
    private Buffer buffer;
    private Node router;
    private boolean running;

    public Sender(DatagramSocket socket, Network network, Buffer buffer, Node router) {
        this.socket = socket;
        this.network = network;
        this.buffer = buffer;
        this.router = router;
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
        try {
            send();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void send() throws IOException {
        InetAddress address = InetAddress.getByName("localhost");
        int sequence = 0;

        // router/seqNum/router cost port/*
        String message = router.toString() + "/" + router.toString() + "/" + sequence + "/" + router.getPort() + "/";
        for (Map.Entry<Node, Double> m : router.getNeighbours().entrySet()) {
            Node n = m.getKey();
            message += n.toString() + " " + m.getValue() + " " + n.getPort() + "/";
        }
        buffer.addPacket(message);

        while (isRunning()) {
            try {
                Thread.sleep(UPDATE_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            buffer.getPackets();

            message = buffer.getPacket();
            String[] splitMsg = message.split("/");
            String origSender = splitMsg[0];
            String sender = splitMsg[1];
            if (!splitMsg[1].equals(this.router.toString())) {
                splitMsg[1] = this.router.toString();
                sender = splitMsg[1];
            }
            byte[] msg = String.join("/", splitMsg).getBytes();
            for (Node n : router.getNeighbours().keySet()) {
                if (n.toString().equals(sender) || n.toString().equals(origSender)) continue;  // don't send packet to the sender of that packet
                DatagramPacket packet = new DatagramPacket(msg, msg.length, address, n.getPort());
                socket.send(packet);
            }
        }
        stop();
    }
}
