package indi.hiro.pagoda;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Hiro on 2019/4/5.
 */
public class S7Event implements java.io.Serializable {

    static void appendHex(StringBuilder sb, int value, int digit) {
        for (int i = digit - 1; i >= 0; i--) {
            int hex = 0xF & (value >> (i << 2));
            if (hex < 0xA) {
                sb.append((char) ('0' + hex));
            } else {
                sb.append((char) ('A' - 0xA + hex));
            }
        }
    }

    static String blockName(int c) {
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

    final int time;
    final int host;
    final int sender;
    final int code;
    final int returnCode;
    final int parameter1;
    final int parameter2;
    final int parameter3;
    final int parameter4;

    public S7Event(ResultSet resultSet, S7EventReader reader) throws SQLException {
        time = resultSet.getInt(reader.iTime);
        host = resultSet.getInt(reader.iHost);
        sender = resultSet.getInt(reader.iSender);
        code = resultSet.getInt(reader.iCode);
        returnCode = 0xFFFF & resultSet.getInt(reader.iReturnCode);
        parameter1 = 0xFFFF & resultSet.getInt(reader.iParameter1);
        parameter2 = 0xFFFF & resultSet.getInt(reader.iParameter2);
        parameter3 = 0xFFFF & resultSet.getInt(reader.iParameter3);
        parameter4 = 0xFFFF & resultSet.getInt(reader.iParameter4);
    }

    public long getTimeMillis() {
        return 1000L * time;
    }

    public String eventToHtml() {
        return "<span title=\"" + hintToString().replace(" ", "&nbsp;") + "\">" + eventToString().replace(" ", "&nbsp;") + "</span>";
    }

    public String eventToString() {
        switch (code) {
            case 0x00000001: // evcServerStarted
                return protocolName() + "服务器启动";
            case 0x00000002: // evcServerStopped
                return protocolName() + "服务器关闭";
            case 0x00000004: // evcListenerCannotStart
                return protocolName() + "监听启动失败";
            case 0x00000008: // evcClientAdded
                return protocolName() + "服务器与客户端建立连接";
            case 0x00000010: // evcClientRejected
                return "客户端拒绝连接";
            case 0x00000020: // evcClientNoRoom
                return "空间不足";
            case 0x00000040: // evcClientException
                return "客户端错误";
            case 0x00000080: // evcClientDisconnected
                return protocolName() + "服务器与客户端断开连接";
            case 0x00000100: // evcClientTerminated
                return "客户端退出";
            case 0x00000200: // evcClientsDropped
                return "客户端掉线";
            case 0x00010000: // evcPDUincoming
                return badPduDetail();
            case 0x00020000: // evcDataRead
                return dataDetail(false);
            case 0x00040000: // evcDataWrite
                return dataDetail(true);
            case 0x00080000: // evcNegotiatePDU
                return negotiatePduDetail();
            case 0x00100000: // evcReadSZL
                return szlDetail();
            case 0x00200000: // evcClock
                return clockDetail();
            case 0x00400000: // evcUpload
                return loadDetail(false);
            case 0x00800000: // evcDownload
                return loadDetail(true);
            case 0x01000000: // evcDirectory
                return directoryDetail();
            case 0x02000000: // evcSecurity
                return securityDetail();
            case 0x04000000: // evcControl
                return controlDetail();
            case 0x08000000: // evcGroupProgrammer
                return programmerDetail();
            case 0x10000000: // evcGroupCyclicData
                return cyclicDataDetail();
            case 0x20000000: // evcModbusPDUincoming
                return badModbusPduDetail();
            case 0x40000000: // evcModbusData
                return modbusDataDetail();
            case 0x80000000: // evcModbusDiagnostics
                return modbusDiagnosticsDetail();
            default:
                return "未知事件";
        }
    }

    private String protocolName() {
        switch (parameter1) {
            case 102:
                return "S7comm ";
            case 502:
                return "Modbus ";
            default:
                return "";
        }
    }

    private String badPduType() {
        switch (returnCode) {
            case 0x0001: // evrFragmentRejected
                return "错误：报文段被拒绝";
            case 0x0002: // evrMalformedPDU
                return "错误：报文格式错误";
            case 0x0003: // evrSparseBytes
                return "错误：报文太短";
            case 0x0004: // evrCannotHandlePDU
                return "错误：无法处理报文";
            case 0x0005: // evrNotImplemented
                return "错误：功能没有实现";
            case 0x0006: // evrErrException
                return "错误：发生异常";
            case 0x0007: // evrErrAreaNotFound
                return "错误：区域未找到";
            case 0x0008: // evrErrOutOfRange
                return "错误：超出边界";
            case 0x0009: // evrErrOverPDU
                return "错误：超长报文";
            case 0x000A:
                return "错误：传输长度";
            case 0x000B:
                return "错误：无效的用户数据组";
            case 0x000C:
                return "错误：无效的系统状态表编号";
            case 0x000D:
                return "错误：长度不匹配";
            case 0x000E:
                return "错误：无法上传";
            case 0x000F:
                return "错误：无法下载";
            case 0x0010:
                return "错误：非法ID";
            case 0x0011:
                return "错误：资源未找到";
            case 0x0012:
                return "错误：内存限制";
            case 0x0013:
                return "错误：无效的协议ID";
            case 0x0014:
                return "错误：报文太短";
            case 0x0015:
                return "错误：数据长度";
            case 0x0016:
                return "错误：未知的Modbus功能码";
            case 0x0017:
                return "错误：未知的Modbus子功能码";
            default:
                return "错误：未知的错误类型";
        }
    }

    private String badPduDetail() {
        return badPduType() + "（长度 " + parameter1 + " ）";
    }

    private String dataDetail(boolean rw) {
        StringBuilder sb = new StringBuilder();
        sb.append("在 0x");
        appendHex(sb, parameter3, 4);
        sb.append(rw ? " 写入" : " 读取");
        sb.append("长度 0x");
        appendHex(sb, parameter4, 4);
        sb.append(" 的数据");
        if (returnCode != 0) {
            sb.append("（");
            sb.append(dataResult());
            sb.append("）");
        }
        return sb.toString();
    }

    private String dataResult() {
        switch (returnCode) {
            case 0x0000: // evrNoError
                return "成功";
            case 0x0006: // evrErrException
                return "发生异常";
            case 0x0007: // evrErrAreaNotFound
                return "数据区没有找到";
            case 0x0008: // evrErrOutOfRange
                return "超出范围";
            case 0x0009: // evrErrOverPDU
                return "数据过大";
            case 0x000A: // evrErrTransportSize
                return "非法传输长度";
            case 0x000D: // evrDataSizeMismatch
                return "长度失配";
            default:
                return "未知结果";
        }
    }

    private String negotiatePduDetail() {
        return "协商PDU长度为" + parameter1;
    }

    private String szlDetail() {
        StringBuilder sb = new StringBuilder();
        sb.append("读取系统状态子表 0x");
        appendHex(sb, parameter1, 4);
        sb.append(" 索引 0x");
        appendHex(sb, parameter2, 4);
        if (returnCode != 0) {
            sb.append(" （失败）");
        }
        return sb.toString();
    }

    private String clockDetail() {
        switch (parameter1) {
            case 0x0001: // evsGetClock
                return "读时钟";
            case 0x0002: // evsSetClock
                return "写时钟";
            default:
                return "未知的时钟操作";
        }
    }

    private String loadDetail(boolean ud) {
        StringBuilder sb = new StringBuilder();
        sb.append(ud ? "下载" : "上传");
        sb.append(blockName(parameter1));
        sb.append(parameter2);
        if (returnCode != 0) {
            sb.append("（失败）");
        }
        return sb.toString();
    }

    private String directoryDetail() {
        StringBuilder sb = new StringBuilder();
        switch (parameter1) {
            case 0x0001:
                sb.append("获取块列表");
                break;
            case 0x0002:
                sb.append("枚举");
                sb.append(blockName(parameter2));
                sb.append("开始");
                break;
            case 0x0003:
                sb.append("枚举");
                sb.append(blockName(parameter2));
                sb.append("继续");
                break;
            case 0x0004:
                sb.append("获取");
                sb.append(blockName(parameter2));
                sb.append("信息");
                break;
            default:
                sb.append("未知的块操作");
        }
        if (returnCode != 0) {
            sb.append("（失败）");
        }
        return sb.toString();
    }

    private String securityDetail() {
        switch (parameter1) {
            case 0x0001: // evsSetPassword
                return "安全功能：设置会话密码";
            case 0x0002: // evsClrPassword
                return "安全功能：清除会话密码";
            default:
                return "安全功能：未知";
        }
    }

    private String controlDetail() {
        switch (parameter1) {
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

    public String programmerDetail() {
        switch (parameter1) {
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

    public String cyclicDataDetail() {
        return "数据循环功能";
    }

    private String badModbusPduDetail() {
        return badPduType() + "（长度 " + parameter1 + " ）";
    }

    private String modbusDataDetail() {
        StringBuilder sb = new StringBuilder();
        switch (parameter1) {
            case 0x01: // ModbusFunctionReadCoils
                sb.append("在输出寄存器 Q 位地址 0x");
                appendHex(sb, parameter2, 4);
                sb.append(" 处读取 ");
                sb.append(parameter3);
                sb.append(" 位数据");
                break;
            case 0x02: // ModbusFunctionReadDiscreteInputs
                sb.append("在输入寄存器 I 位地址 0x");
                appendHex(sb, parameter2, 4);
                sb.append(" 处读取 ");
                sb.append(parameter3);
                sb.append(" 位数据");
                break;
            case 0x03: // ModbusFunctionReadHoldingRegisters
                sb.append("在位寄存器 M 字地址 0x");
                appendHex(sb, parameter2, 4);
                sb.append(" 处读取 ");
                sb.append(parameter3);
                sb.append(" 字数据");
                break;
            case 0x04: // ModbusFunctionReadInputRegisters
                sb.append("在输入寄存器 I 字地址 0x");
                appendHex(sb, parameter2, 4);
                sb.append(" 处读取 ");
                sb.append(parameter3);
                sb.append(" 字数据");
                break;
            case 0x05: // ModbusFunctionWriteSingleCoil
                sb.append("在输出寄存器 Q 位地址 0x");
                appendHex(sb, parameter2, 4);
                sb.append(" 处写入 ");
                sb.append(parameter3 != 0 ? "1" : "0");
                break;
            case 0x06: // ModbusFunctionWriteSingleRegister
                sb.append("在位寄存器 M 字地址 0x");
                appendHex(sb, parameter2, 4);
                sb.append(" 处写入 0x");
                appendHex(sb, parameter3, 4);
                break;
            case 0x0F: // ModbusFunctionWriteMultipleCoils
                sb.append("在输出寄存器 Q 位地址 0x");
                appendHex(sb, parameter2, 4);
                sb.append(" 处写入 ");
                sb.append(parameter3);
                sb.append(" 位数据");
                break;
            case 0x10: // ModbusFunctionWriteMultipleRegisters
                sb.append("在位寄存器 M 位地址 0x");
                appendHex(sb, parameter2, 4);
                sb.append(" 处写入 ");
                sb.append(parameter3);
                sb.append(" 位数据");
                break;
        }
        return sb.toString();
    }

    private String modbusDiagnosticsDetail() {
        return "读取设备 " + parameter3 + " 的" + modbusDeviceFieldName();
    }

    private String modbusDeviceFieldName() {
        switch (parameter4) {
            case 0x00: // ModbusObjectIdVendorName
                return "生产商名称";
            case 0x01: // ModbusObjectIdProductCode
                return "产品代号";
            case 0x02: // ModbusObjectIdMajorMinorRevision
                return "主次版本";
            case 0x03: // ModbusObjectIdVendorUrl
                return "生产商 URL";
            case 0x04: // ModbusObjectIdProductName
                return "产品名称";
            case 0x05: // ModbusObjectIdModelName
                return "型号名称";
            case 0x06: // ModbusObjectIdApplicationName
                return "应用名称";
            default:
                return "未知信息";
        }
    }

    public String hintToString() {
        switch (code) {
            case 0x00000001: // evcServerStarted
            case 0x00000002: // evcServerStopped
            case 0x00000004: // evcListenerCannotStart
            case 0x00000008: // evcClientAdded
            case 0x00000010: // evcClientRejected
            case 0x00000020: // evcClientNoRoom
            case 0x00000040: // evcClientException
            case 0x00000080: // evcClientDisconnected
            case 0x00000100: // evcClientTerminated
            case 0x00000200: // evcClientsDropped
                return "";
            case 0x00010000: // evcPDUincoming
            case 0x00020000: // evcDataRead
            case 0x00040000: // evcDataWrite
            case 0x00080000: // evcNegotiatePDU
                return fullContentHint();
            case 0x00100000: // evcReadSZL
                return szlHint();
            case 0x00200000: // evcClock
            case 0x00400000: // evcUpload
            case 0x00800000: // evcDownload
            case 0x01000000: // evcDirectory
            case 0x02000000: // evcSecurity
            case 0x04000000: // evcControl
            case 0x08000000: // evcGroupProgrammer
            case 0x10000000: // evcGroupCyclicData
            default:
                return fullContentHint();
        }
    }

    private String szlHint() {
        String hint = szlMainGroup();
        if (hint != null) {
            return hint;
        }
        hint = szlSubGroup();
        if (hint != null) {
            return hint;
        }
        return fullContentHint();
    }

    private String szlMainGroup() {
        switch (parameter1) {
            case 0x0011:
                return "设备序列号";
            case 0x0013:
                return "用户存储器区域/工作存储器";
            case 0x0113:
                return "用户存储器区域/指定存储器区域的一条数据记录";
            case 0x0014:
                return "操作系统区域/所有系统区域的数据记录";
            case 0x0F14:
                return "操作系统区域/仅标题信息";
            case 0x0015:
                return "块类型";
            case 0x0F19:
                return "模块LED的状态/仅标题信息";
            case 0x001C:
                return "组件标识的所有记录，包括设备名称和CPU型号";
            case 0x0025:
                return "所有部分过程映像和OB的分配";
            case 0x00A0:
                return "诊断缓冲区";
            case 0x00B1:
            case 0x00B2:
            case 0x00B3:
                return "模块诊断信息";
            case 0x0222:
                return "报警状态/指定中断的数据记录";
            case 0x0591:
                return "模块状态信息/主机模块的所有子模块";
            case 0x0C91:
                return "模块状态信息/中央机架中的模块或与集成的DP接口模块连接的模块";
            case 0x0D91:
                return "模块状态信息/指定机架中的所有模块";
            case 0x0F74:
                return "报头信息";
            case 0x0F25:
                return "SZL部分列表标题信息";
            default:
                return null;
        }
    }

    private String szlSubGroup() {
        switch (0xFFFF0000 & (parameter1 << 16) | 0x0000FFFF & parameter2) {
            case 0x01110001:
                return "模块标识/CPU类型和版本号";
            case 0x01110006:
                return "模块标识/基本硬件的标识";
            case 0x01110007:
                return "模块标识/基本固化程序的标识";
            case 0x00120000:
                return "CPU特征/所有记录";
            case 0x00120100:
                return "CPU特征/CPU中的时间系统";
            case 0x00120300:
                return "CPU特征/STEP7操作设置";
            case 0x01120000:
                return "CPU特征/STEP7处理";
            case 0x01120100:
                return "CPU特征/CPU中的时间系统";
            case 0x01120200:
                return "CPU特征/CPU的系统特性";
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

    private String fullContentHint() {
        StringBuilder sb = new StringBuilder();
        sb.append("事件码 0x");
        appendHex(sb, code, 8);
        if (code != 0 && (code & (code - 1)) == 0) {
            sb.append("(1<<").append(Integer.numberOfTrailingZeros(code)).append(")");
        }
        if (returnCode != 0) {
            sb.append(" 返回码 0x");
            appendHex(sb, returnCode, 4);
        }
        if (parameter1 != 0 || parameter2 != 0 || parameter3 != 0 || parameter4 != 0) {
            sb.append(" 参数 0x");
            appendHex(sb, parameter1, 4);
            if (parameter2 != 0 || parameter3 != 0 || parameter4 != 0) {
                sb.append(", 0x");
                appendHex(sb, parameter2, 4);
                if (parameter3 != 0 || parameter4 != 0) {
                    sb.append(", 0x");
                    appendHex(sb, parameter3, 4);
                    if (parameter4 != 0) {
                        sb.append(", 0x");
                        appendHex(sb, parameter4, 4);
                    }
                }
            }
        }
        return sb.toString();
    }
}
