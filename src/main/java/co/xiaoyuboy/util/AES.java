package co.xiaoyuboy.util;

/**
 * @Author: Smile
 * @Date: 2023-11-11 21:46
 * @Description:
 */

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class AES {
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String AES_KEY = "0123456789abcdef"; // 16字节的密钥

    public static String encrypt(String plaintext) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String ciphertext) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("密钥解密异常，退出程序---->"+ciphertext);
        }
    }
    public static boolean isExpired(String timeString) {
        LocalDate time = LocalDate.parse(timeString, DateTimeFormatter.ISO_DATE);
        LocalDate currentDate = LocalDate.now();
        return currentDate.isBefore(time)|| currentDate.isEqual(time);
    }
    public static void main(String[] args) {
        String plaintext = "2023-11-30"; // 要加密的时间字符串

        // 加密
        String encryptedText = encrypt(plaintext);
        System.out.println("Encrypted Text: " + encryptedText);

        // 解密
        String decryptedText = decrypt(encryptedText);
        System.out.println("Decrypted Text: " + decryptedText);

        // 判断解密后的时间是否超过当前时间
        boolean isExpired = isExpired(decryptedText);

        if (isExpired) {
            System.out.println("没有到期");

        } else {
            System.out.println("到期了");
            // 继续执行其他操作
        }
    }
}
