<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.io.*,java.util.*,java.sql.*,java.text.*"%>
<html>
<%!
    static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static final int PAGE_SIZE = 20;
    
    public void jspInit() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {}
    }
    
    public int parsePageParameter(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 1;
        }
    }
    
    public String timeDecode(long time) {
        return SDF.format(new java.util.Date(1000 * time));
    }
    
    public String senderDecode(int eventSender) {
        if (eventSender == 0) {
            return "";
        }
        return (0xFF & eventSender) + "." + (0xFF & (eventSender >> 8)) + "." + (0xFF & (eventSender >> 16)) + "." + (0xFF & (eventSender >> 24));
    }
    
    public String badPduDecode(int erc) {
        switch (erc) {
            case 0x0001:
                return "报文错误：报文段被拒绝";
            case 0x0002:
                return "报文错误：报文格式错误";
            case 0x0003:
                return "报文错误：报文太短";
            case 0x0004:
                return "报文错误：无法处理报文";
            case 0x0005:
                return "报文错误：功能没有实现";
            default:
                return "报文错误：未知的错误类型";
        }
    }
    
    public String badPduDecode(int erc, int p1) {
        return "接收报文，长度&nbsp;=&nbsp;" + p1 + "字节" + badPduDecode(erc);
    }
    
    public String dataDecode(int erc) {
        switch (erc) {
            case 0x0000:
                return "&nbsp;（成功）";
            case 0x0006:
                return "&nbsp;（发生异常）";
            case 0x0008:
                return "&nbsp;（超出范围）";
            case 0x0009:
                return "&nbsp;（数据过大）";
            case 0x000A:
                return "&nbsp;（非法传输长度）";
            case 0x000D:
                return "&nbsp;（长度失配）";
            default:
                return "&nbsp;（未知错误）";
        }
    }
    
    public String dataDecode(int erc, int p3, int p4) {
        return "，位置&nbsp;=&nbsp;" + p3 + "，长度&nbsp;=&nbsp;" + p4 + dataDecode(erc);
    }
	
	public String szlDecode1(int code) {
		switch (code) {
			case 0x0011:
				return "设备序列号";
			case 0x0013:
				return "用户存储器区域/工作存储器";
			case 0x0014:
				return "操作系统区域";
			case 0x0015:
				return "块类型";
			case 0x0F19:
			case 0x0F74:
				return "报头信息";
			case 0x001C:
				return "组件标识的所有记录，包括设备名称和CPU型号";
			case 0x0222:
				return "中断状态";
			case 0x0591:
				return "模块状态信息/主机模块的所有子模块";
			case 0x0C91:
				return "模块状态信息/中央机架中的模块或与集成的DP接口模块连接的模块";
			case 0x0D91:
				return "模块状态信息/指定机架中的所有模块";
			case 0x00A0:
				return "诊断缓冲区";
			case 0x00B1:
			case 0x00B2:
			case 0x00B3:
				return "模块诊断信息";
			default:
                return null;
		}
	}
	
	public String szlDecode2(int code) {
		switch (code) {
			case 0x01110001:
				return "CPU标识/CPU类型和版本号";
			case 0x01110006:
				return "CPU标识/基本硬件的标识";
			case 0x01110007:
				return "CPU标识/基本固化程序的标识";
			case 0x00120000:
				return "CPU特征/STEP7处理（所有记录）";
			case 0x00120100:
				return "CPU特征/CPU中的时间系统（所有记录）";
			case 0x00120300:
				return "CPU特征/STEP7操作设置（所有记录）";
			case 0x01120000:
				return "CPU特征/STEP7处理（特征记录）";
			case 0x01120100:
				return "CPU特征/CPU中的时间系统（特征记录）";
			case 0x01120300:
				return "CPU特征/STEP7操作设置（特征记录）";
			case 0x0F120000:
				return "CPU特征/STEP7处理（仅报头）";
			case 0x0F120100:
				return "CPU特征/CPU中的时间系统（仅报头）";
			case 0x0F120300:
				return "CPU特征/STEP7操作设置（仅报头）";
			case 0x00190001:
			case 0x00740001:
			case 0x01740001:
				return "SF-LED灯的状态";
			case 0x00190004:
			case 0x00740004:
			case 0x01740004:
				return "RUN-LED灯的状态";
			case 0x00190005:
			case 0x00740005:
			case 0x01740005:
				return "STOP-LED灯的状态";
			case 0x00190006:
			case 0x00740006:
			case 0x01740006:
				return "FORCE-LED灯的状态";
			case 0x011C0001:
				return "组件标识/站名称";
			case 0x011C0002:
				return "组件标识/模块名称";
			case 0x011C0003:
				return "组件标识/模块设备标识";
			case 0x011C0004:
				return "组件标识/版权说明";
			case 0x011C0005:
				return "组件标识/模块序列号";
			case 0x011C0008:
				return "组件标识/MMC序列号";
			case 0x011C000A:
				return "组件标识/OEM标识";
			case 0x01320004:
				return "通讯状态/连接的数目和类型";
			case 0x01320005:
				return "通讯状态/CPU保护层，键开关的位置，用户程序的版本标识和组态";
			case 0x01320006:
				return "通讯状态/诊断状态数据";
			case 0x01320008:
				return "通讯状态/PBK状态参数";
			case 0x0132000B:
				return "通讯状态/目标系统，修正因子，运行计时，日期时间";
			case 0x0132000C:
				return "通讯状态/运行计时";
			case 0x02320004:
				return "CPU保护层/CPU保护层和键开关的位置，用户程序的版本标识和硬件组态";
			case 0x00920000:
				return "中央组态中模块机架的预期状态";
			case 0x02920000:
				return "中央组态中模块机架的实际状态";
			case 0x06920000:
				return "中央组态中扩展设备的正常状态";
			case 0x00940000:
				return "在中央组态中模块机架的期望状态";
			case 0x02940000:
				return "在中央组态中模块机架的实际状态";
			case 0x06940000:
				return "在中央组态中机架的错误状态";
			case 0x07940000:
				return "在中央组态中机架的错误和/或维修状态";
			case 0x0F940000:
				return "模块机架的状态信息/仅标题信息";
			case 0x00B10000:
				return "模块诊断信息的数据记录";
			case 0x00B20000:
			case 0x00B30000:
				return "模块诊断信息的完整模块相关记录";
			default:
                return null;
		}
	}
    
    public String szlDecode(int erc, int p1, int p2) {
		String shown = "读取系统状态，子表&nbsp;=&nbsp;" + p1 + "，索引&nbsp;=&nbsp;" + p2 + (erc == 0 ? "&nbsp;（成功）" : "&nbsp;（失败）");
		String tooltip = szlDecode1(p1);
		if (tooltip == null) {
			tooltip = szlDecode2(0xFFFF0000 & (p1 << 16) | 0x0000FFFF & p2);
		}
		if (tooltip != null) {
			return "<div class=\"tooltip\">" + shown + "<span class=\"tooltiptext\">" + tooltip + "</span></div>";
		} else {
			return shown;
		}
    }
    
    public String blockName(int c) {
        switch (c) {
            case 0x38:
                return "组织块OB";
            case 0x41:
                return "数据块DB";
            case 0x42:
                return "系统数据块SDB";
            case 0x43:
                return "功能FC";
            case 0x44:
                return "系统功能SFC";
            case 0x45:
                return "功能块FB";
            case 0x46:
                return "系统功能块SFB";
            default:
                return "未知块";
        }
    }
	
	public String loadDecode(int erc, int p1, int p2) {
		return blockName(p1) + p2 + (erc == 0 ? "&nbsp;（成功）" : "&nbsp;（失败）");
	}
    
    public String blockDecode(int p1, int p2) {
        switch (p1) {
            case 0x0001:
                return "获取块列表";
            case 0x0002:
                return "枚举块开始（" + blockName(p2) + "）";
            case 0x0003:
                return "枚举块继续（" + blockName(p2) + "）";
            case 0x0004:
                return "获取块信息（" + blockName(p2) + "）";
            default:
                return "未知操作（" + blockName(p2) + "）";
        }
    }
    
    public String blockDecode(int erc, int p1, int p2) {
        return blockDecode(p1, p2) + (erc == 0 ? "&nbsp;（成功）" : "&nbsp;（失败）");
    }
    
    public String securityDecode(int p1) {
        switch (p1) {
            case 0x0001:
                return "安全功能：设置会话密码";
            case 0x0002:
                return "安全功能：清除会话密码";
            default:
                return "安全功能：未知";
        }
    }
    
    public String controlDecode(int p1) {
        switch (p1) {
            case 0:
                return "控制功能：未知";
            case 1:
                return "控制功能：冷启动";
            case 2:
                return "控制功能：暖启动";
            case 3:
                return "控制功能：停止运行";
            case 4:
                return "控制功能：内存压缩";
            case 5:
                return "控制功能：RAM复制到ROM";
            case 6:
                return "控制功能：替换块";
            default:
                return "控制功能：未定义";
        }
    }
	
	public String programmerDecode(int p1) {
		switch (p1) {
			case 0x0001:
				return "编程功能：标准请求";
			case 0x0002:
				return "编程功能：闪烁LED";
			case 0x0003:
				return "编程功能：请求诊断模式";
			case 0x0004:
				return "编程功能：读取诊断信息";
			case 0x0005:
				return "编程功能：退出诊断模式";
			default:
				return "编程功能：未定义";
		}
	}
    
    public String eventDecode(int ec, int erc, int p1, int p2, int p3, int p4) {
        switch (ec) {
            case 0x00000001:
                return "服务器启动";
            case 0x00000002:
                return "服务器关闭";
            case 0x00000004:
                return "监听启动失败";
            case 0x00000008:
                return "与客户端建立连接";
            case 0x00000010:
                return "客户端拒绝连接";
            case 0x00000020:
                return "空间不足";
            case 0x00000040:
                return "客户端错误";
            case 0x00000080:
                return "与客户端断开连接";
            case 0x00000100:
                return "客户端退出";
            case 0x00000200:
                return "客户端掉线";
            case 0x00010000:
                return badPduDecode(erc, p1);
            case 0x00020000:
                return "读数据" + dataDecode(erc, p3, p4);
            case 0x00040000:
                return "写数据" + dataDecode(erc, p3, p4);
            case 0x00080000:
                return "协商PDU";
            case 0x00100000:
                return szlDecode(erc, p1, p2);
            case 0x00200000:
                return "时钟";
            case 0x00400000:
                return "上传" + loadDecode(erc, p1, p2);
			case 0x00800000:
                return "下载" + loadDecode(erc, p1, p2);
            case 0x01000000:
                return blockDecode(erc, p1, p2);
            case 0x02000000:
                return securityDecode(p1);
            case 0x04000000:
                return controlDecode(p2);
			case 0x08000000:
				return programmerDecode(p1);
			case 0x10000000:
				return "数据循环功能";
            default:
                return "未知事件";
        }
    }
    
    public void printPageTabs(JspWriter out, Statement statement, int page) throws Exception {
        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM server_events;");
        if (resultSet.first()) {
            int size = resultSet.getInt("COUNT(*)");
            out.println("<p>记录总数&nbsp;=&nbsp;" + size + "</p>");
            out.print("<p>");
            size = (size + PAGE_SIZE - 1) / PAGE_SIZE;
            for (int i = 1; i <= size; i++) {
                if (i == page) {
                    out.print("[" + i + "]&nbsp;");
                } else {
                    out.print("<a href='events.jsp?page=" + i + "'>[" + i + "]</a>&nbsp;");
                }
            }
            out.println("</p>");
        } else {
            out.println("<p>没有事件记录</p>");
        }
    }
    
    public void printTable(JspWriter out, Statement statement, int page) throws Exception {
        out.println("<table width='100%' border='1' frame='void' rules='rows'>");
        out.println("<tr><th width='22%' align='left'>时间</th><th width='16%' align='left'>客户端</th><th align='left'>事件内容</th></tr>");
        ResultSet resultSet = statement.executeQuery("SELECT * FROM server_events ORDER BY id DESC LIMIT " + PAGE_SIZE * (page - 1) + "," + PAGE_SIZE + ";");
        int iTime = resultSet.findColumn("time");
        int iEventSender = resultSet.findColumn("eventSender");
        int iEC = resultSet.findColumn("eventCode");
        int iERC = resultSet.findColumn("eventReturnCode");
        int iP1 = resultSet.findColumn("p1");
        int iP2 = resultSet.findColumn("p2");
        int iP3 = resultSet.findColumn("p3");
        int iP4 = resultSet.findColumn("p4");
        while (resultSet.next()) {
            out.print("<tr><td>");
            out.print(timeDecode(resultSet.getLong(iTime)));
            out.print("</td><td>");
            out.print(senderDecode(resultSet.getInt(iEventSender)));
            out.print("</td><td>");
            out.print(eventDecode(resultSet.getInt(iEC), resultSet.getInt(iERC),
            resultSet.getInt(iP1), resultSet.getInt(iP2), resultSet.getInt(iP3), resultSet.getInt(iP4)));
            out.println("</td></tr>");
        }
        out.println("</table>");
        printPageTabs(out, statement, page);
    }
%>
<head>
    <meta charset='utf-8'>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" href="/image/favicon.jpg" type="image/x-icon"/>
    <link rel="stylesheet" href="style.css">
    <script src="http://code.jquery.com/jquery-latest.min.js" type="text/javascript"></script>
    <script src="navigation_bar.js"></script>
    <title>蜜罐后台监控系统</title>
</head>
<body>
<div id='title'>“槐树花”蜜罐后台监控系统</div>
<div id='subtitle'>&nbsp;&nbsp;&nbsp;&nbsp;NESC, ZJU</div>
<div id='navigationbar'><ul>
    <li><a href='#'><span>系统简介</span></a><ul>
		<li><a href='index.html'><span>概述</span></a></li>
		<li><a href='background.html'><span>研究背景</span></a></li>
		<li><a href='architecture.html'><span>系统结构</span></a></li>
		<li><a href='manual.html'><span>系统功能</span></a></li>
	</ul></li>
    <li><a href='#'><span>用户</span></a><ul>
		<li><a href='login.jsp'><span>登录/注销</span></a></li>
		<li><a href='authority.jsp'><span>权限管理</span></a></li>
	</ul></li>
    <li class='active'><a href='#'><span>监控记录</span></a><ul>
		<li><a href='events.jsp'><span>事件数据</span></a></li>
		<li><a href='events_raw.jsp'><span>事件数据（原始）</span></a></li>
		<li><a href='ids.jsp'><span>端口扫描检测</span></a></li>
		<li><a href='ids_map.jsp'><span>端口扫描地图</span></a></li>
	</ul></li>
    <li><a href='#'><span>连接到蜜罐</span></a><ul>
		<li><a href='honeypot_state.jsp'><span>读取状态信息</span></a></li>
		<li><a href='honeypot_block.jsp'><span>读取块</span></a></li>
	</ul></li>
</ul></div>
<div id='content'>
<%
    Object username = session.getAttribute("username");
    Object password = session.getAttribute("password");
    if (username != null && password != null && !username.equals("")) {
        try {
            Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://rm-uf6n32jy1k5m0720wzo.mysql.rds.aliyuncs.com:3306/honeypot?useSSL=true",
                        username.toString(),
                        password.toString());
            printTable(out, connection.createStatement(), parsePageParameter(request.getParameter("page")));
            connection.close();
        } catch (Exception e) {
            out.println("<p>" + e.toString() + "</p>");
        }
    } else {
        out.println("<p>您还没有登录</p>");
    }
%>
</div>
</body>
</html>