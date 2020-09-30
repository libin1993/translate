package com.doit.net.bean;

import java.util.List;

/**
 * Author：Libin on 2020/9/24 10:56
 * Email：1993911441@qq.com
 * Describe：
 */
public class Get2GCommonResponseBean extends Base2GBean<Get2GCommonResponseBean.Params> {
    public static class Params{
        private String boardid;
        private String carrierid;
        private String swver;
        private String ntcver;
        private List<RunState> runstate;
        private String datetime;
        private String maxtrans;
        private List<String> callnum;
        private List<String> fcn;

        public String getCarrierid() {
            return carrierid;
        }

        public void setCarrierid(String carrierid) {
            this.carrierid = carrierid;
        }

        public String getBoardid() {
            return boardid;
        }

        public void setBoardid(String boardid) {
            this.boardid = boardid;
        }

        public String getSwver() {
            return swver;
        }

        public void setSwver(String swver) {
            this.swver = swver;
        }

        public String getNtcver() {
            return ntcver;
        }

        public void setNtcver(String ntcver) {
            this.ntcver = ntcver;
        }

        public List<RunState> getRunstate() {
            return runstate;
        }

        public void setRunstate(List<RunState> runstate) {
            this.runstate = runstate;
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

        public List<String> getFcn() {
            return fcn;
        }

        public void setFcn(List<String> fcn) {
            this.fcn = fcn;
        }

        public static class RunState{
            private String c1rf;
            private String c2rf;
            private String sync;
            private String c3rf;

            public String getC1rf() {
                return c1rf;
            }

            public void setC1rf(String c1rf) {
                this.c1rf = c1rf;
            }

            public String getC2rf() {
                return c2rf;
            }

            public void setC2rf(String c2rf) {
                this.c2rf = c2rf;
            }

            public String getSync() {
                return sync;
            }

            public void setSync(String sync) {
                this.sync = sync;
            }

            public String getC3rf() {
                return c3rf;
            }

            public void setC3rf(String c3rf) {
                this.c3rf = c3rf;
            }
        }
    }
}
