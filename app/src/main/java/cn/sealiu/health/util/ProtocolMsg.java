package cn.sealiu.health.util;

import android.support.annotation.Nullable;

/**
 * Created by liuyang
 * on 2017/6/23.
 */
public class ProtocolMsg {
    // 响应数据类型
    // TODO: 2017/7/21 由于蓝牙模块的硬件的程序写错了，实时数据的类型写成了 "01" ，实际应该为："21"，以后需要改过来
    public static final String RS_DATA = "21";
    public static final String RS_STATUS_OR_PARAM = "22";
    public static final String RS_EXECUTE_STATUS = "23";
    public static final String RS_ACK = "24";

    static final String EOF = "ff";
    static final String END = "0d0a";

    // 请求数据类型
    public static final String RE_RT_DATA_START = "01";
    public static final String RE_RT_DATA_STOP = "02";
    public static final String RE_HISTORY_DATA = "03";
    public static final String RE_STATUS = "04";
    public static final String RE_SYNC_TIME = "05";
    public static final String RE_FIX_NORM = "06";
    public static final String RE_DEVICE_PARAM = "07";
    public static final String RE_CERTIFICATION = "08";

    public static final String DEVICE_PARAM_ENABLE_DATE = "0001";
    public static final String DEVICE_PARAM_CHANNEL_NUM = "0002";
    public static final String DEVICE_PARAM_USER_ID = "0003";

    public static final String DEVICE_PARAM_COMFORT_ONE = "0004";
    public static final String DEVICE_PARAM_COMFORT_TWO = "0005";
    public static final String DEVICE_PARAM_COMFORT_THREE = "0006";
    public static final String DEVICE_PARAM_COMFORT_FOUR = "0007";

    public static final String DEVICE_PARAM_SLOPE = "0008";
    public static final String DEVICE_PARAM_OFFSET = "0009";
    public static final String DEVICE_PARAM_SAMPLING_RATE = "000A";
    public static final String DEVICE_PARAM_CHANNEL_ONE = "000B";
    public static final String DEVICE_PARAM_CHANNEL_TWO = "000C";
    public static final String DEVICE_PARAM_CHANNEL_THREE = "000D";
    public static final String DEVICE_PARAM_CHANNEL_FOUR = "000E";
    public static final String DEVICE_PARAM_DOCTOR_ID = "000F";
    public static final String DEVICE_PARAM_HIGH_MID = "0010";
    public static final String DEVICE_PARAM_LOW_MID = "0011";

    // 执行结果
    public static final String EXECUTE_SUCCESS = "00";
    public static final String EXECUTE_FAILED_WRONG_UID = "01";
    public static final String EXECUTE_FAILED_NUMBER_LIMIT = "02";
    public static final String EXECUTE_FAILED_UID_EXIST = "03";
    public static final String EXECUTE_FAILED_WRONG_MID = "04";

    // 状态类型
    public static final String RS_STATUS = "01";
    public static final String RS_PARAM = "02";

    static String rightPad(String originalString, int length) {

        StringBuilder originalStringBuilder = new StringBuilder(originalString);
        while (originalStringBuilder.length() < length) {
            originalStringBuilder.append('0');
        }
        originalString = originalStringBuilder.toString();
        return originalString;
    }

    static String leftPad(@Nullable String originalString, int length) {
        if (originalString == null) originalString = "";
        int strSize = originalString.length();
        int padLen = length - strSize;
        StringBuilder tempStr = new StringBuilder();
        for (int i = 0; i < padLen; i++) {
            tempStr.append('0');
        }

        return tempStr.append(originalString).toString();
    }
}
