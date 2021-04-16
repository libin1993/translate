package com.doit.net.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by Zxc on 2019/5/30.
 */
@Table(name = "BlackListInfo")
public class BlackListInfo {
    @Column(name = "id", isId = true)
    private int id;

    @Column(name = "imsi")
    private String imsi;

    @Column(name = "msisdn")
    private String msisdn;

    @Column(name = "remark")
    private String remark;

    @Column(name = "block")
    private int block;

    public BlackListInfo(String imsi, String msisdn, String remark, int block) {
        this.imsi = imsi;
        this.msisdn = msisdn;
        this.remark = remark;
        this.block = block;
    }


    public BlackListInfo() {
    }

    public String getImsi() {
        return imsi;
    }

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public String getRemark() {
        return remark;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
