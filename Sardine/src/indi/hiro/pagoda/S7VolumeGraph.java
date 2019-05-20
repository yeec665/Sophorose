package indi.hiro.pagoda;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Hiro on 2019/4/5.
 * SQL :
 * CREATE TABLE s7_events(id INT AUTO_INCREMENT PRIMARY KEY, time INT NOT NULL, host INT NOT NULL, es INT NOT NULL, ec INT NOT NULL, erc SMALLINT NOT NULL, p1 SMALLINT NOT NULL, p2 SMALLINT NOT NULL, p3 SMALLINT NOT NULL, p4 SMALLINT NOT NULL);
 */
@SuppressWarnings("unused")
public class S7VolumeGraph extends DatabaseReader {

    public static final int SVG_WIDTH = 860;
    public static final int SVG_HEIGHT = 380;
    public static final int N_DAYS = 12;
    public static final Color POINT = new Color(0x4BB6D4);
    public static final Color CURVE = new Color(0x3D80D4);
    public static final Color AXIS = new Color(0x9D9DA7);

    public S7VolumeGraph(HttpSession session, HttpServletRequest request, String jspName) {
        super(session, request, jspName);
    }

    public void printContent(JspWriter out) throws IOException {
        Connection connection = getConnection(out);
        if (connection != null) {
            try {
                Statement statement = connection.createStatement();
                printGraph(statement, out);
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void printGraph(Statement statement, JspWriter out) throws SQLException, IOException {
        out.println("<center><svg xmlns='http://www.w3.org/2000/svg' version='1.1' width='" + SVG_WIDTH + "' height='" + SVG_HEIGHT + "'>");
        ArrayList<VolumeRecord> volumeRecords = curvePoints(statement, out);
        out.println("<g stroke='#9D9DA7' stroke-width='2' stroke-linecap='round' fill='none'>");
        out.print("<line x1='0' y1='");out.print(0.9f * SVG_HEIGHT);out.print("' x2='");out.print(SVG_WIDTH);out.print("' y2='");out.print(0.9f * SVG_HEIGHT);out.print("'/>");
        out.println("</g>");
        out.println("<g fill='#4BB6D4'>");
        for (int i = 0; i < N_DAYS; i++) {
            VolumeRecord vr = volumeRecords.get(i);
            out.print("<circle cx='");out.print(vr.x);out.print("' cy='");out.print(vr.y);out.println("' r='8'/>");
        }
        out.println("</g>");
        out.print("<path d='");
        for (int i = 0; i < N_DAYS; i++) {
            VolumeRecord vr = volumeRecords.get(i);
            out.print(i == 0 ? "M " : " L ");out.print(vr.x);out.print(" ");out.print(vr.y);
        }
        out.println("' stroke='#3D80D4' stroke-width='4' stroke-linecap='round' stroke-linejoin='round' fill='none'/>");
        out.println("<g font-size='15' fill='black' stroke='none'>");
        cal.setTimeInMillis(now.getTimeInMillis());
        for (int i = 0; i < N_DAYS; i++) {
            VolumeRecord vr = volumeRecords.get(i);
            out.print("<text x='");out.print(vr.x - 5);out.print("' y='");out.print(vr.y - 12);out.print("'>");out.print(vr.countEs);out.println("</text>");
            out.print("<text x='");out.print(vr.x - 30);out.print("' y='");out.print(0.95f * SVG_HEIGHT);out.print("'>");out.print(dateToString(cal.getTimeInMillis()));out.println("</text>");
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        out.println("</g>");
        out.println("</svg><p>近" + N_DAYS + "天的访问量统计图</p></center>");
    }

    protected ArrayList<VolumeRecord> curvePoints(Statement statement, JspWriter out) throws SQLException {
        cal.setTimeInMillis(now.getTimeInMillis());
        //cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        ArrayList<VolumeRecord> rv = new ArrayList<>(N_DAYS);
        int maxEs = 1;
        for (int i = 0; i < N_DAYS; i++) {
            //int t2 = (int) (cal.getTimeInMillis() / 1000);
            //cal.add(Calendar.DAY_OF_MONTH, -1);
            int t1 = (int) (cal.getTimeInMillis() / 1000);
            int t2 = t1 + 24 * 60 * 60;
            ResultSet resultSet = statement.executeQuery("select count(distinct es) from s7_events where " + t1 + " <= time AND time < " + t2 + " AND es != 0;");
            resultSet.next();
            VolumeRecord vr = new VolumeRecord();
            vr.countEs = resultSet.getInt(1);
            vr.x = SVG_WIDTH * (N_DAYS - i - 0.5f) / N_DAYS;
            rv.add(vr);
            maxEs = Math.max(maxEs, vr.countEs);
            resultSet.close();
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        for (int i = 0; i < N_DAYS; i++) {
            VolumeRecord vr = rv.get(i);
            vr.y = SVG_HEIGHT * (0.9f - 0.8f * vr.countEs / maxEs);
        }
        return rv;
    }

    static class VolumeRecord extends Point2D.Float {
        public int countEs;
    }
}
