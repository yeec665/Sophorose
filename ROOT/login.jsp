<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.io.*,java.util.*,java.sql.*,indi.hiro.pagoda.*"%>
<html>
<head>
    <meta charset='utf-8'>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" href="/image/favicon.jpg" type="image/x-icon"/>
    <link rel="stylesheet" href="style.css">
    <script src="http://code.jquery.com/jquery-latest.min.js" type="text/javascript"></script>
    <script src="navigation_bar.js"></script>
    <title>用户登录/注销</title>
</head>
<body>
<div id='title'>“槐树花”蜜罐后台监控系统</div>
<div id='subtitle'>&nbsp;&nbsp;&nbsp;&nbsp;NESC, ZJU</div>
<div id='navigationbar'><ul>
    <li><a href='#'><span>系统简介</span></a><ul>
        <li><a href='index.html'><span>首页</span></a></li>
        <li><a href='background.html'><span>研究背景</span></a></li>
        <li><a href='structure.html'><span>系统结构</span></a></li>
        <li><a href='principle.html'><span>系统原理</span></a></li>
        <li><a href='result.html'><span>结果与分析</span></a></li>
	</ul></li>
    <li class='active'><a href='#'><span>用户</span></a><ul>
		<li><a href='login.jsp'><span>登录/注销</span></a></li>
		<li><a href='grant.jsp'><span>权限管理</span></a></li>
	</ul></li>
    <li><a href='#'><span>监控记录</span></a><ul>
        <li><a href='s7_events.jsp'><span>事件记录</span></a></li>
        <li><a href='s7_volume.jsp'><span>访问量</span></a></li>
        <li><a href='ids_captures.jsp'><span>端口访问</span></a></li>
        <li><a href='ids_statistics.jsp'><span>端口扫描记录</span></a></li>
        <li><a href='ids_analysis.jsp'><span>端口扫描分析</span></a></li>
	</ul></li>
    <li><a href='#'><span>连接到蜜罐</span></a><ul>
		<li><a href='honeypot_info.jsp'><span>读取状态信息</span></a></li>
		<li><a href='honeypot_data.jsp'><span>读取数据</span></a></li>
	</ul></li>
</ul></div>
<div id='logindialog'><center>
    <%
        SardineLogin.collectPost(request, session);
        int result = SardineLogin.login(session);
        if (result == SardineLogin.LC_LOGIN_SUCCESS) {
            String username = session.getAttribute("username").toString();
    %>
    <p>欢迎访问，<%= username %></p>
    <form action="login.jsp" method="POST">
        <input type="hidden" name="username" value="">
        <input type="hidden" name="password" value="">
        <input type="submit" value="注销" width="100%">
    </form>
    <%
        } else if (result == SardineLogin.LC_FIELD_REQUIRED) {
    %>
        <form action="login.jsp" method="POST">
        <table>
        <tr><td>用户名：</td><td><input type="text" name="username" value="visitor"></td></tr>
        <tr><td>密码：</td><td><input type="password" name="password" value="123456"></td></tr>
        <tr><td colspan="2"><center><input type="submit" value="登录" width="100%"></center></td></tr>
        </table>
        </form>
    <%
        } else if (result == SardineLogin.LC_LINK_ERROR) {
    %>
    <p>数据库连接错误</p>
    <form action="login.jsp" method="POST">
        <input type="hidden" name="username" value="">
        <input type="hidden" name="password" value="">
        <input type="submit" value="返回" width="100%">
    </form>
    <%
        } else if (result == SardineLogin.LC_ACCESS_DENIED) {
    %>
    <p>用户名或密码错误</p>
    <form action="login.jsp" method="POST">
        <input type="hidden" name="username" value="">
        <input type="hidden" name="password" value="">
        <input type="submit" value="返回" width="100%">
    </form>
    <%
        } else if (result == SardineLogin.LC_ACCESS_DENIED) {
    %>
    <p>未知错误</p>
    <form action="login.jsp" method="POST">
        <input type="hidden" name="username" value="">
        <input type="hidden" name="password" value="">
        <input type="submit" value="返回" width="100%">
    </form>
    <%
        }
    %>
</center></div>
</body>
</html>