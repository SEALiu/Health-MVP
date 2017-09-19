package cn.sealiu.health.data.bean;

import java.io.Serializable;

/**
 * Created by liuyang
 * on 2017/9/18.
 */

public class Post implements Serializable {
    private int p_id;
    private String title;
    private int author_id;
    private String content;
    private String image;
    private int target_id;
    private String time;
    private int type_id;
    private User user;

    public Post(int p_id, String title, int author_id, String content, String image, int target_id, String time, int type_id, User user) {
        this.p_id = p_id;
        this.title = title;
        this.author_id = author_id;
        this.content = content;
        this.image = image;
        this.target_id = target_id;
        this.time = time;
        this.type_id = type_id;
        this.user = user;
    }

    public int getP_id() {
        return p_id;
    }

    public void setP_id(int p_id) {
        this.p_id = p_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(int author_id) {
        this.author_id = author_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getType_id() {
        return type_id;
    }

    public void setType_id(int type_id) {
        this.type_id = type_id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getTarget_id() {
        return target_id;
    }

    public void setTarget_id(int target_id) {
        this.target_id = target_id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
