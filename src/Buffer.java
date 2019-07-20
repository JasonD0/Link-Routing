import java.util.Vector;

public class Buffer {
    private Vector<String> packets;
    private int i;

    public Buffer() {
        this.packets = new Vector<>();
        this.i = 0;
    }

    public Vector<String> getPackets() {
        return new Vector<>(this.packets);
    }

    public void addPacket(String packet) {
        this.packets.add(packet);
    }

    public String getPacket() {
        String pkt = packets.get(i);
        i++;
        i = i % packets.size();
        return pkt;
    }
}
