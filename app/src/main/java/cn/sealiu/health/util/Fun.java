package cn.sealiu.health.util;

import android.support.annotation.Nullable;

import java.security.MessageDigest;

/**
 * Created by liuyang
 * on 2017/9/12.
 */

public class Fun {
    public static String leftPad(@Nullable String originalString, int length, char padCharacter) {
        if (originalString == null) originalString = "";
        int strSize = originalString.length();
        int padLen = length - strSize;
        StringBuilder tempStr = new StringBuilder();
        for (int i = 0; i < padLen; i++) {
            tempStr.append(padCharacter);
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
}
