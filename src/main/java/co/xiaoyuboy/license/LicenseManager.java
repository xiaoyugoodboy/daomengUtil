package co.xiaoyuboy.license;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

/**
 * 管理授权流程（纯本地）.
 */
public class LicenseManager {

    private static final byte[] SECRET_KEY = "Daomeng@2024#License!".getBytes(StandardCharsets.UTF_8);
    private static final Path LICENSE_FILE =
            Paths.get(System.getProperty("user.home"), ".daomeng", "license.dat");

    public LicenseInfo ensureLicense(BufferedReader reader) throws IOException {
        String machineId = MachineCodeGenerator.generateMachineCode();
        System.out.println("======== 授权校验 ========");
        System.out.println("本机机械码: " + machineId);
        System.out.println("请将此机械码发送给管理员换取授权码。");

        LicenseInfo info = loadLicense(machineId);
        if (info != null) {
            printLicenseSummary(info);
            System.out.print("检测到本地授权，是否重新输入授权码?(yes重新授权/回车跳过):");
            String choose = reader.readLine();
            if (!"yes".equalsIgnoreCase(choose)) {
                return info;
            }
            deleteLicenseFile();
            info = null;
        }

        while (true) {
            System.out.print("请输入授权码(输入exit退出):");
            String code = reader.readLine();
            if (code == null) {
                continue;
            }
            code = code.trim();
            if (code.isEmpty()) {
                System.out.println("授权码不能为空");
                continue;
            }
            if ("exit".equalsIgnoreCase(code)) {
                System.out.println("未获得授权，程序退出");
                System.exit(0);
            }
            try {
                LicenseInfo licenseInfo = decodeLicenseCode(code, machineId);
                saveLicenseCode(code);
                System.out.println("授权成功！有效期至：" + licenseInfo.getExpireAtText());
                printLicenseSummary(licenseInfo);
                return licenseInfo;
            } catch (Exception e) {
                System.out.println("授权码无效: " + e.getMessage());
            }
        }
    }

    public void ensureAccountMatches(LicenseInfo info, String account) {
        if (!info.getPhone().equals(account)) {
            System.out.println("当前登录账号(" + account + ")与授权手机号不一致(" + info.getPhone() + ")，请使用被授权的手机号登录。");
            System.exit(0);
        }
        if (info.isExpired()) {
            System.out.println("授权已过期，请联系管理员重新授权。");
            deleteLicenseFile();
            System.exit(0);
        }
    }

    private void printLicenseSummary(LicenseInfo info) {
        System.out.println("授权手机号: " + info.getPhone());
        System.out.println("授权剩余天数: " + info.getRemainingDays() + " 天");
        System.out.println("==========================");
    }

    private LicenseInfo loadLicense(String machineId) {
        if (!Files.exists(LICENSE_FILE)) {
            return null;
        }
        try {
            String code = Files.readString(LICENSE_FILE, StandardCharsets.UTF_8).trim();
            if (code.isEmpty()) {
                return null;
            }
            LicenseInfo info = decodeLicenseCode(code, machineId);
            if (info.isExpired()) {
                System.out.println("本地授权已过期，请重新授权。");
                deleteLicenseFile();
                return null;
            }
            return info;
        } catch (Exception e) {
            System.out.println("读取本地授权失败: " + e.getMessage());
            deleteLicenseFile();
            return null;
        }
    }

    private void saveLicenseCode(String code) {
        try {
            Files.createDirectories(LICENSE_FILE.getParent());
            Files.writeString(LICENSE_FILE, code, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("保存授权文件失败: " + e.getMessage());
        }
    }

    private void deleteLicenseFile() {
        try {
            Files.deleteIfExists(LICENSE_FILE);
        } catch (IOException ignored) {
        }
    }

    public static String createLicenseCode(String machineId, String phone, long validDays) {
        long issuedAt = Instant.now().toEpochMilli();
        long expireAt = Instant.now().plus(validDays, ChronoUnit.DAYS).toEpochMilli();
        JSONObject payload = new JSONObject();
        payload.set("machineId", machineId);
        payload.set("phone", phone);
        payload.set("expireAt", expireAt);
        payload.set("issuedAt", issuedAt);
        String payloadJson = payload.toString();
        String payloadBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signature = hmacHex(payloadBase64);
        return payloadBase64 + "." + signature;
    }

    public LicenseInfo decodeLicenseCode(String licenseCode, String expectedMachineId) {
        String[] parts = licenseCode.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("授权码格式不正确");
        }
        String payloadBase64 = parts[0];
        String signature = parts[1];
        String expectedSignature = hmacHex(payloadBase64);
        if (!expectedSignature.equalsIgnoreCase(signature)) {
            throw new IllegalArgumentException("授权码签名无效");
        }
        String payloadJson = new String(Base64.getUrlDecoder().decode(payloadBase64), StandardCharsets.UTF_8);
        JSONObject json = JSONUtil.parseObj(payloadJson);
        String machineId = json.getStr("machineId");
        if (!expectedMachineId.equals(machineId)) {
            throw new IllegalArgumentException("授权码不属于本机");
        }
        String phone = json.getStr("phone");
        long expireAt = json.getLong("expireAt");
        long issuedAt = json.getLong("issuedAt");
        return new LicenseInfo(machineId, phone, expireAt, issuedAt, licenseCode);
    }

    private static String hmacHex(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET_KEY, "HmacSHA256"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("计算授权签名失败", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
