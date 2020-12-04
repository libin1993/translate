package com.doit.net.bean;

/**
 * Created by wiker on 2016-08-15.
 */
public class UeidBean {
    private String imsi;
    private String number;
    private String rptTime;
    private int type;  //0:2G    1:4G
    private String remark;
    private boolean isBlack;

    public boolean isBlack() {
        return isBlack;
    }

    public void setBlack(boolean black) {
        isBlack = black;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    //为管控而加
    private int rptTimes = 1; //上报次数累积
    private int srsp;  //最近一次场强

    public UeidBean(){

    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public UeidBean(String imsi, String rptTime, int type) {
        this.imsi = imsi;
        this.rptTime = rptTime;
        this.type = type;
    }

    public String getImsi() {
        return imsi;
    }

    public String getNumber() {
        return number;
    }

    public String getRptTime() {
        return rptTime;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }
    public void setNumber(String number) {
        this.number = number;
    }


    public void setRptTime(String rptTime) {
        this.rptTime = rptTime;
    }


    public int getRptTimes() {
        return rptTimes;
    }

    public int getSrsp() {
        return srsp;
    }

    public void setRptTimes(int rptTimes) {
        this.rptTimes = rptTimes;
    }

    public void setSrsp(int srsp) {
        this.srsp = srsp;
    }
}
