package indi.hiro.pagoda;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Hiro on 2019/4/5.
 */
public abstract class DatabaseReader {

    public static final String KEY_TIME_FORMAT = "timeFormat";
    public static final String KEY_ENTRY_PER_PAGE = "entryPerPage";
    public static final String KEY_PAGE = "page";

    static final SimpleDateFormat SDF_YEAR = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static final SimpleDateFormat SDF_MONTH = new SimpleDateFormat("MM/dd HH:mm:ss");
    static final SimpleDateFormat SDF_DATE = new SimpleDateFormat("dd HH:mm:ss");
    static final SimpleDateFormat SDF_TIME = new SimpleDateFormat("HH:mm:ss");
    static final SimpleDateFormat SDF_ZH_YEAR = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
    static final SimpleDateFormat SDF_ZH_MONTH = new SimpleDateFormat("MM月dd日 HH:mm:ss");
    static final SimpleDateFormat SDF_ZH_DATE = new SimpleDateFormat("dd日 HH:mm:ss");
    static final SimpleDateFormat SDF_ZH_DATE_ONLY = new SimpleDateFormat("MM月dd日");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {}
    }

    final String jspName;

    final Calendar now = Calendar.getInstance();
    final Calendar cal = Calendar.getInstance();
    final Date date = new Date();
    final String username;
    final String password;
    final int timeFormat;
    final int entryPerPage;
    final int page;

    public DatabaseReader(HttpSession session, HttpServletRequest request, String jspName) {
        this.jspName = jspName;
        username = readUserNameFromSession(session);
        password = readPasswordFromSession(session);
        timeFormat = readTimeFormatFromSession(session);
        entryPerPage = readEntryPerPageFromSession(session);
        page = readPageFromRequest(request);
    }

    private String readUserNameFromSession(HttpSession session) {
        Object username = session.getAttribute(SardineLogin.KEY_USER_NAME);
        if (username != null && !("").equals(username)) {
            return username.toString();
        } else {
            return null;
        }
    }

    private String readPasswordFromSession(HttpSession session) {
        Object password = session.getAttribute(SardineLogin.KEY_PASSWORD);
        if (password != null && !("").equals(password)) {
            return password.toString();
        } else {
            return null;
        }
    }

    private int readTimeFormatFromSession(HttpSession session) {
        Object oTimeFormat = session.getAttribute(KEY_TIME_FORMAT);
        if (oTimeFormat instanceof Integer) {
            return (Integer) oTimeFormat;
        } else {
            return 0;
        }
    }

    private int readEntryPerPageFromSession(HttpSession session) {
        Object oResultPerPage = session.getAttribute(KEY_ENTRY_PER_PAGE);
        if (oResultPerPage instanceof Integer) {
            int value = (Integer) oResultPerPage;
            if (2 <= value && value <= 100) {
                return value;
            }
        }
        return 20;
    }

    private int readPageFromRequest(HttpServletRequest request) {
        int page = 0;
        try {
            page = Integer.parseInt(request.getParameter(KEY_PAGE));
            if (page < 0) {
                page = 0;
            }
        } catch (NumberFormatException ignored) {}
        return page;
    }

    public Connection getConnection(JspWriter out) throws IOException {
        if (username == null || password == null) {
            out.println("<p>您还没有登录</p>");
            return null;
        }
        try {
            return DriverManager.getConnection(SardineLogin.CONNECTION_URL, username, password);
        } catch (SQLException e) {
            if (e instanceof SQLRecoverableException) {
                out.println("<p>数据库连接错误</p>");
            } else if (e.getMessage().startsWith("Access denied for user")) {
                out.println("<p>用户名或密码错误</p>");
            } else {
                out.println("<p>数据库系统未知错误</p>");
            }
            return null;
        }
    }

    public void printTableHead(JspWriter out, String[] heads) throws IOException {
        out.print("<tr>");
        for (String s : heads) {
            out.print("<th align='left'>");
            out.print(s);
            out.print("</th>");
        }
        out.println("</tr>");
    }

    public void printPageTabs(Statement statement, String sql, JspWriter out) throws SQLException, IOException {
        ResultSet resultSet = statement.executeQuery(sql);
        resultSet.first();
        int nEntry = resultSet.getInt("COUNT(*)");
        resultSet.close();
        int nPage = (nEntry + entryPerPage - 1) / entryPerPage;
        out.println("<center>");
        if (nPage <= 0) {
            out.println("<p>没有数据记录</p>");
        } else {
            out.println("<p>共" + nEntry + "条记录，分" + nPage + "页</p>");
            out.println("<ul class='pagetab'>");
            if (page >= nPage) {
                if (nPage <= 10) {
                    for (int i = 0; i < nPage; i++) {
                        printPageTab(i, out);
                    }
                } else {
                    for (int i = 0; i < 4; i++) {
                        printPageTab(i, out);
                    }
                    printPageEllipsis(out);
                    for (int i = nPage - 4; i < nPage; i++) {
                        printPageTab(i, out);
                    }
                }
            } else {
                if (nPage <= 10) {
                    for (int i = 0; i < nPage; i++) {
                        if (i == page) {
                            printPageTabSelected(i, out);
                        } else {
                            printPageTab(i, out);
                        }
                    }
                } else {
                    if (page <= 6) {
                        for (int i = 0; i < page; i++) {
                            printPageTab(i, out);
                        }
                    } else {
                        for (int i = 0; i < 3; i++) {
                            printPageTab(i, out);
                        }
                        printPageEllipsis(out);
                        for (int i = page - 3; i < page; i++) {
                            printPageTab(i, out);
                        }
                    }
                    printPageTabSelected(page, out);
                    if (nPage <= page + 6) {
                        for (int i = page + 1; i < nPage; i++) {
                            printPageTab(i, out);
                        }
                    } else {
                        for (int i = page + 1; i < page + 3; i++) {
                            printPageTab(i, out);
                        }
                        printPageEllipsis(out);
                        for (int i = nPage - 3; i < nPage; i++) {
                            printPageTab(i, out);
                        }
                    }
                }
            }
            out.println("</ul>");
        }
        out.println("</center>");
    }

    private void printPageTab(int index, JspWriter out) throws IOException {
        out.print("<a href='" + jspName + "?" + KEY_PAGE + "=" + index + "'><li>" + (index + 1) + "</li></a>");
    }

    private void printPageEllipsis(JspWriter out) throws IOException {
        out.print("<li class='dull'>...</li>");
    }

    private void printPageTabSelected(int index, JspWriter out) throws IOException {
        out.print("<li class='active'>" + (index + 1) + "</li>");
    }

    public String timeToString(long timeMillis) {
        date.setTime(timeMillis);
        if ((0x0100 & timeFormat) != 0) {
            cal.setTimeInMillis(timeMillis);
            if (now.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) && now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                return "今天 " + SDF_TIME.format(date);
            }
            cal.setTimeInMillis(timeMillis - 24 * 60 * 60 * 1000);
            if (now.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) && now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                return "昨天 " + SDF_TIME.format(date);
            }
            cal.setTimeInMillis(timeMillis - 2 * 24 * 60 * 60 * 1000);
            if (now.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) && now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                return "前天 " + SDF_TIME.format(date);
            }
        }
        switch (0xFF & timeFormat) {
            default:
                return SDF_YEAR.format(date);
            case 1:
                return SDF_MONTH.format(date);
            case 2:
                cal.setTimeInMillis(timeMillis);
                if (now.get(Calendar.YEAR) != cal.get(Calendar.YEAR)) {
                    return SDF_YEAR.format(date);
                } else {
                    return SDF_MONTH.format(date);
                }
            case 3:
                cal.setTimeInMillis(timeMillis);
                if (now.get(Calendar.YEAR) != cal.get(Calendar.YEAR)) {
                    return SDF_YEAR.format(date);
                } else if (now.get(Calendar.MONTH) != cal.get(Calendar.MONTH)) {
                    return SDF_MONTH.format(date);
                } else {
                    return SDF_DATE.format(date);
                }
            case 4:
                return SDF_ZH_YEAR.format(date);
            case 5:
                return SDF_ZH_MONTH.format(date);
            case 6:
                cal.setTimeInMillis(timeMillis);
                if (now.get(Calendar.YEAR) != cal.get(Calendar.YEAR)) {
                    return SDF_ZH_YEAR.format(date);
                } else {
                    return SDF_ZH_MONTH.format(date);
                }
            case 7:
                cal.setTimeInMillis(timeMillis);
                if (now.get(Calendar.YEAR) != cal.get(Calendar.YEAR)) {
                    return SDF_ZH_YEAR.format(date);
                } else if (now.get(Calendar.MONTH) != cal.get(Calendar.MONTH)) {
                    return SDF_ZH_MONTH.format(date);
                } else {
                    return SDF_ZH_DATE.format(date);
                }
        }
    }

    public String dateToString(long timeMillis) {
        date.setTime(timeMillis);
        return SDF_ZH_DATE_ONLY.format(date);
    }

    public String timeIntervalToString(long timeMillis) {
        int t = Math.abs((int) timeMillis);
        if (t < 1000) {
            return t + "毫秒";
        } else if (t < 10 * 1000) {
            return (t / 1000) + "." + (t % 1000 / 100) + "秒";
        } else if (t < 60 * 1000) {
            return (t / 1000) + "秒";
        } else if (t < 60 * 60 * 1000) {
            return (t / (60 * 1000)) + "分" + (t % (60 * 1000) / 1000) + "秒";
        } else if (t < 24 * 60 * 60 * 1000) {
            return (t / (60 * 60 * 1000)) + "小时" + (t % (60 * 60 * 1000) / (60 * 1000)) + "分" + (t % (60 * 1000) / 1000) + "秒";
        } else {
            return (t / (24 * 60 * 60 * 1000)) + "天" + (t % (24 * 60 * 60 * 1000) / (60 * 60 * 1000)) + "小时" + (t % (60 * 60 * 1000) / (60 * 1000)) + "分";
        }
    }

    public String srcAddrToString(int a) {
        if (a == 0) {
            return "&nbsp;";
        }
        String s = addrToString(a);
        return "<a href='http://ip.tool.chinaz.com/" + s + "' target='_blank'>" + s + "</a>";
    }

    public String addrToString(int a) {
        if (a == 0) {
            return "&nbsp;";
        }
        return (0xFF & (a >> 24)) + "." + (0xFF & (a >> 16)) + "." + (0xFF & (a >> 8)) + "." + (0xFF & a);
    }

    public String portToString(int p) {
        String s = TcpPorts.portDescription(p);
        if (s != null) {
            return "<span title='" + s + "'>" + p + "</span>";
        } else {
            return Integer.toString(p);
        }
    }

    public String dataAmountToString(int x) {
        if (x < 1024) {
            return x + "&nbsp;B";
        } else if (x < 32 * 1024) {
            return (x / 1024) + "." + (x % 1024 * 10 / 1024) + "&nbsp;KB";
        } else if (x < 1024 * 1024) {
            return (x / 1024) + "&nbsp;KB";
        } else if (x < 32 * 1024 * 1024) {
            return (x / (1024 * 1024)) + "." + (x % (1024 * 1024) * 10 / (1024 * 1024)) + "&nbsp;MB";
        } else if (x < 1024 * 1024 * 1024) {
            return (x / (1024 * 1024)) + "&nbsp;MB";
        } else {
            return (x / (1024 * 1024 * 1024)) + "." + (x % (1024 * 1024 * 1024) * 10 / (1024 * 1024 * 1024)) + "&nbsp;GB";
        }
    }
}
