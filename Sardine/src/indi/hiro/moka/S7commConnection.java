package indi.hiro.moka;

import java.io.EOFException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Hiro on 2019/4/10.
 */
public class S7commConnection extends SocketWrap {

    static final byte[] ISO_CONNECT = {
            // TPKT (RFC1006 Header)
            (byte)0x03, // RFC 1006 ID (3)
            (byte)0x00, // Reserved, always 0
            (byte)0x00, // High part of packet length (entire frame, payload and TPDU included)
            (byte)0x16, // Low part of packet length (entire frame, payload and TPDU included)
            // COTP (ISO 8073 Header)
            (byte)0x11, // PDU Size Length
            (byte)0xE0, // CR - Connection Request ID
            (byte)0x00, // Dst Reference HI
            (byte)0x00, // Dst Reference LO
            (byte)0x00, // Src Reference HI
            (byte)0x01, // Src Reference LO
            (byte)0x00, // Class + Options Flags
            (byte)0xC0, // PDU Max Length ID
            (byte)0x01, // PDU Max Length HI
            (byte)0x0A, // PDU Max Length LO
            (byte)0xC1, // Src TSAP Identifier
            (byte)0x02, // Src TSAP Length (2 bytes)
            (byte)0x01, // Src TSAP HI (will be overwritten)
            (byte)0x00, // Src TSAP LO (will be overwritten)
            (byte)0xC2, // Dst TSAP Identifier
            (byte)0x02, // Dst TSAP Length (2 bytes)
            (byte)0x01, // Dst TSAP HI (will be overwritten)
            (byte)0x02  // Dst TSAP LO (will be overwritten)
    };

    // SZL First telegram request
    static final byte[] S7_SZL_FIRST = {
            (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x21,
            (byte)0x02, (byte)0xf0, (byte)0x80, (byte)0x32,
            (byte)0x07, (byte)0x00, (byte)0x00,
            (byte)0x05, (byte)0x00, // Sequence out
            (byte)0x00, (byte)0x08, (byte)0x00,
            (byte)0x08, (byte)0x00, (byte)0x01, (byte)0x12,
            (byte)0x04, (byte)0x11, (byte)0x44, (byte)0x01,
            (byte)0x00, (byte)0xff, (byte)0x09, (byte)0x00,
            (byte)0x04,
            (byte)0x00, (byte)0x00, // ID (29)
            (byte)0x00, (byte)0x00  // Index (31)
    };

    // SZL Next telegram request
    static final byte[] S7_SZL_NEXT = {
            (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x21,
            (byte)0x02, (byte)0xf0, (byte)0x80, (byte)0x32,
            (byte)0x07, (byte)0x00, (byte)0x00, (byte)0x06,
            (byte)0x00, (byte)0x00, (byte)0x0c, (byte)0x00,
            (byte)0x04, (byte)0x00, (byte)0x01, (byte)0x12,
            (byte)0x08, (byte)0x12, (byte)0x44, (byte)0x01,
            (byte)0x01, // Sequence
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x0a, (byte)0x00, (byte)0x00, (byte)0x00
    };

    static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    static String getCStringAt(byte[] array, int offset, int limit) {
        limit = Math.min(array.length, offset + limit);
        int ptr = offset;
        while (ptr < limit && array[ptr] != 0) ptr++;
        return new String(array, offset, ptr - offset);
    }

    public static String[] getDeviceInfo(String host, int port, int rack, int slot) throws Exception {
        ArrayList<String> rv = new ArrayList<>();
        S7commConnection connection = new S7commConnection(host, port);
        try {
            connection.isoConnect(rack, slot);
            byte[] szl = connection.readSzl(0x0011, 0x0000);
            rv.add("硬件标识符");
            rv.add(getCStringAt(szl, 14, 26));
            szl = connection.readSzl(0x001C, 0x0010);
            rv.add("应用名称"); // AS name
            rv.add(getCStringAt(szl, 14, 32));
            rv.add("模块名称"); // Module name
            rv.add(getCStringAt(szl, 14 + 34, 32));
            rv.add("生产商"); // Vendor copyright
            rv.add(getCStringAt(szl, 14 + 3 * 34, 32));
            rv.add("序列号"); // Serial number
            rv.add(getCStringAt(szl, 14 + 4 * 34, 32));
            rv.add("模块类型名称"); // Module type name
            rv.add(getCStringAt(szl, 14 + 5 * 34, 32));
            rv.add("记忆卡"); // MMC card
            rv.add(getCStringAt(szl, 14 + 6 * 34, 32));
        } finally {
            connection.close();
        }
        return rv.toArray(new String[0]);
    }

    public S7commConnection(String host, int port) throws Exception {
        super(host, port);
    }

    public byte[] receiveIso() throws Exception {
        byte[] header = receive(7);
        return receive(getShortAt(header, 2) - 7);
    }

    public void isoConnect(int rack, int slot) throws Exception {
        byte[] request = Arrays.copyOf(ISO_CONNECT, ISO_CONNECT.length);
        setShortAt(request, 16, 0x0100);
        setShortAt(request, 20, 0x0100 | 0x00E0 & (rack << 5) | 0x001F & slot);
        send(request);
        receiveIso();
    }

    public byte[] readSzl(int id, int index) throws Exception {
        byte[] rv;
        byte[] request = Arrays.copyOf(S7_SZL_FIRST, S7_SZL_FIRST.length);
        byte[] response;
        int sequence = 0;
        boolean continuing;
        setShortAt(request, 11, ++sequence);
        setShortAt(request, 29, id);
        setShortAt(request, 31, index);
        send(request);
        response = receiveIso();
        rv = Arrays.copyOfRange(response, 22, 22 + getShortAt(response, 8));
        continuing = getByteAt(response, 19) != 0x00;
        while (continuing) {
            request = Arrays.copyOf(S7_SZL_NEXT, S7_SZL_NEXT.length);
            setShortAt(request, 11, ++sequence);
            send(request);
            response = receiveIso();
            rv = concat(rv, Arrays.copyOfRange(response, 22, 22 + getShortAt(response, 8)));
            continuing = getByteAt(response, 19) != 0x00;
        }
        return rv;
    }
}
