package cn.sealiu.health.util;

import android.support.annotation.Nullable;

import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by liuyang
 * on 2017/9/12.
 */

public class Fun {
    public static String rightPad(String originalString, int length) {

        StringBuilder originalStringBuilder = new StringBuilder(originalString);
        while (originalStringBuilder.length() < length) {
            originalStringBuilder.append('0');
        }
        originalString = originalStringBuilder.toString();
        return originalString;
    }

    public static String leftPad(@Nullable String originalString, int length) {
        if (originalString == null) originalString = "";
        int strSize = originalString.length();
        int padLen = length - strSize;
        StringBuilder tempStr = new StringBuilder();
        for (int i = 0; i < padLen; i++) {
            tempStr.append('0');
        }

        return tempStr.append(originalString).toString();
    }


    /**
     * 根据选择的算法进行加密
     *
     * @param algorithm 可选：SHA-256，SHA-1，MD5
     * @param base      加密前内容
     * @return 加密后内容
     */
    public static String encode(String algorithm, String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (byte aHash : hash) {
                String hex = Integer.toHexString(0xff & aHash);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 根据开始时间和结束时间返回时间段内的时间集合
     *
     * @param beginDate
     * @param endDate
     * @return List
     */
    @SuppressWarnings("unchecked")
    public static List<String> getDatesBetweenTwoDate(Date beginDate, Date endDate) {
        Calendar begin = Calendar.getInstance();
        begin.setTime(beginDate);

        Calendar end = Calendar.getInstance();
        end.setTime(endDate);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        List<String> betweenDays = new ArrayList<>();

        while (true) {
            if (begin.before(end)) {
                betweenDays.add(df.format(begin.getTime()));
                begin.add(Calendar.DATE, 1);
            } else {
                break;
            }
        }

        return betweenDays;
    }
}
