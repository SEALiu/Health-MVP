package cn.sealiu.health.data.bean;

import android.support.annotation.Nullable;

/**
 * Created by liuyang
 * on 2017/9/12.
 */

public class User {
    @Nullable
    private final String userUid;

    @Nullable
    private final String username;

    @Nullable
    private final String typeId;

    @Nullable
    private final String userId;

    @Nullable
    private final Boolean logged;

    @Nullable
    private final String phone;

    @Nullable
    private final String pwd;

    @Nullable
    private String mid;

    public User(@Nullable String userUid, @Nullable String username, @Nullable String typeId,
                @Nullable String userId, @Nullable Boolean logged, @Nullable String phone,
                @Nullable String pwd, @Nullable String mid) {
        this.userUid = userUid;
        this.username = username;
        this.typeId = typeId;
        this.userId = userId;
        this.logged = logged;
        this.phone = phone;
        this.pwd = pwd;
        this.mid = mid;
    }

    @Nullable
    public String getUserUid() {
        return userUid;
    }

    @Nullable
    public String getUsername() {
        return username;
    }

    @Nullable
    public String getTypeId() {
        return typeId;
    }

    @Nullable
    public String getUserId() {
        return userId;
    }

    @Nullable
    public Boolean getLogged() {
        return logged;
    }

    @Nullable
    public String getPhone() {
        return phone;
    }

    @Nullable
    public String getPwd() {
        return pwd;
    }

    @Nullable
    public String getMid() {
        return mid;
    }

    public void setMid(@Nullable String mid) {
        this.mid = mid;
    }
}
