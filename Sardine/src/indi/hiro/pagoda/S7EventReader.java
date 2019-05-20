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
 * Created by Hiro on 2019/4/5.
 * SQL :
 * CREATE TABLE s7_events(id INT AUTO_INCREMENT PRIMARY KEY, time INT NOT NULL, host INT NOT NULL, es INT NOT NULL, ec INT NOT NULL, erc SMALLINT NOT NULL, p1 SMALLINT NOT NULL, p2 SMALLINT NOT NULL, p3 SMALLINT NOT NULL, p4 SMALLINT NOT NULL);
 */
@SuppressWarnings("unused")
public class S7EventReader extends DatabaseReader {

    public static final String FILTER_ALL = "";
    public static final String FILTER_SERVER_START_STOP = " WHERE ec = " + 0x00000001 + " OR ec = " + 0x00000002;
    public static final String FILTER_CLIENT_SESSION = " WHERE ec = " + 0x00000008 + " OR ec = " + 0x00000010 + " OR ec = " + 0x00000080;
    public static final String FILTER_READ_INFO = " WHERE ec = " + 0x00100000 + " OR ec = " + 0x80000000;
    public static final String FILTER_ACCESS_DEEP = " WHERE ec = " + 0x00020000 + " OR ec = " + 0x00040000 + " OR ec = " + 0x00400000 + " OR ec = " + 0x00800000 + " OR ec = " + 0x01000000 + " OR ec = " + 0x40000000;

    public static final String[] TABLE_HEADS = {
            "时间", "主机", "客户端", "事件内容"
    };

    final String filter;

    int iTime;
    int iHost;
    int iSender;
    int iCode;
    int iReturnCode;
    int iParameter1;
    int iParameter2;
    int iParameter3;
    int iParameter4;

    public S7EventReader(HttpSession session, HttpServletRequest request, String jspName, String filter) {
        super(session, request, jspName);
        this.filter = filter;
    }

    public void printContent(JspWriter out) throws IOException {
        Connection connection = getConnection(out);
        if (connection != null) {
            try {
                Statement statement = connection.createStatement();
                printTable(statement, out);
                printPageTabs(statement, "SELECT COUNT(*) FROM s7_events" + filter + ";", out);
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void printTable(Statement statement, JspWriter out) throws SQLException, IOException {
        ResultSet resultSet = statement.executeQuery("SELECT * FROM s7_events" + filter + " ORDER BY id DESC LIMIT " + (entryPerPage * page) + "," + entryPerPage + ";");
        out.println("<table width='100%' frame='void' rules='rows'>");
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
        iHost = resultSet.findColumn("host");
        iSender = resultSet.findColumn("es");
        iCode = resultSet.findColumn("ec");
        iReturnCode = resultSet.findColumn("erc");
        iParameter1 = resultSet.findColumn("p1");
        iParameter2 = resultSet.findColumn("p2");
        iParameter3 = resultSet.findColumn("p3");
        iParameter4 = resultSet.findColumn("p4");
    }

    public void printTableRow(ResultSet resultSet, JspWriter out) throws SQLException, IOException {
        S7Event s7Event = new S7Event(resultSet, this);
        out.print("<tr>");
        out.print("<td>");out.print(timeToString(s7Event.getTimeMillis()));out.print("</td>");
        out.print("<td>");out.print(s7Event.host);out.print("</td>");
        out.print("<td>");out.print(srcAddrToString(s7Event.sender));out.print("</td>");
        out.print("<td>");out.print(s7Event.eventToHtml());out.print("</td>");
        out.println("</tr>");
    }
}
