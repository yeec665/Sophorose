package indi.hiro.pagoda;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Hiro on 2019/4/3.
 */
public class SqlHelper implements Runnable {

    private String host = "localhost";
    private String database = "honeypot";
    private String username = "root";
    private String password = "123456";

    private Connection connection;
    private Statement statement;

    final ConcurrentLinkedQueue<String> sqlQueue = new ConcurrentLinkedQueue<>();
    final Object lock = new Object();

    public SqlHelper() {
        Thread thread = new Thread(this, "SqlConnection");
        thread.start();
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://" + host + ":3306/" + database + "?useSSL=true", username, password);
    }

    public void testConnect() {
        try {
            System.out.println("MySQL : Testing connection");
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            statement.execute("SHOW tables;");
            statement.close();
            connection.close();
            System.out.println("MySQL : Test connection successful");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void enqueue(String sql) {
        //System.out.println("SqlHelper.enqueue");
        if (sql != null) {
            sqlQueue.add(sql);
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    @Override
    public void run() {
        while (Terminal.SYSTEM_RUNNING) {
            try {
                synchronized (lock) {
                    lock.wait(30000);
                }
            } catch (InterruptedException ignored) {}
            workLoop();
        }
        closeConnection();
    }

    private void workLoop() {
        boolean firstLoop = true;
        while (true) {
            String sql = sqlQueue.poll();
            if (sql != null) {
                firstLoop = false;
                executeSql(sql);
                continue;
            } else if (firstLoop) {
                closeConnection();
            }
            break;
        }
    }

    private void executeSql(String sql) {
        //System.out.println("SqlHelper.executeSql");
        try {
            if (connection == null || connection.isClosed()) {
                connection = getConnection();
                statement = connection.createStatement();
            } else if (statement == null || statement.isClosed()) {
                statement = connection.createStatement();
            }
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        synchronized (this) {
            try {
                if (statement != null) {
                    statement.close();
                    statement = null;
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
