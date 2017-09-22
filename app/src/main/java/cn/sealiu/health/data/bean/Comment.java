package cn.sealiu.health.data.bean;

/**
 * Created by liuyang
 * on 2017/8/2.
 */

public class Comment {
    private String content;
    private String time;
    private User user;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
