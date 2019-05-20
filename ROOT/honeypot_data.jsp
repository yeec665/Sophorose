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
    HoneypotData honeypotData = new HoneypotData(request);
%>
<br>
<form action='honeypot_data.jsp' method='POST'>
	<fieldset>
	<legend>Modbus读取数据</legend>
	IP地址：<input type='text' name='host' value='<%out.print(honeypotData.inputHost());%>'>&nbsp;&nbsp;
	端口：<input type='number' name='port' value='<%out.print(honeypotData.inputPort());%>' min='0' max='65535'>&nbsp;&nbsp;
	地址：<input type='number' name='address' value='<%out.print(honeypotData.inputAddress());%>' min='0' max='100'>&nbsp;&nbsp;
	长度：<input type='number' name='length' value='<%out.print(honeypotData.inputLength());%>' min='0' max='200'>&nbsp;&nbsp;
	<input type='submit' value='读取数据'>
	<br><input type='radio' name='function' value='1'<%out.print(honeypotData.functionReadCoil());%>>&nbsp;读线圈
	<br><input type='radio' name='function' value='2'<%out.print(honeypotData.functionReadDiscreteInputs());%>>&nbsp;读离散输入
	<br><input type='radio' name='function' value='3'<%out.print(honeypotData.functionReadHoldingRegisters());%>>&nbsp;读保持寄存器
	<br><input type='radio' name='function' value='4'<%out.print(honeypotData.functionReadInputRegisters());%>>&nbsp;读输入寄存器
	</fieldset>
</form>
<div id='content'>
<%
    honeypotData.printContent(out);
%>
</div>
</body>
</html>
