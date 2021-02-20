package com.doit.net.bean;

/**
 * Author：Libin on 2021/2/19 14:59
 * Email：1993911441@qq.com
 * Describe：
 */
public class UBCStateBean {
    private String id;
    private String ubc_state;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUbc_state() {
        return ubc_state;
    }

    public void setUbc_state(String ubc_state) {
        this.ubc_state = ubc_state;
    }

    @Override
    public String toString() {
        return "UBCStateBean{" +
                "id='" + id + '\'' +
                ", ubc_state='" + ubc_state + '\'' +
                '}';
    }
}
