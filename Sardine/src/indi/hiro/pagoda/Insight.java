package indi.hiro.pagoda;


import indi.hiro.moka.S7Demo;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Hiro on 2019/4/3.
 */
public class Insight {

    static final int[] DATA_I = {
            0xFF,0x09,0x00,0x78,0x00,0x11,0x00,0x00,0x00,0x1C,0x00,0x04,0x00,0x01,0x36,0x45,0x53,0x37,0x20,
            0x33,0x31,0x37,0x2D,0x32,0x45,0x4B,0x31,0x34,0x2D,0x30,0x41,0x42,0x30,0x20,0x00,0xC0,0x00,0x04,
            0x00,0x01,0x00,0x06,0x36,0x45,0x53,0x37,0x20,0x33,0x31,0x37,0x2D,0x32,0x45,0x4B,0x31,0x34,0x2D,
            0x30,0x41,0x42,0x30,0x20,0x00,0xC0,0x00,0x04,0x00,0x01,0x00,0x07,0x20,0x20,0x20,0x20,0x20,0x20,
            0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x00,0xC0,0x56,0x03,0x02,
            0x06,0x00,0x81,0x42,0x6F,0x6F,0x74,0x20,0x4C,0x6F,0x61,0x64,0x65,0x72,0x20,0x20,0x20,0x20,0x20,
            0x20,0x20,0x20,0x20,0x00,0x00,0x41,0x20,0x09,0x09
    };

    private static byte[] toByteArray(int[] ii) {
        byte[] bb = new byte[ii.length];
        for (int i = 0; i < bb.length; i++) {
            bb[i] = (byte) ii[i];
        }
        return bb;
    }

    private static void indexOf(int x) {
        for (int i = 0; i < DATA_I.length; i++) {
            if (DATA_I[i] == x) {
                System.out.print("Index = " + i + "   ");
            }
        }
    }

    private static void toHex(StringBuilder sb, int x) {
        x &= 0xFF;
        if (x > 0x7F) {
            sb.append("(byte)");
        }
        sb.append("0x");
        int hx = 0xF & (x >> 4);
        sb.append((char) (hx < 0xA ? '0' + hx : 'A' - 0xA + hx));
        hx = 0xF & x;
        sb.append((char) (hx < 0xA ? '0' + hx : 'A' - 0xA + hx));
        sb.append(",");
    }

    private static void clear() {
        clear(1);
        clear(2);
        clear(4);
        clear(5);
        clear(6);
        clear(7);
    }

    private static void clear(int si) {
        int off = SZL001C.SPLIT_INDEX[si] + 1;
        for (int i = 0; i < 32; i++) {
            DATA_I[off + i] = 0x00;
        }
    }

    private static void split() {
        StringBuilder sb = new StringBuilder("    ");
        int[] splitIndex = SZL001C.SPLIT_INDEX;
        int splitPtr = 1;
        for (int i = 0; i < DATA_I.length; i++) {
            if (splitIndex[splitPtr] == i) {
                sb.append("\r\n    ");
                splitPtr++;
            }
            toHex(sb, DATA_I[i]);
        }
        System.out.println(sb);
    }

    private static void printSplit() {
        byte[] data = (new SZL001C()).getData();
        int[] splitIndex = SZL001C.SPLIT_INDEX;
        for (int i = 1; i < splitIndex.length; i++) {
            System.out.println(i - 1);
            System.out.println(new String(data, splitIndex[i - 1], splitIndex[i] - splitIndex[i - 1]));
        }
    }

    static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        System.out.println(new String(toByteArray(DATA_I)));
        //S7Demo.test();
    }
}
