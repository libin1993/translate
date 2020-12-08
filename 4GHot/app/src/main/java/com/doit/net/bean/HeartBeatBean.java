package com.doit.net.bean;

/**
 * Author：Libin on 2020/12/7 13:13
 * Email：1993911441@qq.com
 * Describe：
 */
public class HeartBeatBean {
    private int cdma_sync;
    private int mp_state;
    private String id;
    private int temp;

    public int getCdma_sync() {
        return cdma_sync;
    }

    public void setCdma_sync(int cdma_sync) {
        this.cdma_sync = cdma_sync;
    }

    public int getMp_state() {
        return mp_state;
    }

    public void setMp_state(int mp_state) {
        this.mp_state = mp_state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }
}
