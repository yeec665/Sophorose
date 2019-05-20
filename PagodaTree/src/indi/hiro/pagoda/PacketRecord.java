package indi.hiro.pagoda;

/**
 * Created by Hiro on 2019/4/6.
 *
 * SQL :
 * CREATE TABLE ids_captures(id INT AUTO_INCREMENT PRIMARY KEY, time BIGINT NOT NULL, len INT NOT NULL, srcAddr INT NOT NULL, dstAddr INT NOT NULL, srcPort INT NOT NULL, dstPort INT NOT NULL, tcpFlags INT NOT NULL);
 */
public class PacketRecord {

    public static final int TCP_ALL = 0x3F;
    public static final int TCP_URG = 0x20;
    public static final int TCP_ACK = 0x10;
    public static final int TCP_PSH = 0x08;
    public static final int TCP_RST = 0x04;
    public static final int TCP_SYN = 0x02;
    public static final int TCP_FIN = 0x01;

    final long time;
    final int len;
    final int srcAddr;
    final int dstAddr;
    final int srcPort;
    final int dstPort;
    final int tcpFlags;

    public PacketRecord(long sec, int usec, int len, long srcAddr, long dstAddr, int srcPort, int dstPort, int flags) {
        this.time = sec * 1000L + usec / 1000L;
        this.len = len;
        this.srcAddr = (int) srcAddr;
        this.dstAddr = (int) dstAddr;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.tcpFlags = TCP_ALL & flags;
    }

    public boolean hasFlags(int flag) {
        return (tcpFlags | ~flag) == 0xFFFFFFFF;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public void intoDatabase(SqlHelper sqlHelper) {
        if (dstPort != 102 && dstPort != 502) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ids_captures SET time=\'").append(time);
        sb.append("\',len=\'").append(len);
        sb.append("\',srcAddr=\'").append(srcAddr);
        sb.append("\',dstAddr=\'").append(dstAddr);
        sb.append("\',srcPort=\'").append(srcPort);
        sb.append("\',dstPort=\'").append(dstPort);
        sb.append("\',tcpFlags=\'").append(tcpFlags);
        sb.append("\';");
        sqlHelper.enqueue(sb.toString());
    }
}
