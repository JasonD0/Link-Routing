import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.Map;

public class Lsr {
    public static void main(String[] args) throws IOException {
        String configFile = args[0];

        BufferedReader br = new BufferedReader(new FileReader(new File(configFile)));
        String msg = br.readLine();
        String[] splitMsg = msg.split(" ");
        String routerID = splitMsg[0];
        int port = Integer.valueOf(splitMsg[1]);
        Node currNode = new Node(routerID, port);

        Network network = new Network();

        Buffer buffer = new Buffer();

        config(network, currNode, br);

        DatagramSocket socket = new DatagramSocket(port);

        Receiver r = new Receiver(socket, network, buffer, currNode);
        new Thread(r).start();

        Sender s = new Sender(socket, buffer, currNode);
        new Thread(s).start();

        Dijkstra d = new Dijkstra(network, currNode);
        new Thread(d).start();
    }

    private static void config(Network network, Node router, BufferedReader br) throws IOException {
        network.addNode(router);

        String msg = br.readLine(); // ignore number of neighbours
        while ((msg = br.readLine()) != null) {
            String[] splitMsg = msg.split(" ");
            String routerID = splitMsg[0];
            Double cost = Double.valueOf(splitMsg[1]);
            int port = Integer.valueOf(splitMsg[2]);

            Node neighbourNode = new Node(routerID, port);
            network.addNode(neighbourNode);
            network.makeEdge(router, neighbourNode, cost);
        }
    }
}