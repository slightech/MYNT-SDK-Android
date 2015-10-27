package com.slightech.sdkdemo.util;

/**
 * Created by Willard  on 2015/9/23.
 */
public class StringUtil {

    public static final String EMPTY = "";

    //-----------------------------------------------------------------------

    public static String byteArrayToHexString(byte[] bytes) {
        final StringBuilder sb = new StringBuilder(bytes.length);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        if (len <= 0) {
            return null;
        } else if (len % 2 == 1) {
            s = '0' + s;
            ++len;
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

}
