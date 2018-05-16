package com.trc.android.common.util;

import java.security.MessageDigest;

/**
 * @author HanTuo on 2017/5/31.
 */

public class Md5Util {
    public static String getMd5(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
            byte[] byteArray = messageDigest.digest();
            StringBuilder md5StrBuff = new StringBuilder();

            for (byte aByteArray : byteArray) {
                if (Integer.toHexString(255 & aByteArray).length() == 1) {
                    md5StrBuff.append("0").append(Integer.toHexString(255 & aByteArray));
                } else {
                    md5StrBuff.append(Integer.toHexString(255 & aByteArray));
                }
            }

            return md5StrBuff.toString();
        } catch (Exception e) {
            return str.hashCode() + "_" + str.length();
        }
    }

}
