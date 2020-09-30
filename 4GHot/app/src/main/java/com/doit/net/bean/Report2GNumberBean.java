package com.doit.net.bean;

import java.util.List;

/**
 * Author：Libin on 2020/9/24 14:25
 * Email：1993911441@qq.com
 * Describe：翻译上报
 */
public class Report2GNumberBean {
    private String id;
    private List<List<String>> inlist;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<List<String>> getInlist() {
        return inlist;
    }

    public void setInlist(List<List<String>> inlist) {
        this.inlist = inlist;
    }
}
