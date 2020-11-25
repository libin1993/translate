package com.doit.net.bean;

import java.util.List;

/**
 * Author：Libin on 2020/11/25 14:26
 * Email：1993911441@qq.com
 * Describe：黑名单
 * */
public class BlackListBean {
    private String id;
    private List<String> namelist;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getNamelist() {
        return namelist;
    }

    public void setNamelist(List<String> namelist) {
        this.namelist = namelist;
    }
}
