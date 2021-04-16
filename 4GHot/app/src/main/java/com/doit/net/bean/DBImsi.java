package com.doit.net.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Author：Libin on 2021/1/5 14:35
 * Email：1993911441@qq.com
 * Describe：发送短信imsi
 */
@Table(name = "imsi")
public class DBImsi {
    @Column(name = "id", isId = true)
    private int id;

    @Column(name = "imsi")
    private String imsi;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public DBImsi(String imsi) {
        this.imsi = imsi;
    }

    public DBImsi() {
    }
}
