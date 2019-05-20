package indi.hiro.pagoda;

/**
 * Created by Hiro on 2019/4/4.
 */
public class CodeC {

    public static void writeByte(StringBuilder sb, byte b) {
        sb.append("0x");
        int hx = 0xF & (b >> 4);
        sb.append((char) (hx < 0xA ? '0' + hx : 'A' - 0xA + hx));
        hx = 0xF & b;
        sb.append((char) (hx < 0xA ? '0' + hx : 'A' - 0xA + hx));
        sb.append(",");
    }

    public static void writeBytes(StringBuilder sb, byte[] bb, String indent, int nCol) {
        for (int i = 0; i < bb.length; i++) {
            if (i % nCol == 0) {
                sb.append("\r\n").append(indent);
            }
            writeByte(sb, bb[i]);
        }
        sb.deleteCharAt(sb.length() - 1);
    }

    public static void writeByteArray(StringBuilder sb, byte[] bb, String name) {
        sb.append("  byte ").append(name).append("[").append(bb.length).append("] = {");
        writeBytes(sb, bb, "    ", 19);
        sb.append("\r\n  };");
    }
}
