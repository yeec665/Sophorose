<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="indi.hiro.pagoda.*"%>
<html>
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
        <li><a href='index.html'><span>首页</span></a></li>
        <li><a href='background.html'><span>研究背景</span></a></li>
        <li><a href='structure.html'><span>系统结构</span></a></li>
        <li><a href='principle.html'><span>系统原理</span></a></li>
        <li><a href='result.html'><span>结果与分析</span></a></li>
	</ul></li>
    <li><a href='#'><span>用户</span></a><ul>
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
    <li class='active'><a href='#'><span>连接到蜜罐</span></a><ul>
		<li><a href='honeypot_info.jsp'><span>读取状态信息</span></a></li>
		<li><a href='honeypot_data.jsp'><span>读取数据</span></a></li>
	</ul></li>
</ul></div>
<%
    HoneypotInfo honeypotInfo = new HoneypotInfo(request);
%>
<br>
<form action='honeypot_info.jsp' method='POST'>
	<fieldset>
	<legend>连接</legend>
	IP地址：<input type='text' name='host' value='<%out.print(honeypotInfo.inputHost());%>'>&nbsp;&nbsp;
	端口：<input type='number' name='port' value='<%out.print(honeypotInfo.inputPort());%>' min='0' max='65535'>&nbsp;&nbsp;
	机架：<input type='number' name='rack' value='<%out.print(honeypotInfo.inputRack());%>' min='0' max='7'>&nbsp;&nbsp;
	插槽：<input type='number' name='slot' value='<%out.print(honeypotInfo.inputSlot());%>' min='0' max='31'>&nbsp;&nbsp;
	<input type='submit' value='读取状态信息'>
	</fieldset>
</form>
<br>
<div id='content'>
<%
    honeypotInfo.printContent(out);
%>
</div>
</body>
</html>
