import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class Lsr {
    final static long UPDATE_INTERVAL = 1000;           // 1 second
    final static long ROUTE_UPDATE_INTERVAL = 30000;    // 30 seconds
    private DatagramSocket socket;
    private Node router;
    private HashMap<String, Integer> nodeSequence;

    public Lsr(String routerID, int port) throws IOException {
        this.socket = new DatagramSocket(port);
        this.router = new Node(routerID, port);
        this.nodeSequence = new HashMap<>();
    }

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
        network.addNode(currNode);

        new Lsr(routerID, port);

        msg = br.readLine();
        while ((msg = br.readLine()) != null) {
            splitMsg = msg.split(" ");
            routerID = splitMsg[0];
            Double cost = Double.valueOf(splitMsg[1]);
            port = Integer.valueOf(splitMsg[2]);

            Node neighbourNode = new Node(routerID, port);
            network.addNode(neighbourNode);
            network.makeEdge(neighbourNode, currNode, cost);
        }
    }

 /*   void send(Node router) throws IOException {
        InetAddress address = InetAddress.getByName("localhost");
        int sequence = 0;

        while (true) {
            *//* format pkt data *//*

            // router/seqNum/router cost/router cost/ etc
            String message = router.toString() + "/" + sequence + "/";

            for (Map.Entry<Node, Double> m : router.getNeighbours().entrySet()) {
                Node n = m.getKey();
                message += n.toString() + " " + n.getPort() + " " + m.getValue() + "/";
            }
            message += "/\r\n";

            byte[] msg = message.getBytes();
            DatagramPacket packet = new DatagramPacket(msg, msg.length, address, 1555);
            socket.send(packet);
        }
    }

    void receive(Network network) throws IOException {
        while (true) {
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
            socket.receive(packet);

            *//* process pkt and add to network *//*
            String msg = new String(packet.getData(), 0, packet.getLength());
            String[] splitMsg = msg.split("/");

            String node = splitMsg[0];
            int sequence = Integer.valueOf(splitMsg[1]);

            // received unchanged packet
            if (nodeSequence.containsKey(node)) {
                if (nodeSequence.get(node) <= sequence) continue;
            } else {
                nodeSequence.put(node, sequence);
            }

            for (int i = 2; i < splitMsg.length - 1; ++i) {
                String edges = splitMsg[i];
                String[] edgesInfo = edges.split(" ");
                String routerID = edgesInfo[0];
                int port = Integer.valueOf(edgesInfo[1]);
                double cost = Double.valueOf(edgesInfo[2]);
                Node neighbour = new Node(routerID, port);
                network.addNode(neighbour);
                network.makeEdge(neighbour, this.router, cost);
            }
        }
    }*/
}
