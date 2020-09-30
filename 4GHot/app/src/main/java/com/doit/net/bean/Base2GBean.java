package com.doit.net.bean;


import java.util.List;

/**
 * Author：Libin on 2020/9/23 15:34
 * Email：1993911441@qq.com
 * Describe：配置2G
 */
public class Base2GBean<T> {
    private String id;
    private List<T> params;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<T> getParams() {
        return params;
    }

    public void setParams(List<T> params) {
        this.params = params;
    }
}
