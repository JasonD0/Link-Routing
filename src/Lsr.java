import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramSocket;

public class Lsr {
    public static void main(String[] args) throws IOException {
        Network network = new Network();
        String configFile = args[0];

        BufferedReader br = new BufferedReader(new FileReader(new File(configFile)));
        String msg = br.readLine();
        String[] splitMsg = msg.split(" ");
        String routerID = splitMsg[0];
        int port = Integer.valueOf(splitMsg[1]);
        Node currNode = new Node(routerID, port);

        connectRouter(network, currNode, br);

        DatagramSocket socket = new DatagramSocket(port);
        Receiver r = new Receiver(socket, network, currNode);
        new Thread(r).start();

        Sender s = new Sender(socket, network, currNode);
        new Thread(s).start();

        Dijkstra d = new Dijkstra(network, currNode);
        new Thread(d).start();
    }

    private static void connectRouter(Network network, Node router, BufferedReader br) throws IOException {
        network.addNode(router);

        String msg = br.readLine(); // ignore number of neighbours
        while ((msg = br.readLine()) != null) {
            String[] splitMsg = msg.split(" ");
            String routerID = splitMsg[0];
            Double cost = Double.valueOf(splitMsg[1]);
            int port = Integer.valueOf(splitMsg[2]);

            Node neighbourNode = new Node(routerID, port);
            network.addNode(neighbourNode);
            network.makeEdge(neighbourNode, router, cost);
        }
    }
}

 /* TEST
Node A = new Node("A", 1);
Node B = new Node("B", 2);
Node C = new Node("C", 3);
Node D = new Node("D", 4);
Node E = new Node("E", 5);
network.addNode(A);
network.addNode(B);
network.addNode(C);
network.addNode(D);
network.addNode(E);
network.makeEdge(A, B, 3.0);
network.makeEdge(A, C, 1.0);
network.makeEdge(B, C, 7.0);
network.makeEdge(B, D, 5.0);
network.makeEdge(B, E, 1.0);
network.makeEdge(C, D, 2.0);
network.makeEdge(D, E, 7.0);*/