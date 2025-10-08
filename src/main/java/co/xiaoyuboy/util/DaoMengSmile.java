package co.xiaoyuboy.util;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrSplitter;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import co.xiaoyuboy.daomenjava.h;
import co.xiaoyuboy.daomenjava.i;
import co.xiaoyuboy.entity.App;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * @Author: Smile
 * @Date: 2024-03-20 16:32
 * @Description:
 */
public class DaoMengSmile {
    /**
     * 密码加密
     *
     * @param password
     * @return
     */
    public String pwdEncrypt(String password) {
        String encryptedDataHex = "";
        try {
            // 设置加密秘钥
            String secretKeyHex = "353134333435373434453431353936433445343134393643";
            byte[] secretKey = hexToBytes(secretKeyHex);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey, "DESede");
            // 设置加密内容
            byte[] data = password.getBytes("UTF-8");
            // 加密
            byte[] encryptedData = encrypt(data, keySpec);
            encryptedDataHex = bytesToHex(encryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedDataHex.toUpperCase(Locale.ROOT);
    }

    // 加密
    private static byte[] encrypt(byte[] data, SecretKeySpec keySpec) throws Exception {
        Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    // 将16进制字符串转换为字节数组
    private static byte[] hexToBytes(String hexString) {
        int length = hexString.length();
        byte[] bytes = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return bytes;
    }

    // 将字节数组转换为16进制字符串
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 组装登录的请求体
     */
    public String getLoginData(String account, String pwd) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String version = App.version;
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> mapJson3 = new HashMap<>();
        mapJson3.put("account", account);
        mapJson3.put("pwd", this.pwdEncrypt(pwd));
        mapJson3.put("timestamp", timestamp);
        mapJson3.put("version", version);

        String token = getSignToken(mapJson3);
        mapJson3.put("signToken", token);

        String jsonHead = "";
        try {
            jsonHead = mapper.writeValueAsString(sortMapByKey(mapJson3));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonHead;
    }

    public static String getSignToken(Map<String, String> map) {
        Map<String, String> map2 = new HashMap<>();
        map2.put("version", map.get("version"));
        map2.put("timestamp", map.get("timestamp"));
        map2.put("pwd", map.get("pwd"));
        map2.put("account", map.get("account"));
        //这个i源码中扣的i.d方法(就是一个sha512加密)
        String d = i.d(map2);
        return d;
    }

    public String getMapToString(Map<String, String> map){
        ObjectMapper mapper = new ObjectMapper();
        String json = "";
        try {
            json = mapper.writeValueAsString(sortMapByKey(map));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("json转换异常"+map);
        }
        return json;
    }

    /**
     * 给键值对进行排序
     *
     * @param map
     * @return
     */
    private static Map<String, String> sortMapByKey(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return map;
        }
        // 使用 TreeMap 对键进行排序
        Map<String, String> sortedMap = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String key1, String key2) {
                return key1.compareTo(key2);
            }
        });
        sortedMap.putAll(map);
        return sortedMap;
    }

    /**
     * 加密请求体(传入需要加密的请求体)
     */
    public String getSignature(String data) {
        //随机16位字符串
        String randStr = RandomUtil.randomStringUpper(16);
        //ase的密钥
        String hexStr = HexUtil.encodeHexStr(randStr);
        String aseJiaMi = this.getAse(data, hexStr);
        try {
            String rsa = h.getRSA(randStr);
            String qingtiuti = this.aesRsa(aseJiaMi, rsa);
            String base64Data = Base64.getEncoder().encodeToString(qingtiuti.getBytes(StandardCharsets.UTF_8));
            //返回加密rsa加密的结果
            return "d=" + base64Data;
        } catch (Exception e) {
            throw new RuntimeException("rsa加密出现异常"+e.getMessage());
        }

    }

    /**
     * 拼接两段密钥 组成请求体
     *
     * @param aes
     * @param rsa
     * @return
     */
    private  String aesRsa(String aes, String rsa) {
        String[] strings = StrSplitter.splitByLength(aes, 60);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            if ((i + 1) == strings.length) {
                stringBuilder.append(strings[i]);
                stringBuilder.append(rsa);
            } else {
                stringBuilder.append(strings[i] + " ");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * @param plaintext 传入的文本
     * @param keyHex    需要加密的密钥
     * @return 加密结果
     */
    private String getAse(String plaintext, String keyHex) {
//        System.out.println("加密方法输入文本---->"+plaintext);
//        System.out.println("加密方法输入key---->"+keyHex);
//        String ivHex = "39363138393133313230313132303130";
        String ivHex = "31363238303932313231333132323133";
        byte[] keyBytes = hexStringToByteArray(keyHex);
        byte[] ivBytes = hexStringToByteArray(ivHex);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
        try {
            // 加密
            Cipher encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encryptedBytes = encryptCipher.doFinal(plaintext.getBytes());
            String encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);
            return encryptedText;
        } catch (Exception e) {
            throw new RuntimeException("ase加密出现异常");
        }
    }

    private static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] byteArray = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            byteArray[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return byteArray;
    }

    /**
     * 公共请求头
     *
     * @return
     */
    public  String getHead() {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> mapJson3 = new HashMap<>();
        mapJson3.put("channelName", "dmkj_Android");
        mapJson3.put("countryCode", "CN");
        mapJson3.put("createTime", "1697851651123");
        mapJson3.put("device", "dmkj_Android");
        mapJson3.put("channelName", "Redmi 23013RK75C");
        mapJson3.put("hardware", "qcom");
        mapJson3.put("jPushId", "");
        mapJson3.put("modifyTime", "1697877932733");
        mapJson3.put("operator", "%E4%B8%AD%E5%9B%BD%E7%A7%BB%E5%8A%A8");
        mapJson3.put("screenResolution", "1440-3080");
        DateTime date = DateUtil.date();
        long time2 = date.getTime();
        mapJson3.put("startTime", String.valueOf(time2));
        mapJson3.put("sysVersion", "Android 33 13");
        mapJson3.put("system", "Android 33 13");
        mapJson3.put("sysVersion", "android");
        mapJson3.put("uuid", "020000000000");
        mapJson3.put("version", co.xiaoyuboy.entity.App.version);
        String jsonHead = "";
        try {
            jsonHead = mapper.writeValueAsString(mapJson3);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonHead;

    }
}
