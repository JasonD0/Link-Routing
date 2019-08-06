import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Buffer {
    private Vector<String> packets;
    private Map<String, String> LSA;
    private Vector<String> routers;
    private Vector<String> processing_packets;
    private boolean updated;

    public void removeRouter(String routerID) {
        for (Map.Entry<String, String> lsa : LSA.entrySet()) {
            String key = lsa.getKey();
            String value = lsa.getValue();

            LSA.put(key, value.replaceAll(routerID + " .*?/", ""));
        }
        LSA.remove(routerID);
        routers.remove(routerID);
    }

    public Buffer() {
        this.packets = new Vector<>();
        this.LSA = Collections.synchronizedMap(new HashMap<>());
        this.routers = new Vector<>();
        this.processing_packets = new Vector<>();
        this.updated = false;
    }

    public void addPacket(String packet, String routerID, boolean replace) {
        if (this.LSA.containsKey(routerID) && !replace) return;
        this.LSA.put(routerID, packet);
        this.routers.add(routerID);
        this.updated = true;
    }

    public void addPacket(String packet) {
        this.packets.add(packet);
        this.processing_packets.add(packet);
    }

    public void initLSA(String router, String packet) {
        this.LSA.put(router, packet);
        this.routers.add(router);
    }

    public String getPacket() {
        String packet = packets.get(0);
        packets.remove(0);
        return packet;
    }

    public String getProcessingPacket() {
        String packet = processing_packets.get(0);
        processing_packets.remove(0);
        return packet;
    }

    public String getPeriodicPacket() {
        String packet = "";
        for (String router : routers) {
            packet += LSA.get(router) + "-";
        }
        return packet;
    }

    public void doWait() {
        if (packets.size() > 0) return;
        try {
            synchronized(packets) {
                packets.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void doNotify() {
        synchronized(packets) {
            packets.notify();
        }
    }

    public void doProcessingWait() {
        if (processing_packets.size() > 0) return;
        try {
            synchronized(processing_packets) {
                processing_packets.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void doProcessingNotify() {
        synchronized(processing_packets) {
            processing_packets.notify();
        }
    }

    public boolean updated() {
        boolean u = this.updated;
        if (u) this.updated = false;
        return u;
    }
}
