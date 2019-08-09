import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Buffer {
    private static List<String> packets;
    private static List<String> periodic_packets;
    private static Map<String, String> LSA;
    private static List<String> routers;    // keep order of insertion (make sure this router first)
    private static List<String> processing_packets;
    private static int failedCount;

    public Buffer() {
        packets = Collections.synchronizedList(new ArrayList<>());
        periodic_packets = Collections.synchronizedList(new ArrayList<>());
        LSA = Collections.synchronizedMap(new HashMap<>());
        routers = Collections.synchronizedList(new ArrayList<>());
        processing_packets = Collections.synchronizedList(new ArrayList<>());
        failedCount = 0;
    }

    private void addRouter(String router) {
        synchronized (routers) {
            if (routers.contains(router)) return;
            routers.add(router);
        }
    }

    public synchronized void initLSA(String router, String packet) {
        synchronized (LSA){
            LSA.put(router, packet);
        }
        synchronized (routers) {
            addRouter(router);
        }
        periodic_packets.add(packet);
    }

    public void addLSA(String packet, String routerID) {
        synchronized (LSA) {
            if (LSA.containsKey(routerID)) return;
            LSA.put(routerID, packet);
        }
        synchronized (routers) {
            addRouter(routerID);
        }
        System.out.println(routerID);
    }

    public void addPacket(String packet, boolean periodic) {
        if (periodic) {
            synchronized (periodic_packets) {
                periodic_packets.add(packet);
            }

        } else {
            synchronized (packets) {
                packets.add(packet);
            }
        }

        doNotify();
    }

    public String getPacket() {
        // prioritise forwarding packets received
        synchronized (packets) {
            if (packets.size() > 0) {
                String packet = packets.get(0);
                packets.remove(0);
                return packet;
            }
        }

        // get LSA
        synchronized (periodic_packets) {
            String packet = periodic_packets.get(0);
            periodic_packets.remove(0);
            return packet;
        }
    }

    public void removeRouter(String routerID) {
        synchronized (LSA) {
            LSA.remove(routerID);
            for (Map.Entry<String, String> lsa : LSA.entrySet()) {
                String key = lsa.getKey();
                String value = lsa.getValue();

                LSA.put(key, value.replaceAll(routerID + " .*?/", ""));
            }
        }

        synchronized (routers) {
            routers.remove(routerID);
        }

        synchronized (periodic_packets) {
            periodic_packets.clear();
            periodic_packets.add(getPeriodicPacket());
        }

        ++failedCount;
    }

    public int getReplacedCount() {
        return failedCount;
    }

    public String getPeriodicPacket() {
        String packet = "";
        Vector<String> routersCpy;
        Map<String, String> LSAcpy;
        synchronized (routers) {
            routersCpy = new Vector<>(routers);
        }
        synchronized (LSA) {
            LSAcpy = new HashMap<>(LSA);
        }
        for (String router : routersCpy) {
            packet += LSAcpy.get(router) + "-";
        }
        return packet;
    }

    public void doWait() {
        synchronized(packets) {
            synchronized (periodic_packets) {
                if (packets.size() > 0 || periodic_packets.size() > 0) return;
            }
            try {
                packets.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void doNotify() {
        synchronized(packets) {
            packets.notify();
        }
    }


    public void addProcessingPacket(String pkt) {
        synchronized (processing_packets) {
            processing_packets.add(pkt);
        }
        doProcessingNotify();
    }

    public String getProcessingPacket() {
        synchronized (processing_packets) {
            String packet = processing_packets.get(0);
            processing_packets.remove(0);
            return packet;
        }
    }

    public void doProcessingWait() {
        synchronized (processing_packets) {
            if (processing_packets.size() > 0) return;
            try {
                processing_packets.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void doProcessingNotify() {
        synchronized(processing_packets) {
            processing_packets.notify();
        }
    }
}
