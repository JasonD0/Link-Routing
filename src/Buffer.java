import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class Buffer {
    private Vector<String> packets;
    private Vector<String> LSA;
    private Set<String> routersSeen;

    public Buffer() {
        this.packets = new Vector<>();
        this.LSA = new Vector<>();
        this.routersSeen = Collections.synchronizedSet(new HashSet<>());
    }

    public Vector<String> getPackets() {
        return new Vector<>(this.packets);
    }

    public void addPacket(String packet, String routerID) {
        if (this.routersSeen.contains(routerID)) return;
        this.LSA.add(packet);
        this.routersSeen.add(routerID);
    }

    public void addPacket(String packet) {
        this.packets.add(packet);
    }

    public void initLSA(String packet) {
        this.LSA.add(packet);
    }

    public String getPacket() {
        String packet = packets.get(0);
        packets.remove(0);
        return packet;
    }

    public String getPeriodicPacket() {
        String packet = "";
        for (String pkt : LSA) {
            packet += pkt + "-";
        }
        return packet;
    }

    public void removePacket(String packet) {
        this.LSA.remove(packet);
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
}
