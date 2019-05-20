package indi.hiro.pagoda;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Hiro on 2019/4/6.
 * SQL :
 * CREATE TABLE ids_captures(id INT AUTO_INCREMENT PRIMARY KEY, time BIGINT NOT NULL, len INT NOT NULL, srcAddr INT NOT NULL, dstAddr INT NOT NULL, srcPort INT NOT NULL, dstPort INT NOT NULL, tcpFlags INT NOT NULL);
 */
@SuppressWarnings("unused")
public class IdsCapturesReader extends DatabaseReader {

    public static final int TCP_ALL = 0x3F;
    public static final int TCP_URG = 0x20;
    public static final int TCP_ACK = 0x10;
    public static final int TCP_PSH = 0x08;
    public static final int TCP_RST = 0x04;
    public static final int TCP_SYN = 0x02;
    public static final int TCP_FIN = 0x01;

    public static final String[] TABLE_HEADS = {
            "时间", "长度", "源IP地址", "目的IP地址", "源端口", "目的端口", "TCP标志位"
    };

    int iTime;
    int iLen;
    int iSrcAddr;
    int iDstAddr;
    int iSrcPort;
    int iDstPort;
    int iTcpFlags;

    public IdsCapturesReader(HttpSession session, HttpServletRequest request) {
        super(session, request, "ids_captures.jsp");
    }

    public void printContent(JspWriter out) throws IOException {
        Connection connection = getConnection(out);
        if (connection != null) {
            try {
                Statement statement = connection.createStatement();
                printTable(statement, out);
                printPageTabs(statement, "SELECT COUNT(*) FROM ids_captures;", out);
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void printTable(Statement statement, JspWriter out) throws SQLException, IOException {
        ResultSet resultSet = statement.executeQuery("SELECT * FROM ids_captures ORDER BY id DESC LIMIT " + (entryPerPage * page) + "," + entryPerPage + ";");
        out.println("<table width='100%' border='1' frame='void' rules='rows'>");
        findColumns(resultSet);
        printTableHead(out, TABLE_HEADS);
        while (resultSet.next()) {
            printTableRow(resultSet, out);
        }
        out.println("</table>");
        resultSet.close();
    }

    public void findColumns(ResultSet resultSet) throws SQLException {
        iTime = resultSet.findColumn("time");
        iLen = resultSet.findColumn("len");
        iSrcAddr = resultSet.findColumn("srcAddr");
        iDstAddr = resultSet.findColumn("dstAddr");
        iSrcPort = resultSet.findColumn("srcPort");
        iDstPort = resultSet.findColumn("dstPort");
        iTcpFlags = resultSet.findColumn("tcpFlags");
    }

    public void printTableRow(ResultSet resultSet, JspWriter out) throws SQLException, IOException {
        out.print("<tr>");
        out.print("<td>");out.print(timeToString(resultSet.getLong(iTime)));out.print("</td>");
        out.print("<td>");out.print(resultSet.getInt(iLen));out.print("</td>");
        out.print("<td>");out.print(srcAddrToString(resultSet.getInt(iSrcAddr)));out.print("</td>");
        out.print("<td>");out.print(addrToString(resultSet.getInt(iDstAddr)));out.print("</td>");
        out.print("<td>");out.print(portToString(resultSet.getInt(iSrcPort)));out.print("</td>");
        out.print("<td>");out.print(portToString(resultSet.getInt(iDstPort)));out.print("</td>");
        out.print("<td>");printTcpFlags(resultSet.getInt(iTcpFlags), out);out.print("</td>");
        out.println("</tr>");
    }

    private void printTcpFlags(int flags, JspWriter out) throws IOException {
        out.print("<font title='紧急标志位' color='");out.print((flags & TCP_URG) != 0 ? "black" : "white");out.print("'>URG</font>&nbsp;");
        out.print("<font title='确认标志位' color='");out.print((flags & TCP_ACK) != 0 ? "black" : "white");out.print("'>ACK</font>&nbsp;");
        out.print("<font title='数据标志位' color='");out.print((flags & TCP_PSH) != 0 ? "black" : "white");out.print("'>PSH</font>&nbsp;");
        out.print("<font title='连接重置标志位' color='");out.print((flags & TCP_RST) != 0 ? "black" : "white");out.print("'>RST</font>&nbsp;");
        out.print("<font title='同步标志位' color='");out.print((flags & TCP_SYN) != 0 ? "black" : "white");out.print("'>SYN</font>&nbsp;");
        out.print("<font title='结束标志位' color='");out.print((flags & TCP_FIN) != 0 ? "black" : "white");out.print("'>FIN</font>");
    }
}
