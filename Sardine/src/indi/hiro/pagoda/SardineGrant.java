package indi.hiro.pagoda;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Hiro on 2019/4/5.
 */
@SuppressWarnings("unused")
public class SardineGrant {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {}
    }

    public static final Pattern GRANT_PATTERN = Pattern.compile("^GRANT (.*) ON ([^ ]*)\\.([^ ]*) .*$", Pattern.CASE_INSENSITIVE);
    public static final Pattern GRANT_SPLIT = Pattern.compile(" *, *");

    public static void printGrants(HttpSession session, JspWriter out) throws IOException {
        Object username = session.getAttribute(SardineLogin.KEY_USER_NAME);
        Object password = session.getAttribute(SardineLogin.KEY_PASSWORD);
        if (username == null || password == null || ("").equals(username)) {
            out.println("您还没有登录");
            return;
        }
        Connection connection;
        try {
            connection = DriverManager.getConnection(SardineLogin.CONNECTION_URL, username.toString(), password.toString());
        } catch (SQLException e) {
            out.println("登录失败");
            return;
        }
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SHOW GRANTS FOR " + username + ";");
            out.println("<p>" + username + "的权限：</p>");
            while (resultSet.next()) {
                grantDecode(resultSet.getString(1), out);
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            out.println("<p>" + e + "</p>");
        }
    }

    private static void grantDecode(String resultString, JspWriter out) throws IOException {
        //System.out.println(resultString);
        Matcher matcher = GRANT_PATTERN.matcher(resultString);
        if (matcher.matches()) {
            out.println("<p>在" + databaseName(matcher.group(2)) + "中" + tableName(matcher.group(3)) + "上拥有权限：</p>");
            String[] items = GRANT_SPLIT.split(matcher.group(1));
            for (String s : items) {
                String ts = translateGrantName(s);
                if (ts != null) {
                    out.println("<p>" + ts + "（" + s + "）</p>");
                } else {
                    out.println("<p>" + s + "</p>");
                }
            }
        } else {
            out.println("<p>" + resultString + "</p>");
        }
    }

    private static String databaseName(String str) {
        if ("*".equals(str)) {
            return "所有数据库";
        } else {
            return "数据库" + str;
        }
    }

    private static String tableName(String str) {
        if ("*".equals(str)) {
            return "所有表";
        } else {
            return "表" + str;
        }
    }

    private static String translateGrantName(String str) {
        switch (str.toUpperCase()) {
            case "SELECT":
                return "查询";
            case "INSERT":
                return "插入";
            case "UPDATE":
                return "更新记录";
            case "DELETE":
                return "删除记录";
            case "CREATE":
                return "创建表";
            case "DROP":
                return "删除表";
            case "CREATE USER":
                return "创建用户";
            default:
                return null;
        }
    }
}
