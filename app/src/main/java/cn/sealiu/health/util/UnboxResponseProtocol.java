package cn.sealiu.health.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liuyang
 * on 2017/6/23.
 *
 * 响应报文：
 * ----------------------------------------------------------------
 * |         |    包头  |    数据包    |    包尾     |   0x0d 0x0a   |
 * ----------------------------------------------------------------
 * |   长度   |     1   |      13     |     1       |     2        |
 * ----------------------------------------------------------------
 *
 * 数据部分：
 *
 * 数据类型 = 0x21 实时数据／历史数据
 * ------------------------------------------------------------------------
 * |         |  数据类型  | 时 | 分 | 秒 | 序号 | 通道1 | 通道2 | 通道3 | 通道4 |
 * ------------------------------------------------------------------------
 * |   长度   |     1     | 1 | 1  | 1 |  1   |  2   |   2   |  2   |  2   |
 * ------------------------------------------------------------------------
 *
 * 数据类型 = 0x22
 * 状态类型 = 0x01 设备状态
 * -----------------------------------------------------------------------------------
 * |         |  数据类型  |   状态类型    | 时 | 分 | 秒 | 电池电量 | SD卡容量 |  系统日期 |
 * -----------------------------------------------------------------------------------
 * |   长度   |     1    |      1       |  1 | 1  | 1 |    1    |    2    |     4    |
 * -----------------------------------------------------------------------------------
 *
 * 数据类型 = 0x22
 * 状态类型 = 0x02 设备参数
 * -----------------------------------------------------------
 * |         |  数据类型  |   状态类型    | 参数类型 |  参数内容  |
 * -----------------------------------------------------------
 * |   长度   |     1    |      1       |    2    |     6     |
 * -----------------------------------------------------------
 *
 * 数据类型 = 0x23
 * --------------------------------------------------------------
 * |         |  数据类型  |   指令类型    | 结果 | 子指令  |  其他   |
 * --------------------------------------------------------------
 * |   长度   |     1    |      1       |  1  |   1    |    9    |
 * --------------------------------------------------------------
 *
 */
public class UnboxResponseProtocol extends ProtocolMsg {
    private boolean isValidate;
    private String msg;
    private String type;
    private String time;

    public UnboxResponseProtocol(String msg) {
        // check msg format
        Pattern p = Pattern.compile("FF[\\dA-F]{26}FF0D0A");
        Matcher matcher = p.matcher(msg.toUpperCase());

        if (matcher.find()) {
            this.isValidate = true;
            this.msg = msg.substring(matcher.start(), matcher.end());
        } else {
            this.isValidate = false;
        }

        this.msg = msg;
        if (msg.length() >= 4) this.type = msg.substring(2, 4);
        else this.type = "";
    }

    public String getMsg() {
        return msg;
    }

    public boolean getIsValidate() {
        return isValidate;
    }

    /**
     * 获取响应报文的类型
     *
     * @return String 数据类型
     */
    public String getType() {
        return this.type;
    }

    public boolean isStatus() {
        return this.msg.substring(4, 6).equals(RS_STATUS);
    }

    public boolean isParam() {
        return this.msg.substring(4, 6).equals(RS_PARAM);
    }

    /**
     * 获取"0x21"类型响应报文的时间
     *
     * @return String 时间信息 HH:mm:ss
     */

    public String getDataTime() {
        if (!type.equals(RS_DATA)) return null;
        this.time = getTime(4);
        return this.time;
    }

    /**
     * 获取"0x22"类型，状态类型为："0x01" 的响应报文的时间
     *
     * @return String 时间信息 HH:mm:ss
     */
    public String getDeviceStatusTime() {
        if (!type.equals(RS_STATUS_OR_PARAM)) return null;
        this.time = getTime(6);
        return this.time;
    }

    /**
     * 获取响应报文的时间
     *
     * @param offset 时间字段在报文中的偏移位置
     * @return String 时间信息 HH:mm:ss
     */
    private String getTime(int offset) {
        int hour = Integer.parseInt(msg.substring(offset, offset + 2), 16);
        int minute = Integer.parseInt(msg.substring(offset + 2, offset + 4), 16);
        int second = Integer.parseInt(msg.substring(offset + 4, offset + 6), 16);

        return Fun.leftPad(hour + "", 2) + ":" +
                Fun.leftPad(minute + "", 2) + ":" +
                Fun.leftPad(second + "", 2);
    }

    /**
     * 获取"0x21"类型响应报文中的数据序号（序号：用以区别相同时间的多个报文）
     *
     * @return String 序号
     */
    public String getSequenceNum() {
        if (!this.type.equals(RS_DATA)) return "";
        return this.msg.substring(10, 12);
    }

    /**
     * 获取"0x21"类型响应报文中的4个通道的电压值
     *
     * @return String[] 4个通道的电压值
     */
    public String[] getData() {
        if (!this.type.equals(RS_DATA)) return new String[]{};

        int offset = 12;
        String[] result = new String[4];
        result[0] = msg.substring(offset, offset + 4);
        result[1] = msg.substring(offset + 4, offset + 8);
        result[2] = msg.substring(offset + 8, offset + 12);
        result[3] = msg.substring(offset + 12, offset + 16);

        return result;
    }

    /**
     * 获取"0x22"，状态类型为："0x01"类型响应报文中的电池剩余量
     *
     * @return String 电池剩余量
     */
    public String getPowerLeft() {
        if (!this.type.equals(RS_STATUS_OR_PARAM)) return "";
        if (!this.msg.substring(4, 6).equals(RS_STATUS)) return "";

        return this.msg.substring(12, 14);
    }

    /**
     * 获取"0x22"，状态类型为："0x01"类型响应报文中的 SD 卡剩余量
     *
     * @return String SD卡剩余量
     */
    public String getStorageLeft() {
        if (!this.type.equals(RS_STATUS_OR_PARAM)) return "";
        if (!this.msg.substring(4, 6).equals(RS_STATUS)) return "";

        return this.msg.substring(14, 18);
    }

    public String getSystemTime() {
        if (!this.type.equals(RS_STATUS_OR_PARAM)) return "";
        if (!this.msg.substring(4, 6).equals(RS_STATUS)) return "";

        return this.msg.substring(18, 26);
    }

//    /**
//     * 获取"0x22"，状态类型为："0x01"类型响应报文中的传感器状态
//     *
//     * @return String[] 4个传感器状态
//     */
//    public String[] getSensorStatus() {
//        if (!this.type.equals(RS_STATUS_OR_PARAM)) return new String[]{};
//        if (!this.msg.substring(4, 6).equals(RS_STATUS)) return new String[]{};
//
//        int offset = 18;
//        String[] result = new String[4];
//        result[0] = msg.substring(offset, offset + 2);
//        result[1] = msg.substring(offset + 2, offset + 4);
//        result[2] = msg.substring(offset + 4, offset + 6);
//        result[3] = msg.substring(offset + 6, offset + 8);
//
//        return result;
//    }

    /**
     * 获取"0x22"，状态类型为："0x02"类型响应报文中的参数类型
     */
    public String getParamType() {
        if (!this.type.equals(RS_STATUS_OR_PARAM)) return "";
        if (!this.msg.substring(4, 6).equals(RS_PARAM)) return "";

        return msg.substring(6, 10);
    }

    /**
     * 获取"0x22"，状态类型为："0x02"类型响应报文中的参数内容
     */
    public String getParamContent() {
        if (!this.type.equals(RS_STATUS_OR_PARAM)) return "";
        if (!this.msg.substring(4, 6).equals(RS_PARAM)) return "";

        return msg.substring(10, 28);
    }

    /**
     * 获取"0x23"类型报文中响应的请求指令类型。
     */
    public String getExecuteResultType() {
        if (!this.type.equals(RS_EXECUTE_STATUS) && !this.type.equals(RS_ACK)) return "";
        return this.msg.substring(4, 6);
    }

    /**
     * 获取"0x23"类型报文中响应的指令执行结果。
     */
    public String getExecuteResult() {
        if (!this.type.equals(RS_EXECUTE_STATUS) && !this.type.equals(RS_ACK)) return "";
        return this.msg.substring(6, 8);
    }

    /**
     * 获取"0x23"类型报文中响应的指令的"其他"字段内容。
     * 例如：设备认证返回的设备id
     */
    public String getExecuteBindedData() {
        // TODO: 2017/9/28 uncomment code below when 0x23 08 is done
        //if (!this.type.equals(RS_EXECUTE_STATUS)) return "";
        return this.msg.substring(10, 28);
    }

    /**
     * 由于同一个请求指令中有多个不同类型的返回结果，所以需要一个子类型加以区分。
     *
     * 获取"0x23" 的子类型
     */
    public String getChildExecuteResultType() {
        if (!this.type.equals(RS_EXECUTE_STATUS)) return "";
        return this.msg.substring(8, 10);
    }

    /**
     * 获取"0x23" 返回的数据
     */
    public String getExecuteResultValue() {
        if (!this.type.equals(RS_EXECUTE_STATUS)) return "";
        return this.msg.substring(8, 28);
    }
}
