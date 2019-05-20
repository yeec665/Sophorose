package indi.hiro.moka;

import java.util.HashMap;

/**
 * Created by Hiro on 2019/4/10.
 */
public class S7Demo {

    public static String byteArrayToString(byte[] array) {
        StringBuilder sb = new StringBuilder();
        int length = array.length;
        for (int i = 0; i < length; i++) {
            int x = array[i];
            int hx = 0xF & (x >> 4);
            sb.append((char) (hx < 0xA ? '0' + hx : 'A' - 0xA + hx));
            hx = 0xF & x;
            sb.append((char) (hx < 0xA ? '0' + hx : 'A' - 0xA + hx));
            if ((0xF & i) == 0xF) {
                sb.append('\n');
            } else {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    static String getCStringAt(byte[] array, int offset, int limit) {
        limit = Math.min(array.length, offset + limit);
        int ptr = offset;
        while (ptr < limit && array[ptr] != 0) ptr++;
        return new String(array, offset, ptr - offset);
    }

    public static HashMap<String, String> getDeviceInfo() throws Exception {
        HashMap<String, String> map = new HashMap<>();
        S7commConnection connection = new S7commConnection("47.102.149.116", 102);
        try {
            connection.isoConnect(0, 2);
            byte[] szl = connection.readSzl(0x001C, 0x0010);
            map.put("AS Name", getCStringAt(szl, 14, 32));
            map.put("Module Name", getCStringAt(szl, 14 + 34, 32));
            map.put("Vendor copyright", getCStringAt(szl, 14 + 3 * 34, 32));
            map.put("Serial number", getCStringAt(szl, 14 + 4 * 34, 32));
            map.put("Module type name", getCStringAt(szl, 14 + 5 * 34, 32));
            map.put("MMC card", getCStringAt(szl, 14 + 6 * 34, 32));
        } finally {
            connection.close();
        }
        return map;
    }

    public static void test() {
        try {
            System.out.println(getDeviceInfo());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
