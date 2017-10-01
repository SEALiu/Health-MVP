package cn.sealiu.health.data.bean;

/**
 * Created by liuyang
 * on 2017/10/1.
 */

public class DatastatusBean {
    private String id;
    private String time;
    private int status;

    public DatastatusBean(String time, int status) {
        this.time = time;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
