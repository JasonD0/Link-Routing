import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Lsr {
    final static long UPDATE_INTERVAL = 1000;           // 1 second
    final static long ROUTE_UPDATE_INTERVAL = 30000;    // 30 seconds
    DatagramSocket socket;

    public static void main(String[] args) throws IOException {
        String configName = args[0];
        BufferedReader br = new BufferedReader(new FileReader(new File(configName)));

        Network network = new Network();

        /* process config file */
        String msg = br.readLine();
        String[] splitMsg = msg.split(" ");

        String routerID = splitMsg[0];
        int port = Integer.valueOf(splitMsg[1]);
        Node currNode = new Node(routerID, port);

        msg = br.readLine();
        while ((msg = br.readLine()) != null) {
            splitMsg = msg.split(" ");
            routerID = splitMsg[0];
            Double cost = Double.valueOf(splitMsg[1]);
            port = Integer.valueOf(splitMsg[2]);

            Node neighbourNode = new Node(routerID, port);
            neighbourNode.addEdge(currNode, cost);
            currNode.addEdge(neighbourNode, cost);
            network.addNode(neighbourNode);
        }
        network.addNode(currNode);
    }

    void send() throws IOException {
        InetAddress address = InetAddress.getByName("localhost");
        while (true) {
            /* format pkt data */
            String message = "";
            byte[] msg = message.getBytes();
            DatagramPacket packet = new DatagramPacket(msg, msg.length, address, 1555);
            socket.send(packet);
        }
    }

    void receive() throws IOException {
        while (true) {
            DatagramPacket packet = new  DatagramPacket(new byte[1024], 1024);
            socket.receive(packet);
            /* process pkt and add to network */
            String msg = new String(packet.getData(), 0, packet.getLength());
        }
    }
}
