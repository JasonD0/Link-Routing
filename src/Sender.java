import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

public class Sender implements Runnable {
    private final static long UPDATE_INTERVAL = 1000;
    private DatagramSocket socket;
    private Network network;
    private Node router;
    private boolean running;

    public Sender(DatagramSocket socket, Network network, Node router) {
        this.socket = socket;
        this.network = network;
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
        stop();
    }

   /* void send() throws IOException {
        InetAddress address = InetAddress.getByName("localhost");
        int sequence = 0;

        while (isRunning()) {
            try {
                Thread.sleep(UPDATE_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // router/seqNum/router cost port/*
            String message = router.toString() + "/" + sequence + "/";
            for (Map.Entry<Node, Double> m : router.getNeighbours().entrySet()) {
                Node n = m.getKey();
                message += n.toString() + " " + m.getValue() + " " + n.getPort() + "/";
            }
            message += "/\r\n";

            byte[] msg = message.getBytes();
            for (Node n : network.getNodes()) {
                DatagramPacket packet = new DatagramPacket(msg, msg.length, address, n.getPort());
                socket.send(packet);
            }
        }
    }*/
}
