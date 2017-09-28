package cn.sealiu.health.data.response;

import cn.sealiu.health.data.bean.Message;

/**
 * Created by liuyang
 * on 2017/8/3.
 */

public class MessageResponse {
    private Message[] PatientList;

    public Message[] getPatientList() {
        return PatientList;
    }

    public void setPatientList(Message[] patientList) {
        PatientList = patientList;
    }
}
