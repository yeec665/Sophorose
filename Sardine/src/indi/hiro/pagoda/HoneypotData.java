package indi.hiro.pagoda;

import com.sun.org.apache.xpath.internal.operations.Mod;
import indi.hiro.moka.ModbusConnection;
import indi.hiro.moka.S7commConnection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;

/**
 * Created by Hiro on 2019/4/10.
 */
@SuppressWarnings("unused")
public class HoneypotData {

    public static final String KEY_HOST = "host";
    public static final String KEY_PORT = "port";
    public static final String KEY_FUNCTION = "function";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_LENGTH = "length";

    final String host, port, address, length, function;

    public HoneypotData(HttpServletRequest request) {
        host = request.getParameter(KEY_HOST);
        port = request.getParameter(KEY_PORT);
        address = request.getParameter(KEY_ADDRESS);
        length = request.getParameter(KEY_LENGTH);
        function = request.getParameter(KEY_FUNCTION);
    }

    public String inputHost() {
        return host == null ? "127.0.0.1" : host;
    }

    public String inputPort() {
        return port == null ? "502" : port;
    }

    public String inputAddress() {
        return address == null ? "0" : address;
    }

    public String inputLength() {
        return length == null ? "8" : length;
    }

    public String functionReadCoil() {
        return function != null && function.equals(Integer.toString(ModbusConnection.RB_FC_COILS)) ? " checked" : "";
    }

    public String functionReadDiscreteInputs() {
        return function != null && function.equals(Integer.toString(ModbusConnection.RB_FC_DISCRETE_INPUTS)) ? " checked" : "";
    }

    public String functionReadHoldingRegisters() {
        return function != null && function.equals(Integer.toString(ModbusConnection.RS_FC_HOLDING_REGISTERS)) ? " checked" : "";
    }

    public String functionReadInputRegisters() {
        return function != null && function.equals(Integer.toString(ModbusConnection.RS_FC_INPUT_REGISTERS)) ? " checked" : "";
    }

    public void printContent(JspWriter out) throws IOException {
        if (emptyParameter()) {
            return;
        }
        try {
            int port = Integer.parseInt(this.port);
            if (port < 0 || port >= 65536) {
                throw new IllegalArgumentException("端口必须在&nbsp;0-65535&nbsp;范围内");
            }
            int address = Integer.parseInt(this.address);
            int length = Integer.parseInt(this.length);
            int function = Integer.parseInt(this.function);
            switch (function) {
                case ModbusConnection.RB_FC_COILS:
                    printTable(ModbusConnection.readBits(host, port, function, address, length), "Q", address, out);
                    break;
                case ModbusConnection.RB_FC_DISCRETE_INPUTS:
                    printTable(ModbusConnection.readBits(host, port, function, address, length), "I", address, out);
                    break;
                case ModbusConnection.RS_FC_HOLDING_REGISTERS:
                    printTable(ModbusConnection.readShorts(host, port, function, address, length), "MW", address, out);
                    break;
                case ModbusConnection.RS_FC_INPUT_REGISTERS:
                    printTable(ModbusConnection.readShorts(host, port, function, address, length), "IW", address, out);
                    break;
            }
        } catch (NumberFormatException e1) {
            out.println("<p>输入格式错误</p>");
        } catch (IllegalArgumentException e2) {
            out.println("<p>" + e2.getMessage() + "</p>");
        } catch (Exception e3) {
            //e3.printStackTrace();
            out.println("<p>连接错误</p>");
        }
    }

    private boolean emptyParameter() {
        return host == null || port == null || address == null || length == null || function == null;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private void printTable(boolean[] content, String prefix, int address, JspWriter out) throws IOException {
        out.println("<table class='bitresult'><tr>");
        for (int i = 0; i < content.length; i++) {
            out.print("<td align='center'>");
            out.print(prefix);
            out.print((address + i) / 8);
            out.print(".");
            out.print(0x7 & (address + i));
            out.print("</td>");
        }
        out.println("</tr><tr>");
        for (int i = 0; i < content.length; i++) {
            if (content[i]) {
                out.print("<td class='vt'>1</td>");
            } else {
                out.print("<td class='vf'>0</td>");
            }
        }
        out.println("</tr></table>");
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private void printTable(int[] content, String prefix, int address, JspWriter out) throws IOException {
        out.println("<table class='bitresult'><tr>");
        for (int i = 0; i < content.length; i++) {
            out.print("<td>");
            out.print(prefix);
            out.print(address + i);
            out.print("</td>");
        }
        out.println("</tr><tr>");
        for (int i = 0; i < content.length; i++) {
            out.print("<td>");
            out.print(content[i]);
            out.print("</td>");
        }
        out.println("</tr></table>");
    }
}
