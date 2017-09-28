package cn.sealiu.health.data.response;

/**
 * Created by liuyang
 * on 2017/9/12.
 */

public class LoginAndRegisterResponse {
    private String status;
    private String result;
    private String userId;
    private String typeId;
    private String userUid;
    private String userMid;
    private String first_calibtime;
    private String comfort_A;
    private String comfort_B;
    private String comfort_C;
    private String comfort_D;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getUserMid() {
        return userMid;
    }

    public void setUserMid(String userMid) {
        this.userMid = userMid;
    }

    public String getFirst_calibtime() {
        return first_calibtime;
    }

    public void setFirst_calibtime(String first_calibtime) {
        this.first_calibtime = first_calibtime;
    }

    public String getComfort_A() {
        return comfort_A;
    }

    public void setComfort_A(String comfort_A) {
        this.comfort_A = comfort_A;
    }

    public String getComfort_B() {
        return comfort_B;
    }

    public void setComfort_B(String comfort_B) {
        this.comfort_B = comfort_B;
    }

    public String getComfort_C() {
        return comfort_C;
    }

    public void setComfort_C(String comfort_C) {
        this.comfort_C = comfort_C;
    }

    public String getComfort_D() {
        return comfort_D;
    }

    public void setComfort_D(String comfort_D) {
        this.comfort_D = comfort_D;
    }
}
