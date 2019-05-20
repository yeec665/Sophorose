package indi.hiro.pagoda;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Hiro on 2019/4/17.
 */
@SuppressWarnings("unused")
public class IdsAnalyzer extends IdsStatisticsReader {

    private static final String[] TABLE_HEADS = {
            "源IP地址", null,
            "开始时间", null,
            "持续长度", null,
            "规模", "源IP地址访问的端口数量",
            "分析结果", "分析算法见系统使用说明"
    };

    public IdsAnalyzer(HttpSession session, HttpServletRequest request) {
        super("ids_analysis.jsp", session, request);
    }

    @Override
    protected String counterSQL() {
        return "SELECT COUNT(*) FROM ids_statistics WHERE nPort >= 20;";
    }

    protected String mainSQL() {
        return "SELECT * FROM ids_statistics WHERE nPort >= 20 ORDER BY id DESC LIMIT " + (entryPerPage * page) + "," + entryPerPage + ";";
    }

    @Override
    protected String[] getTableHeads() {
        return TABLE_HEADS;
    }

    @Override
    public void printTableRow(ResultSet resultSet, JspWriter out) throws SQLException, IOException {
        out.print("<tr>");
        out.print("<td>");out.print(srcAddrToString(resultSet.getInt(iSrcAddr)));out.print("</td>");
        printTimes(resultSet, out);
        out.print("<td>");out.print(resultSet.getInt(inPort));out.print("</td>");
        out.print("<td>");out.print(analyze(resultSet));out.print("</td>");
        out.println("</tr>");
    }

    protected String analyze(ResultSet resultSet) throws SQLException {
        int nPacket = resultSet.getInt(inPacket);
        int nPort = resultSet.getInt(inPort);
        if (nPacket >= 16 * nPort) {
            return "不是端口扫描";
        }
        StringBuilder sb = new StringBuilder();
        if (resultSet.getInt(inSynOnly) * 4 >= nPort) {
            sb.append("SYN扫描");
        }
        if (resultSet.getInt(inNull) * 4 >= nPort) {
            if (sb.length() > 0) {
                sb.append("&nbsp;+&nbsp;");
            }
            sb.append("NULL扫描");
        }
        if (resultSet.getInt(inFinOnly) * 4 >= nPort) {
            if (sb.length() > 0) {
                sb.append("&nbsp;+&nbsp;");
            }
            sb.append("FIN扫描");
        }
        if (resultSet.getInt(inUPF) * 4 >= nPort) {
            if (sb.length() > 0) {
                sb.append("&nbsp;+&nbsp;");
            }
            sb.append("Xmas-Tree扫描");
        }
        if (sb.length() == 0) {
            sb.append("未知的扫描类型");
        }
        return sb.toString();
    }
}
