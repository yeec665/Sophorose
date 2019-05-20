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
 * CREATE TABLE ids_statistics(id INT AUTO_INCREMENT PRIMARY KEY, srcAddr INT NOT NULL, startTime BIGINT NOT NULL, stopTime BIGINT NOT NULL, nPacket INT NOT NULL, nPort INT NOT NULL, netTraffic INT NOT NULL, portFlags INT NOT NULL, nSynOnly INT NOT NULL, nNull INT NOT NULL, nFinOnly INT NOT NULL, nSynAck INT NOT NULL, nUPF INT NOT NULL);
 */
@SuppressWarnings("unused")
public class IdsStatisticsReader extends DatabaseReader {

    private static final String[] TABLE_HEADS = {
            "源IP地址", null,
            "开始时间", null,
            "持续长度", null,
            "包数量", "源IP地址发送到主机的TCP包数量",
            "端口数量", "源IP地址访问的端口数量",
            "网络流量", "源IP地址发送到主机的所有TCP包总大小（包括IPv4头）",
            "访问端口", "源IP地址访问的常见端口",
            "SYN", "仅有SYN（同步）标志位的包数量",
            "NULL", "没有任何标志位的包数量",
            "FIN", "仅有FIN（结束）标志位的包数量",
            "S+A+", "有SYN（同步）和ACK（确认）标志位的包数量",
            "U+P+F+",  "有URG（紧急）PSH（数据）FIN（结束）标志位的包数量",
    };

    static final int[] COMMON_PORTS = {
            23, 79, 80, 102, 139, 443, 445, 500, 502, 1433, 1521, 3306, 3389, 8080, 8088
    };

    int iSrcAddr;
    int iStartTime;
    int iStopTime;
    int inPacket;
    int inPort;
    int iNetTraffic;
    int iPortFlags;
    int inSynOnly;
    int inNull;
    int inFinOnly;
    int inSynAck;
    int inUPF;

    public IdsStatisticsReader(HttpSession session, HttpServletRequest request) {
        super(session, request, "ids_statistics.jsp");
    }

    public IdsStatisticsReader(String jspName, HttpSession session, HttpServletRequest request) {
        super(session, request, jspName);
    }

    public void printContent(JspWriter out) throws IOException {
        Connection connection = getConnection(out);
        if (connection != null) {
            try {
                Statement statement = connection.createStatement();
                printTable(statement, out);
                printPageTabs(statement, counterSQL(), out);
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    protected String counterSQL() {
        return "SELECT COUNT(*) FROM ids_statistics;";
    }

    public void printTable(Statement statement, JspWriter out) throws SQLException, IOException {
        ResultSet resultSet = statement.executeQuery(mainSQL());
        out.println("<table width='100%' border='1' frame='void' rules='rows'>");
        findColumns(resultSet);
        printTableHead(out, getTableHeads());
        while (resultSet.next()) {
            printTableRow(resultSet, out);
        }
        out.println("</table>");
        resultSet.close();
    }

    protected String[] getTableHeads() {
        return TABLE_HEADS;
    }

    protected String mainSQL() {
        return "SELECT * FROM ids_statistics ORDER BY id DESC LIMIT " + (entryPerPage * page) + "," + entryPerPage + ";";
    }

    public void findColumns(ResultSet resultSet) throws SQLException {
        iSrcAddr = resultSet.findColumn("srcAddr");
        iStartTime = resultSet.findColumn("startTime");
        iStopTime = resultSet.findColumn("stopTime");
        inPacket = resultSet.findColumn("nPacket");
        inPort = resultSet.findColumn("nPort");
        iNetTraffic = resultSet.findColumn("netTraffic");
        iPortFlags = resultSet.findColumn("portFlags");
        inSynOnly = resultSet.findColumn("nSynOnly");
        inNull = resultSet.findColumn("nNull");
        inFinOnly = resultSet.findColumn("nFinOnly");
        inSynAck = resultSet.findColumn("nSynAck");
        inUPF = resultSet.findColumn("nUPF");
    }

    @Override
    public void printTableHead(JspWriter out, String[] heads) throws IOException {
        out.print("<tr>");
        for (int i = 0, headsLength = heads.length; i < headsLength; i += 2) {
            String s1 = heads[i];
            String s2 = heads[i + 1];
            out.print("<th align='left'>");
            if (s2 != null) {
                out.print("<span title=\'" + s2 + "\'>" + s1 + "</span>");
            } else {
                out.print(s1);
            }
            out.print("</th>");
        }
        out.println("</tr>");
    }

    public void printTableRow(ResultSet resultSet, JspWriter out) throws SQLException, IOException {
        out.print("<tr>");
        out.print("<td>");out.print(srcAddrToString(resultSet.getInt(iSrcAddr)));out.print("</td>");
        printTimes(resultSet, out);
        out.print("<td>");out.print(resultSet.getInt(inPacket));out.print("</td>");
        out.print("<td>");out.print(resultSet.getInt(inPort));out.print("</td>");
        out.print("<td>");out.print(dataAmountToString(resultSet.getInt(iNetTraffic)));out.print("</td>");
        out.print("<td>");printVisitedPorts(resultSet.getInt(iPortFlags), out);out.print("</td>");
        out.print("<td>");out.print(portToString(resultSet.getInt(inSynOnly)));out.print("</td>");
        out.print("<td>");out.print(portToString(resultSet.getInt(inNull)));out.print("</td>");
        out.print("<td>");out.print(portToString(resultSet.getInt(inFinOnly)));out.print("</td>");
        out.print("<td>");out.print(portToString(resultSet.getInt(inSynAck)));out.print("</td>");
        out.print("<td>");out.print(portToString(resultSet.getInt(inUPF)));out.print("</td>");
        out.println("</tr>");
    }

    protected void printTimes(ResultSet resultSet, JspWriter out) throws SQLException, IOException {
        long startTime = resultSet.getLong(iStartTime);
        long stopTime = resultSet.getLong(iStopTime);
        long currentTime = System.currentTimeMillis();
        out.print("<td>");
        out.print("<span title=\'" + timeIntervalToString(currentTime - startTime) + "前开始\'>");
        out.print(timeToString(resultSet.getLong(iStartTime)));
        out.print("</span>");
        out.print("</td>");
        out.print("<td>");
        if (currentTime - stopTime < 60 * 1000) {
            out.print("<span title=\'仍在进行\'>");
        } else {
            out.print("<span title=\'" + timeIntervalToString(currentTime - stopTime) + "前结束\'>");
        }
        out.print(timeIntervalToString(stopTime - startTime));
        out.print("</span>");
        out.print("</td>");
    }

    private void printVisitedPorts(int portFlags, JspWriter out) throws IOException {
        if (portFlags == 0) {
            out.print("&nbsp;");
        }
        out.print("<font size='1'>");
        boolean firstEntry = true;
        for (int i = 0; i < COMMON_PORTS.length; i++) {
            if ((portFlags & (1 << i)) != 0) {
                if (firstEntry) {
                    firstEntry = false;
                } else {
                    out.print("/");
                }
                out.print(portToString(COMMON_PORTS[i]));
            }
        }
        out.print("</font>");
    }
}
