package cn.sealiu.health.data.bean;

/**
 * Created by liuyang
 * on 2017/9/12.
 */

public class BaseResponse {
    private String status;
    private String result;
    private String userId;
    private String typeId;
    private String userUid;
    // TODO: 2017/9/14 设备ID 参数名称修改与接口一致
    private String mid;

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

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }
}
