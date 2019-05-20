package indi.hiro.pagoda;

import java.util.ArrayList;

/**
 * Created by Hiro on 2019/4/6.
 * SQL :
 * CREATE TABLE ids_statistics(id INT AUTO_INCREMENT PRIMARY KEY, srcAddr INT NOT NULL, startTime BIGINT NOT NULL, stopTime BIGINT NOT NULL, nPacket INT NOT NULL, nPort INT NOT NULL, netTraffic INT NOT NULL, portFlags INT NOT NULL, nSynOnly INT NOT NULL, nNull INT NOT NULL, nFinOnly INT NOT NULL, nSynAck INT NOT NULL, nUPF INT NOT NULL);
 */
public class RecognizedIntrusion {

    static final int[] COMMON_PORTS = {
            23, 79, 80, 102, 139, 443, 445, 500, 502, 1433, 1521, 3306, 3389, 8080, 8088
    };

    public static int portFlags(IntegerCounter ic) {
        int flags = 0;
        for (int i = 0; i < COMMON_PORTS.length; i++) {
            if (ic.count(COMMON_PORTS[i]) > 0) {
                flags |= 1 << i;
            }
        }
        return flags;
    }

    final ArrayList<PacketRecord> records = new ArrayList<>();

    final int srcAddr;

    boolean inserted, updated;

    public RecognizedIntrusion(int srcAddr) {
        this.srcAddr = srcAddr;
    }

    public boolean add(PacketRecord pr) {
        updated = false;
        return records.add(pr);
    }

    public boolean notEmpty() {
        return !records.isEmpty();
    }

    public long startTime() {
        return records.get(0).time;
    }

    public long stopTime() {
        return records.get(records.size() - 1).time;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public void intoDatabase(SqlHelper sqlHelper) {
        if (updated) {
            return;
        }
        updated = true;
        IntegerCounter ic = new IntegerCounter();
        int netTraffic = 0;
        int nSynOnly = 0;
        int nNull = 0;
        int nFinOnly = 0;
        int nSA = 0;
        int nUPF = 0;
        for (PacketRecord pr : records) {
            ic.addOne(pr.dstPort);
            netTraffic += pr.len;
            if (pr.tcpFlags == PacketRecord.TCP_SYN) {
                nSynOnly++;
            }
            if (pr.tcpFlags == 0) {
                nNull++;
            }
            if (pr.tcpFlags == PacketRecord.TCP_FIN) {
                nFinOnly++;
            }
            if (pr.hasFlags(PacketRecord.TCP_SYN | PacketRecord.TCP_ACK)) {
                nSA++;
            }
            if (pr.hasFlags(PacketRecord.TCP_URG | PacketRecord.TCP_PSH | PacketRecord.TCP_FIN)) {
                nUPF++;
            }
        }
        StringBuilder sb = new StringBuilder();
        if (inserted) {
            sb.append("UPDATE ids_statistics SET ");
        } else {
            sb.append("INSERT INTO ids_statistics SET ");
            sb.append("srcAddr=\'").append(srcAddr).append("\',");
            sb.append("startTime=\'").append(startTime()).append("\',");
        }
        sb.append("stopTime=\'").append(stopTime()).append("\',");
        sb.append("nPacket=\'").append(records.size()).append("\',");
        sb.append("nPort=\'").append(ic.size()).append("\',");
        sb.append("netTraffic=\'").append(netTraffic).append("\',");
        sb.append("portFlags=\'").append(portFlags(ic)).append("\',");
        sb.append("nSynOnly=\'").append(nSynOnly).append("\',");
        sb.append("nNull=\'").append(nNull).append("\',");
        sb.append("nFinOnly=\'").append(nFinOnly).append("\',");
        sb.append("nSynAck=\'").append(nSA).append("\',");
        sb.append("nUPF=\'").append(nUPF).append("\'");
        if (inserted) {
            sb.append(" WHERE srcAddr=\'").append(srcAddr).append("\' AND startTime=\'").append(startTime()).append("\'");
        }
        inserted = true;
        sb.append(";");
        sqlHelper.enqueue(sb.toString());
    }
}
