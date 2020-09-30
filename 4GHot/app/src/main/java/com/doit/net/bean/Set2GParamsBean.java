package com.doit.net.bean;

import java.util.List;

/**
 * Author：Libin on 2020/9/23 16:19
 * Email：1993911441@qq.com
 * Describe：2G设置基础环境参数
 */
public class Set2GParamsBean extends Base2GBean<Set2GParamsBean.Params> {
    public static class Params {

        private String boardid;
        private String carrierid;
        private String mcc;
        private String mnc;
        private String lac;
        private String opmode;
        private String dlattn;
        private String ulattn;
        private String sniff;

        //GSM专有参数
        private String ci;
        private String cro;
        private String cfgmode;
        private String fcn;


        //CDMA专有参数
        private List<String> fcnmode;


        private boolean rfState;

        public boolean isRfState() {
            return rfState;
        }

        public void setRfState(boolean rfState) {
            this.rfState = rfState;
        }


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

        public String getMcc() {
            return mcc;
        }

        public void setMcc(String mcc) {
            this.mcc = mcc;
        }

        public String getMnc() {
            return mnc;
        }

        public void setMnc(String mnc) {
            this.mnc = mnc;
        }

        public String getLac() {
            return lac;
        }

        public void setLac(String lac) {
            this.lac = lac;
        }

        public String getOpmode() {
            return opmode;
        }

        public void setOpmode(String opmode) {
            this.opmode = opmode;
        }

        public String getDlattn() {
            return dlattn;
        }

        public void setDlattn(String dlattn) {
            this.dlattn = dlattn;
        }

        public String getUlattn() {
            return ulattn;
        }

        public void setUlattn(String ulattn) {
            this.ulattn = ulattn;
        }

        public String getSniff() {
            return sniff;
        }

        public void setSniff(String sniff) {
            this.sniff = sniff;
        }

        public String getCi() {
            return ci;
        }

        public void setCi(String ci) {
            this.ci = ci;
        }

        public String getCro() {
            return cro;
        }

        public void setCro(String cro) {
            this.cro = cro;
        }

        public String getCfgmode() {
            return cfgmode;
        }

        public void setCfgmode(String cfgmode) {
            this.cfgmode = cfgmode;
        }

        public String getFcn() {
            return fcn;
        }

        public void setFcn(String fcn) {
            this.fcn = fcn;
        }

        public List<String> getFcnmode() {
            return fcnmode;
        }

        public void setFcnmode(List<String> fcnmode) {
            this.fcnmode = fcnmode;
        }
    }
}
