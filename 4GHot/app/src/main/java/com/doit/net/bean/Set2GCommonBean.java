package com.doit.net.bean;

import java.util.List;

/**
 * Author：Libin on 2020/9/23 15:38
 * Email：1993911441@qq.com
 * Describe：2G设置基础环境参数
 */
public class Set2GCommonBean extends Base2GBean<Set2GCommonBean.Params> {

    public static class Params{
        private String boardid;
        private String datetime;
        private String maxtrans;
        private List<String> callnum;


        public Params() {
        }

        public String getBoardid() {
            return boardid;
        }

        public void setBoardid(String boardid) {
            this.boardid = boardid;
        }

        public String getDatetime() {
            return datetime;
        }

        public void setDatetime(String datetime) {
            this.datetime = datetime;
        }

        public String getMaxtrans() {
            return maxtrans;
        }

        public void setMaxtrans(String maxtrans) {
            this.maxtrans = maxtrans;
        }

        public List<String> getCallnum() {
            return callnum;
        }

        public void setCallnum(List<String> callnum) {
            this.callnum = callnum;
        }
    }
}
