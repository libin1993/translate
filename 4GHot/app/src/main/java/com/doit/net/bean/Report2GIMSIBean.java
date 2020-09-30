package com.doit.net.bean;

import java.util.List;

/**
 * Author：Libin on 2020/9/24 14:28
 * Email：1993911441@qq.com
 * Describe：imsi上报
 */
public class Report2GIMSIBean {
    private String id;
    private List<List<String>> imsilist;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<List<String>> getImsilist() {
        return imsilist;
    }

    public void setImsilist(List<List<String>> imsilist) {
        this.imsilist = imsilist;
    }

}
