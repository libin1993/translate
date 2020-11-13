package com.doit.net.protocol;

/**
 * Author：Libin on 2020/9/27 15:11
 * Email：1993911441@qq.com
 * Describe：2G消息类型
 */
public class MsgType2G {
    //主类型
    public static final byte PT_LOGIN = 0x01;   //登录协议
    public static final byte PT_FILE = 0x02;     //文件操作协议
    public static final byte PT_ADJUST = 0x03;  //校时协议
    public static final byte PT_SYSTEM = 0x04;  //设备系统操作协议
    public static final byte PT_PARAM = 0x05;   //参数查询、设置协议
    public static final byte PT_WARNING = 0x06;  //异常通知协议
    public static final byte PT_RESP = 0x07;    //普通通用回复协议(登录成功上报)


    //子协议
    public static final byte SET_NTC_CONFIG = 0x01;   //设置基本环境参数
    public static final byte SET_NTC_CONFIG_ACK = (byte) 0x81;   //配置基本环境参数应答
    public static final byte SET_MCRF_CONFIG = 0x02;   //设置运营商参数、工作模式
    public static final byte SET_MCRF_CONFIG_ACK = (byte) 0x82;   //设置运营商参数、工作模式应答
    public static final byte SET_RF_SWITCH = (byte) 0x05;   //开关射频
    public static final byte SET_RF_SWITCH_ACK = (byte) 0x85;   //开关射频应答
    public static final byte SET_LOC_IMSI = (byte) 0x43;   //设置定位imsi
    public static final byte SET_LOC_IMSI_ACK = (byte) 0xC3;   //设置定位imsi应答

    public static final byte GET_NTC_CONFIG = 0x11;   //查询基本环境参数
    public static final byte GET_NTC_CONFIG_ACK = (byte) 0x91;   //查询基本环境参数应答
    public static final byte GET_MCRF_CONFIG = 0x12;   //查询运营商参数、工作模式
    public static final byte GET_MCRF_CONFIG_ACK = (byte) 0x92;   //查询运营商参数、工作模式应答
    public static final byte GET_MP_STATE = 0x1E;   //查询猫池状态
    public static final byte GET_MP_STATE_ACK = (byte) 0x9E;   //查询猫池状态应答

    public static final byte REBOOT_DEVICE = (byte) 0x31;   //重启设备
    public static final byte REBOOT_DEVICE_ACK = (byte) 0xB1;   //重启设备应答

    public static final byte RPT_IMSINUM_INFO = (byte) 0x21;   //号码翻译上报
    public static final byte RPT_IMSI_INFO = (byte) 0x22;   //imsi上报
    public static final byte RPT_IMSI_LOC_INFO = (byte) 0x2E;   //2G定位上报
    public static final byte RPT_MP_STATE = (byte) 0x30;   //猫池状态上报

    //消息id
    public static final String SET_NTC_CONFIG_ID = "SET_NTC_CONFIG";  //设置基本环境参数id
    public static final String GET_NTC_CONFIG_ID = "GET_NTC_CONFIG";   //查询基本环境参数id
    public static final String SET_MCRF_CONFIG_ID = "SET_MCRF_CONFIG";  //设置查询运营商参数、工作模式id
    public static final String GET_MCRF_CONFIG_ID = "GET_MCRF_CONFIG";   //查询运营商参数、工作模式id
    public static final String SET_RF_SWITCH_ID = "SET_RF_SWITCH";   //开关射频id
    public static final String SYS_REBOOT_ID = "SYS_REBOOT";   //重启设备id
    public static final String SET_LOC_IMSI_ID = "SET_LOC_IMSI";   //设置定位imsi  id
    public static final String GET_MP_STATE_ID = "GET_MP_STATE";   //查询猫池状态id
}
