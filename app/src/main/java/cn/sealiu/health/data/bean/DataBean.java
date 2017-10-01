package cn.sealiu.health.data.bean;

/**
 * Created by liuyang
 * on 2017/8/1.
 */

public class DataBean {
    private String id;
    private String mid;
    private int sequence;
    private String aa;
    private String bb;
    private String cc;
    private String dd;
    private String time;

    public DataBean(String mid, int sequence, String aa, String bb, String cc, String dd, String time) {
        this.mid = mid;
        this.sequence = sequence;
        this.aa = aa;
        this.bb = bb;
        this.cc = cc;
        this.dd = dd;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getAa() {
        return aa;
    }

    public void setAa(String aa) {
        this.aa = aa;
    }

    public String getBb() {
        return bb;
    }

    public void setBb(String bb) {
        this.bb = bb;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getDd() {
        return dd;
    }

    public void setDd(String dd) {
        this.dd = dd;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
