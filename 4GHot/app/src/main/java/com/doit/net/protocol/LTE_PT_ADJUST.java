package com.doit.net.protocol;

import android.util.Log;

import com.doit.net.socket.ServerSocketUtils;
import com.doit.net.utils.FormatUtils;
import com.doit.net.utils.LogUtils;
import com.doit.net.utils.UtilDataFormatChange;

import java.util.Arrays;

/**
 * Author：Libin on 2020/8/6 18:14
 * Email：1993911441@qq.com
 * Describe：校时
 */
public class LTE_PT_ADJUST {

    public static final byte PT_ADJUST=0x03;


    public static final byte ADJUST_APP=0x01;
    public static final byte ADJUST_RESP=0x02;


    //校时回复
    public static void response(LTEReceivePackage receivePackage) {
        LTESendPackage sendPackage = new LTESendPackage();

        //设置Sequence ID
        sendPackage.setPackageSequence(receivePackage.getPackageSequence());
        //设置Session ID
        sendPackage.setPackageSessionID(receivePackage.getPackageSessionID());
        //设置EquipType
        sendPackage.setPackageEquipType(receivePackage.getPackageEquipType());
        //设置预留
        sendPackage.setPackageReserve((byte)0);
        //设置主类型
        sendPackage.setPackageMainType(PT_ADJUST);
        //设置子类型
        sendPackage.setPackageSubType(ADJUST_RESP);


        long localTime = System.currentTimeMillis();  //当前毫秒数
        int currentSeconds = (int) (localTime/1000);  //当前秒数
        int currentMicroseconds = (int) ((localTime % 1000) *1000); //当前微秒


        byte[] subContent = new byte[16];
        System.arraycopy(receivePackage.getByteSubContent(), 0, subContent, 0, 8);
        System.arraycopy(FormatUtils.getInstance().int2Bytes(currentSeconds), 0, subContent, 8, 4);
        System.arraycopy(FormatUtils.getInstance().int2Bytes(currentMicroseconds), 0, subContent, 12, 4);
        //设置内容
        sendPackage.setByteSubContent(subContent);
        //设置校验位


        sendPackage.setPackageCheckNum(sendPackage.getCheckNum());


        //获取整体的包
        byte[] tempSendBytes=sendPackage.getPackageContent();


        LogUtils.log("TCP发送：ip"+receivePackage.getIp()+"Type:" + sendPackage.getPackageMainType() + ";  SubType:0x" + Integer.toHexString(sendPackage.getPackageSubType())+ ";  子协议:" + Arrays.toString(sendPackage.getByteSubContent()));

        ServerSocketUtils.getInstance().sendData(receivePackage.getIp(),tempSendBytes);


    }


//    public static void sendData(byte subType, String paramContent){
//        LTESendPackage sendPackage = new LTESendPackage();
//        //设置Sequence ID
//        sendPackage.setPackageSequence(LTEProtocol.getSequenceID());
//        //设置Session ID
//        sendPackage.setPackageSessionID(LTEProtocol.getSessionID());
//        //设置EquipType
//        sendPackage.setPackageEquipType(LTEProtocol.equipType);
//        //设置预留
//        sendPackage.setPackageReserve((byte) 0xFF);
//        //设置主类型
//        sendPackage.setPackageMainType(PT_ADJUST);
//        //设置子类型
//        sendPackage.setPackageSubType(subType);
//        sendPackage.setByteSubContent(UtilDataFormatChange.stringtoBytesForASCII(paramContent));
//
//        //设置校验位
//        sendPackage.setPackageCheckNum(sendPackage.getCheckNum());
//
//        //获取整体的包
//        byte[] tempSendBytes = sendPackage.getPackageContent();
//        LogUtils.log("TCP发送：Type:" + sendPackage.getPackageMainType() + ";  SubType:0x" + Integer.toHexString(sendPackage.getPackageSubType()) + ";  子协议:" + UtilDataFormatChange.bytesToString(sendPackage.getByteSubContent(), 0));
//        ServerSocketUtils.getInstance().sendData(tempSendBytes);
//    }
}
