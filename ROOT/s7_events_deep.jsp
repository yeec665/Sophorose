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
    <title>PLC蜜罐事件数据</title>
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
    <li class='active'><a href='#'><span>监控记录</span></a><ul>
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
<div id='content'>
    <ul class='filtertab'>
        <li class='dull'>过滤器：</li>
        <a href='s7_events.jsp'><li>全部</li></a>
        <a href='s7_events_server.jsp'><li>服务器启动/停止</li></a>
        <a href='s7_events_client.jsp'><li>客户端会话</li></a>
        <a href='s7_events_info.jsp'><li>读取信息</li></a>
        <li class='active'>深入访问</li>
    </ul>
    <%
        (new S7EventReader(session, request, "s7_events_deep.jsp", S7EventReader.FILTER_ACCESS_DEEP)).printContent(out);
    %>
</div>
</body>
</html>