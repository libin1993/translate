package com.doit.net.bean;

import java.util.List;

/**
 * Author：Libin on 2021/1/5 13:32
 * Email：1993911441@qq.com
 * Describe：
 */
public class SendSmsBean {
    private String id;
    private String action;
    private List<String> imsi;
    private String smsnum;
    private String period;
    private String content;
    private String interval;

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<String> getImsi() {
        return imsi;
    }

    public void setImsi(List<String> imsi) {
        this.imsi = imsi;
    }

    public String getSmsnum() {
        return smsnum;
    }

    public void setSmsnum(String smsnum) {
        this.smsnum = smsnum;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
