package com.doit.net.utils;

/**
 * Created by Zxc on 2019/3/22.
 */

public class UtilOperator {

    public static String getOperatorName(String plmn){
        if(plmn == null){
            return "";
        }
        if (plmn.startsWith("46000") || plmn.startsWith("46002") ||
            plmn.startsWith("46007") || plmn.startsWith("46004")) {// 因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
            // 中国移动
            return "CTJ";
        } else if (plmn.startsWith("46001") || plmn.startsWith("46006") || plmn.startsWith("46009")) {
            // 中国联通
            return "CTU";
        } else if (plmn.startsWith("46003") || plmn.startsWith("46005") || plmn.startsWith("46011")) {
            // 中国电信
            return "CTC";
        }
        return "";
    }

    public static String getOperatorNameCH(String plmn){
        if(plmn == null){
            return "";
        }
        if (plmn.startsWith("46000") || plmn.startsWith("46002") ||
                plmn.startsWith("46007") || plmn.startsWith("46004")) {// 因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
            // 中国移动
            return "移动";
        } else if (plmn.startsWith("46001") || plmn.startsWith("46006") || plmn.startsWith("46009")) {
            // 中国联通
            return "联通";
        } else if (plmn.startsWith("46003") || plmn.startsWith("46005") || plmn.startsWith("46011")) {
            // 中国电信
            return "电信";
        }
        return "??";
    }

    /* 判断频点是否属于目标制式内*/
    public static boolean isArfcnInOperator(String operator, String fcn) {
        if ("".equals(fcn))
            return false;

        int intFcn = Integer.parseInt(fcn);
        if (operator.equals("CTJ")){
            return (rangeInDefined(intFcn, 38250, 38600) ||
                    rangeInDefined(intFcn, 38850, 39350) ||
                    rangeInDefined(intFcn, 40440, 41040) || intFcn == 1300);
        }else if (operator.equals("CTU")){   //联通暂不考虑TDD频段,并包括了B3的DCS
            return (rangeInDefined(intFcn, 1350, 1750) ||
                    rangeInDefined(intFcn, 350, 599));
        }else if (operator.equals("CTC")){
            return (rangeInDefined(intFcn, 1750, 1900) ||
                    rangeInDefined(intFcn, 0, 200)||
                    rangeInDefined(intFcn, 41040, 41240));
        }

        return true;
    }



    private static boolean rangeInDefined(int current, int min, int max) {
        return Math.max(min, current) == Math.min(current, max);
    }

    public static int getBandByFcn(int fcn) {
        if (rangeInDefined(fcn, 0, 599)) {
            return 1;
        } else if (rangeInDefined(fcn, 1200, 1949)) {
            return 3;
        } else if (rangeInDefined(fcn, 37750, 38250)) {
            return 38;
        }else if (rangeInDefined(fcn, 38250, 38650)){
            return 39;
        }else if (rangeInDefined(fcn, 38650, 39650)){
            return 40;
        }else if (rangeInDefined(fcn, 39650, 41589))
            return 41;

        return -1;
    }
}
