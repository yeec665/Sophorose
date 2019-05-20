package indi.hiro.pagoda;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;

/**
 * Created by Hiro on 2019/4/5.
 */
@SuppressWarnings("unused")
public class SardineLogin {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {}
    }

    public static final String CONNECTION_URL = "jdbc:mysql://rm-uf6n32jy1k5m0720wzo.mysql.rds.aliyuncs.com:3306/honeypot?useSSL=true";

    public static final String KEY_USER_NAME = "username";
    public static final String KEY_PASSWORD = "password";

    public static final int LC_LOGIN_SUCCESS = 0;
    public static final int LC_FIELD_REQUIRED = 1;
    public static final int LC_LINK_ERROR = 2;
    public static final int LC_ACCESS_DENIED = 3;
    public static final int LC_UNKNOWN_ERROR = 4;

    public static void collectPost(HttpServletRequest request, HttpSession session) {
        Object username = request.getParameter(KEY_USER_NAME);
        Object password = request.getParameter(KEY_PASSWORD);
        if (username != null && password != null) {
            session.setAttribute(KEY_USER_NAME, username);
            session.setAttribute(KEY_PASSWORD, password);
        }
    }

    public static int login(HttpSession session) {
        Object username = session.getAttribute(KEY_USER_NAME);
        Object password = session.getAttribute(KEY_PASSWORD);
        if (username != null && password != null && !("").equals(username)) {
            return databaseLogin(username.toString(), password.toString());
        } else {
            return LC_FIELD_REQUIRED;
        }
    }

    public static Connection getConnection(HttpSession session) throws SQLException {
        Object username = session.getAttribute(KEY_USER_NAME);
        Object password = session.getAttribute(KEY_PASSWORD);
        if (username != null && password != null && !("").equals(username)) {
            return DriverManager.getConnection(CONNECTION_URL, username.toString(), password.toString());
        } else {
            return null;
        }
    }

    public static int databaseLogin(String username, String password) {
        try {
            DriverManager.getConnection(CONNECTION_URL, username, password).close();
            return LC_LOGIN_SUCCESS;
        } catch (SQLException e) {
            if (e instanceof SQLRecoverableException) {
                return LC_LINK_ERROR;
            }
            if (e.getMessage().startsWith("Access denied for user")) {
                return LC_ACCESS_DENIED;
            }
            return LC_UNKNOWN_ERROR;
        }
    }
}
