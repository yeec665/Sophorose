package indi.hiro.pagoda;

import indi.hiro.moka.S7commConnection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Hiro on 2019/4/10.
 */
@SuppressWarnings("unused")
public class HoneypotInfo {

    public static final String KEY_HOST = "host";
    public static final String KEY_PORT = "port";
    public static final String KEY_RACK = "rack";
    public static final String KEY_SLOT = "slot";

    final String host, port, rack, slot;

    public HoneypotInfo(HttpServletRequest request) {
        host = request.getParameter(KEY_HOST);
        port = request.getParameter(KEY_PORT);
        rack = request.getParameter(KEY_RACK);
        slot = request.getParameter(KEY_SLOT);
    }

    public String inputHost() {
        return host == null ? "127.0.0.1" : host;
    }

    public String inputPort() {
        return port == null ? "102" : port;
    }

    public String inputRack() {
        return rack == null ? "0" : rack;
    }

    public String inputSlot() {
        return slot == null ? "1" : slot;
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
            int rack = Integer.parseInt(this.rack);
            if (rack < 0 || rack >= 8) {
                throw new IllegalArgumentException("机架必须在&nbsp;0-7&nbsp;范围内");
            }
            int slot = Integer.parseInt(this.slot);
            if (slot < 0 || slot >= 8) {
                throw new IllegalArgumentException("插槽必须在&nbsp;0-31&nbsp;范围内");
            }
            printTable(S7commConnection.getDeviceInfo(host, port, rack, slot), out);
        } catch (NumberFormatException e1) {
            out.println("<p>输入格式错误</p>");
        } catch (IllegalArgumentException e2) {
            out.println("<p>" + e2.getMessage() + "</p>");
        } catch (Exception e3) {
            out.println("<p>连接错误</p>");
        }
    }

    private boolean emptyParameter() {
        return host == null || port == null || rack == null || slot == null;
    }

    private void printTable(String[] content, JspWriter out) throws IOException {
        out.println("<table width='100%' border='1' frame='void' rules='rows'>");
        for (int i = 0; i < content.length; i += 2) {
            out.print("<tr><td>");
            out.print(content[i]);
            out.print("</td><td>");
            out.print(content[i + 1]);
            out.print("</td></tr>");
        }
        out.println("</table>");
    }
}
