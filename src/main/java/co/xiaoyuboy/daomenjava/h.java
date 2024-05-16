package co.xiaoyuboy.daomenjava;

/**
 * @author Smile
 * @date 2023-10-23 8:08
 */
//import android.util.Base64;

import cn.hutool.core.codec.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;

public class h {
    public static final String a = "RSA/NONE/PKCS1Padding";
    public static final String b = "BC";
    private static final int c = 117;
    public static final String d = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBANYRgHTdQpfzU2vS\n5w4/f6yPEj8PtpOLk9foMtFFbLhFM+Oj5BNqXXOIenIbOgRAXmIKVBevRUFMRGcx\nbjH5VMmDpMnszwFk09bJvbNG5l58jhhUSoM02BhiEryMs4LMVEV7yEfKAxMpcAsa\nH7hxJzm0ZQw/6CBm0pAtYD3uHf/VAgMBAAECgYBH8EnC/DhM6DC4o3+SgjRdwRbY\nqmco3lcoz5eETFhk9Jyje4hCHhSZptu/TPcKRrdxKxdnfjc/4ml6ZFor4SZklMag\n31cahDAHde+3GpAtt2P8xepbqB+D+MDg4wHTnt81uhVKJr5akpGFgxodvkveelLU\nqv/z+7aIntbaANlvYQJBAOzt6v7MJQYNDkE+CzY1TbOM6nK8KLGeJcFeyvLE/6Ay\nlMumQY7/Y5yHTs0RI+fhZEfNb+aZS/nGNfr2uQShdxsCQQDnTISpGzPVxf88EiY5\ntVb/SmgSA4CENl3XatXAlXHGdhI4DGbMuRfH6OusAOakIMS1BAxVWUGlXuBpSDgb\nMiPPAkEAnnNaxn5WgF542w8YRm+NgHfMT2EgwfCnBVRU29j9r/BD7JJKo8yOnJ8t\nBx3ganIlM5Pjs09wLSHu9eDMyZkGYwJBANwX6dxosChpYfgtZQZg0knW6bTKdE5a\n1W0YkhHRoxQwYiHHW0LPlht6PlfL+6yOKd/93CaS6m6MG4rTV+A5E+0CQFqyjhCp\neUvrQfgni/cju652ufyKExSWxLSidEvwdNhME8TilVf2ASeNBMetBrB7w5UL2AhM\nqE3EXZcAIdW/mGA=";
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static String getRSA(String str) throws Exception {
        //模数和指数
       // RSAPublicKey publicKey = h.i("92652871631204341195357466872992808065662015835732880708328284886580017627321199078591741852685782813532247261974373436405831450354108632662245873517339803934761530440560827499111923123508823201213276396529251311431870189656510952028838583868842047910210779487622718784950674684388777591676380577057730653297", "65537");
        RSAPublicKey publicKey = h.i("111602863222991102481735326647200948288032311528593963811533577327514848303466538610406051306025406098243393089983444437312893044780161636403121177652897602041270225936895701680484846699693780085858497152782257277015987809618250549268987149708345380138314168803776313533258899701102342689157482524625283184299", "65537");
        String f = h.f(publicKey, str);
        return f;
    }

    public static byte[] a(byte[] bArr, int i) {
        byte b2;
        byte[] bArr2 = new byte[i / 2];
        int i2 = 0;
        for (int i3 = 0; i3 < (i + 1) / 2; i3++) {
            int i4 = i2 + 1;
            bArr2[i3] = b(bArr[i2]);
            if (i4 >= i) {
                i2 = i4;
                b2 = 0;
            } else {
                i2 = i4 + 1;
                b2 = b(bArr[i4]);
            }
            bArr2[i3] = (byte) (b2 + (bArr2[i3] << 4));
        }
        return bArr2;
    }

    /* JADX WARN: Code restructure failed: missing block: B:16:0x001d, code lost:
        if (r3 <= 102) goto L13;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static byte b(byte b2) {
        int i;
        if (b2 < 48 || b2 > 57) {
            byte b3 = 65;
            if (b2 < 65 || b2 > 70) {
                b3 = 97;
                if (b2 >= 97) {
                }
            }
            i = (b2 - b3) + 10;
            return (byte) i;
        }
        i = b2 - 48;
        return (byte) i;
    }

    public static String c(byte[] bArr) {
        char[] cArr = new char[bArr.length * 2];
        for (int i = 0; i < bArr.length; i++) {
            char c2 = (char) (((bArr[i] & 240) >> 4) & 15);
            int i2 = i * 2;
            cArr[i2] = (char) (c2 > '\t' ? (c2 + 'A') - 10 : c2 + '0');
            char c3 = (char) (bArr[i] & 15);
            cArr[i2 + 1] = (char) (c3 > '\t' ? (c3 + 'A') - 10 : c3 + '0');
        }
        return new String(cArr);
    }

    public static String d(String str, RSAPrivateKey rSAPrivateKey) throws Exception {
        Cipher cipher = null;
        byte[][] j;
        Cipher.getInstance("RSA").init(2, rSAPrivateKey);
        byte[] bytes = str.getBytes();
        String str2 = "";
        for (byte[] bArr : j(a(bytes, bytes.length), rSAPrivateKey.getModulus().bitLength() / 8)) {
            str2 = str2 + new String(cipher.doFinal(bArr));
        }
        return str2;
    }

    public static String e(String str, RSAPublicKey rSAPublicKey) throws Exception {
        Cipher cipher = null;
        String[] k;
        Cipher.getInstance("RSA").init(1, rSAPublicKey);
        String str2 = "";
        for (String str3 : k(str, (rSAPublicKey.getModulus().bitLength() / 8) - 11)) {
            str2 = str2 + c(cipher.doFinal(str3.getBytes()));
        }
        return str2;
    }

    public static String f(PublicKey publicKey, String str) throws Exception {
        byte[] doFinal;
        Cipher cipher = Cipher.getInstance(a, b);
        cipher.init(1, publicKey);
        byte[] bytes = str.getBytes("UTF-8");
        int length = bytes.length;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i = 0;
        int i2 = 0;
        while (true) {
            int i3 = length - i;
            if (i3 > 0) {
                if (i3 > c) {
                    doFinal = cipher.doFinal(bytes, i, c);
                } else {
                    doFinal = cipher.doFinal(bytes, i, i3);
                }
                byteArrayOutputStream.write(doFinal, 0, doFinal.length);
                i2++;
                i = i2 * c;
            } else {
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.close();
//                return new String(Base64.encode(byteArray, 2));
//                return encodeWithLineLength(byteArray, 2);
                String encodedString = Base64.encode(byteArray);
                return encodedString;
            }
        }
    }
    public static String encodeWithLineLength(byte[] byteArray, int lineLength) {
        String encodedString = Base64.encode(byteArray);
        StringBuilder sb = new StringBuilder();
        int length = encodedString.length();
        for (int i = 0; i < length; i += lineLength) {
            int endIndex = Math.min(i + lineLength, length);
            sb.append(encodedString, i, endIndex);
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }


    public static HashMap<String, Object> g() throws NoSuchAlgorithmException {
        HashMap<String, Object> hashMap = new HashMap<>();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        KeyPair generateKeyPair = keyPairGenerator.generateKeyPair();
        hashMap.put("public", (RSAPublicKey) generateKeyPair.getPublic());
        hashMap.put("private", (RSAPrivateKey) generateKeyPair.getPrivate());
        return hashMap;
    }

    public static RSAPrivateKey h(String str, String str2) {
        try {
            return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new RSAPrivateKeySpec(new BigInteger(str), new BigInteger(str2)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static RSAPublicKey i(String str, String str2) {
        try {
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger(str), new BigInteger(str2)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    public static byte[][] j(byte[] bArr, int i) {
//        int length = bArr.length / i;
//        int length2 = bArr.length % i;
//        int i2 = length + (length2 != 0 ? 1 : 0);
//        byte[][] bArr2 = new byte[i2];
//        for (int i3 = 0; i3 < i2; i3++) {
//            byte[] bArr3 = new byte[i];
//            if (i3 == i2 - 1 && length2 != 0) {
//                System.arraycopy(bArr, i3 * i, bArr3, 0, length2);
//            } else {
//                System.arraycopy(bArr, i3 * i, bArr3, 0, i);
//            }
//            bArr2[i3] = bArr3;
//        }
//        return bArr2;
//    }

    public static byte[][] j(byte[] bArr, int i) {
        if (i <= 0) {
            throw new IllegalArgumentException("i must be positive");
        }
        int length = bArr.length / i;
        int length2 = bArr.length % i;
        int i2 = length + (length2 != 0 ? 1 : 0);
        byte[][] bArr2 = new byte[i2][];
        for (int i3 = 0; i3 < i2; i3++) {
            byte[] bArr3 = new byte[i];
            if (i3 == i2 - 1 && length2 != 0) {
                System.arraycopy(bArr, i3 * i, bArr3, 0, length2);
            } else {
                System.arraycopy(bArr, i3 * i, bArr3, 0, i);
            }
            bArr2[i3] = bArr3;
        }
        return bArr2;
    }




    public static String[] k(String str, int i) {
        String substring;
        int length = str.length() / i;
        int length2 = str.length() % i;
        int i2 = length + (length2 != 0 ? 1 : 0);
        String[] strArr = new String[i2];
        for (int i3 = 0; i3 < i2; i3++) {
            if (i3 == i2 - 1 && length2 != 0) {
                int i4 = i3 * i;
                substring = str.substring(i4, i4 + length2);
            } else {
                int i5 = i3 * i;
                substring = str.substring(i5, i5 + i);
            }
            strArr[i3] = substring;
        }
        return strArr;
    }
}