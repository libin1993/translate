package com.doit.net.bean;

/**
 * Created by wiker on 2016-08-15.
 */
public class LocationBean {

    private String imsi = "";
    private boolean isStart = false;
    private int type;

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isLocateStart() {
        return isStart;
    }

    public void setLocateStart(boolean start) {
        this.isStart = start;
    }

}
