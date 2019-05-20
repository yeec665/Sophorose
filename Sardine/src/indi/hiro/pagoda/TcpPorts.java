package indi.hiro.pagoda;

/**
 * Created by Hiro on 2019/4/4.
 */
public class TcpPorts {

    public static String portDescription(int port) {
        switch (port) {
            default:
                return null;
            case 7:
                return "Echo服务";
            case 13:
                return "日期时间";
            case 17:
                return "每日格言";
            case 20:
                return "FTP数据端口";
            case 21:
                return "FTP控制端口";
            case 22:
                return "安全外壳（SSH）";
            case 23:
                return "Telnet服务";
            case 25:
                return "简单邮件传输协议（SMTP）";
            case 79:
                return "Finger";
            case 80:
                return "超文本传输协议（HTTP）";
            case 102:
                return "S7comm协议";
            case 109:
                return "邮局协议2（POP2）";
            case 110:
                return "邮局协议3（POP3）";
            case 115:
                return "安全文件传输协议（SFTP）";
            case 119:
                return "新闻传输协议（NNTP）";
            case 123:
                return "网络时间协议（NTP）";
            case 143:
                return "互联网消息存取协议（IMAP）";
            case 161:
                return "简单网络管理协议";
            case 179:
                return "边界网关协议（BGP）";
            case 209:
                return "快速邮件传输协议（QMTP）";
            case 443:
                return "安全超文本传输协议（HTTPS）";
            case 445:
                return "共享文件夹和打印机端口";
            case 500:
                return "互联网安全关联和钥匙管理协议（ISAKMP）";
            case 502:
                return "Modbus协议";
            case 554:
                return "实时流播协议（RTSP）";
            case 631:
                return "互联网打印协议（IPP）";
            case 1433:
                return "MS SQL SERVER数据库服务器";
            case 1434:
                return "MS SQL SERVER数据库监视器";
            case 1521:
                return "Oracle数据库";
            case 1723:
                return "点对点隧道协议（PPTP）";
            case 3128:
                return "Squid HTTP代理服务器";
            case 3306:
                return "MySQL数据库";
            case 3389:
                return "Windows远程桌面连接";
            case 4000:
                return "腾讯QQ";
            case 7001:
                return "WebLogic应用服务器";
            case 8080:
                return "TOMCAT服务器";
            case 33060:
                return "MySQL数据库（安全连接）";
        }
    }
}
