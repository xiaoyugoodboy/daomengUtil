package co.xiaoyuboy.daomenjava;

/**
 * @author Smile
 * @date 2023-10-23 12:11
 */
import com.alibaba.fastjson.JSON;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class i {
    static String a = "SHA512";

    public static String a(byte[] bArr) {
        String str = "";
        for (byte b : bArr) {
            String hexString = Integer.toHexString(b & 255);
            if (hexString.length() == 1) {
                str = str + "0";
            }
            str = str + hexString;
        }
        return str;
    }

    private static String b(String str) {
        String str2 = new String();
        for (int i = 1; i < str.length(); i += 2) {
            str2 = str2 + str.charAt(i);
        }
        return str2;
    }

    static String c(String str) {
        String str2 = new String();
        for (int i = 0; i < str.length(); i += 2) {
            str2 = str2 + str.charAt(i);
        }
        return str2;
    }

    public static String d(Map<String, String> map) {
        TreeMap treeMap = new TreeMap();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            treeMap.put(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
        }
        byte[] bytes= JSON.toJSONString(treeMap).getBytes();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            messageDigest.update(bytes);
            return r(c(b(a(messageDigest.digest()))));
        } catch (Exception unused) {
            return null;
        }
    }

    public static void main(String[] args) {
        Map<String,String> map=new HashMap<>();
        map.put("version","9.9.9");
        map.put("timestamp","1700181133195");
        //String s = JSON.toJSONString(map);
        //--->752F5FB2DF483119A88E01DF5C098150
        //--->4C8EF20250169D304AD87ED07CFE1502
        String d = d(map);
        System.out.println(d);
    }
    public static String r(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
            byte[] digest = messageDigest.digest();
            for (int i = 0; i < digest.length; i++) {
                if (Integer.toHexString(digest[i] & 255).length() == 1) {
                    stringBuffer.append("0");
                    stringBuffer.append(Integer.toHexString(digest[i] & 255));
                } else {
                    stringBuffer.append(Integer.toHexString(digest[i] & 255));
                }
            }
            return stringBuffer.toString().toUpperCase();
        } catch (Exception unused) {
            throw new RuntimeException();
        }
    }
}