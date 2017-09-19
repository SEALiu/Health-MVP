package cn.sealiu.health.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by liuyang
 * on 2017/6/23.
 */

public class BoxRequestProtocol extends ProtocolMsg {

    public static final String DEVICE_START_DATA = "0001";
    public static final String DEVICE_CHANNEL_NUM = "0002";
    public static final String SENSOR_PLACE_NAME = "0003";
    public static final String USER_COMFOR_RANGE = "0004";
    public static final String RATIO = "0005";
    public static final String USER_ID = "0006";
    public static final String SENSOR_RATE = "0007";
    private static final int DATA_LEN = 18;
    private static final int TEST_DATA_LEN = 26;

    /**
     * 将String类型的报文转化成为 byte[]
     *
     * @param protocolStr String 报文
     * @return byte[]
     */
    public static byte[] convertHex2Bytes(String protocolStr) {
        // !!!报文中的字母必须是大写!!!
        protocolStr = protocolStr.toUpperCase();
        int len = (protocolStr.length() / 2);
        byte[] result = new byte[len];
        char[] chars = protocolStr.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) ((byte) "0123456789ABCDEF".indexOf(chars[pos]) << 4 | (byte) "0123456789ABCDEF".indexOf(chars[pos + 1]));
        }
        return result;
    }

    /**
     * 封装协议
     *
     * @param data 数据信息 9个字节
     * @param uid 用户的id （32位，4个字节）
     * @return 完整的待发送协议数据
     */
    public static String boxProtocol(String data, @NonNull String uid) {
        // 包头+数据信息+用户ID+包尾+指令结束
        return EOF + data + leftPad(uid, 8, '0') + EOF + END;
    }

    public static String boxProtocolOnlyForTest(String data) {
        // 包头+数据信息+包尾+指令结束
        return EOF + data + EOF + END;
    }

    /**
     * 封装开始实时数据上传
     *
     * @return 数据包 0x01
     */
    public static String boxStartUpload() {
        return rightPad(RE_RT_DATA_START, DATA_LEN, '0');
    }

    /**
     * 封装停止实时数据上传
     *
     * @return 数据包 0x02
     */
    public static String boxStopUpload() {
        return rightPad(RE_RT_DATA_STOP, DATA_LEN, '0');
    }

    /**
     * 封装请求历史数据
     *
     * @param date 指定日期
     * @return 数据包 0x03
     */
    public static String boxRequestHistoryData(Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = df.format(date);
        String[] dataArray = dateStr.split("-");

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            String item = dataArray[i];

            dataArray[i] = leftPad(
                    Integer.toHexString(Integer.parseInt(item)),
                    i == 0 ? 4 : 2,
                    '0'
            );

            result.append(dataArray[i]);
        }

        return rightPad(RE_HISTORY_DATA + result.toString(), DATA_LEN, '0');
    }

    /**
     * 封装请求设备状态信息
     *
     * @return 数据包 0x04
     */
    public static String boxRequestStatus() {
        return rightPad(RE_STATUS, DATA_LEN, '0');
    }

    /**
     * 封装时间同步数据包
     *
     * @param calendar 当前时间，此参数暴露出来是为了方便测试
     * @return 数据包 0x05
     */
    public static String boxSyncTime(Calendar calendar) {


        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
        String dateStr = df.format(calendar.getTime()) + "-" + calendar.get(Calendar.DAY_OF_WEEK);
        String[] dataArray = dateStr.split("-");


        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            String item = dataArray[i];

            dataArray[i] = leftPad(
                    Integer.toHexString(Integer.parseInt(item)),
                    i == 0 ? 4 : 2,
                    '0'
            );

            result.append(dataArray[i]);
        }

        return rightPad(RE_SYNC_TIME + result.toString(), DATA_LEN, '0');
    }

    public static String boxSyncTime() {
        Calendar calendar = Calendar.getInstance();
        return boxSyncTime(calendar);
    }

    /**
     * 封装请求定标数据
     *
     * @return 数据包 0x06
     */
    public static String boxRequestFixNorm() {
        return rightPad(RE_FIX_NORM, DATA_LEN, '0');
    }

    /**
     * 封装请求设备参数
     *
     * @param paramName 参数类型，十六进制 (2个字节)          参数内容长度（字节）
     *                  设备启用日期：0x0001                   4
     *                  设备的采集通道数：0x0002                1
     *                  用户ID：0x0003                        4
     *                  用户的舒适度范围1：0x0004               2
     *                  用户的舒适度范围2：0x0005               2
     *                  用户的舒适度范围3：0x0006               2
     *                  用户的舒适度范围4：0x0007               2
     *                  传感器的压力值转换斜率：0x0008           2( X1000)
     *                  传感器的压力值转换斜率：0x0009           2( X1000)
     *                  采样率：0x000A                        4
     *                  传感器的位置名称1：0x000B               6
     *                  传感器的位置名称2：0x000C               6
     *                  传感器的位置名称3：0x000D               6
     *                  传感器的位置名称4：0x000E               6
     * @return 数据包 0x07
     */
    public static String boxRequestDeviceParam(String paramName, @Nullable String paramValue) {
        if (paramValue == null) {
            return rightPad(RE_DEVICE_PARAM + paramName, DATA_LEN, '0');
        }
        return rightPad(RE_DEVICE_PARAM + paramName + paramValue, DATA_LEN, '0');
    }

    /**
     * 封装用户认证数据包
     *
     * @param machineID 授机设备的机器ID最后4字节的内容 (8个十六进制字符)
     * @return 数据包 0x08
     */
    public static String boxRequestCertification(@NonNull String machineID) {
        return rightPad(RE_CERTIFICATION + machineID, DATA_LEN, '0');
    }


    //---------------------------------------------------------------------------
    //---------------------------------------------------------------------------

    /**
     * only used for testing
     *
     * @param i  sequence number
     * @param AA chanel 1's value
     * @param BB chanel 2's value
     * @param CC chanel 3's value
     * @param DD chanel 4's value
     * @return Response Protocol's Data "0x21"
     */
    public static String boxResponseDataOnlyForTest(String i, String AA, String BB, String CC, String DD) {
        Calendar now = Calendar.getInstance();
        String hour = String.valueOf(now.get(Calendar.HOUR_OF_DAY));
        String minute = String.valueOf(now.get(Calendar.MINUTE));
        String second = String.valueOf(now.get(Calendar.SECOND));

        String data = RS_DATA +
                leftPad(hour, 2, '0') +
                leftPad(minute, 2, '0') +
                leftPad(second, 2, '0') +
                i + AA + BB + CC + DD;
        return rightPad(data, TEST_DATA_LEN, '0');
    }

    /**
     * only used for testing
     *
     * @return Response Protocol's Data "0x22"
     */
    public static String boxResponseDeviceStatusOnlyForTest(String power, String storage, String[] statuses) {
        Calendar now = Calendar.getInstance();
        String hour = String.valueOf(now.get(Calendar.HOUR_OF_DAY));
        String minute = String.valueOf(now.get(Calendar.MINUTE));
        String second = String.valueOf(now.get(Calendar.SECOND));

        String data = RS_STATUS_OR_PARAM + RS_STATUS +
                leftPad(hour, 2, '0') +
                leftPad(minute, 2, '0') +
                leftPad(second, 2, '0') +
                power + storage + statuses[0] + statuses[1] + statuses[2] + statuses[3];

        return rightPad(data, TEST_DATA_LEN, '0');
    }

    /**
     * only used for testing
     *
     * @param RE_Type
     * @param status
     * @return
     */
    public static String boxResponseExecuteStatusOnlyForTest(@NonNull String RE_Type, @NonNull String status) {
        String data = RS_EXECUTE_STATUS + RE_Type + leftPad(status, 2, '0');
        return rightPad(data, TEST_DATA_LEN, '0');
    }
}