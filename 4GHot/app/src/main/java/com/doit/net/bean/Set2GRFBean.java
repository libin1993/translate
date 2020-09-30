package com.doit.net.bean;

/**
 * Author：Libin on 2020/9/24 09:35
 * Email：1993911441@qq.com
 * Describe：2G开关射频
 */
public class Set2GRFBean extends Base2GBean<Set2GRFBean.Params>{
    public static class Params{
        private String boardid;
        private String carrierid;
        private String state;

        public String getBoardid() {
            return boardid;
        }

        public void setBoardid(String boardid) {
            this.boardid = boardid;
        }

        public String getCarrierid() {
            return carrierid;
        }

        public void setCarrierid(String carrierid) {
            this.carrierid = carrierid;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }
}
