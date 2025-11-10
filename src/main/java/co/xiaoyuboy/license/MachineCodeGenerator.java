package co.xiaoyuboy.license;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HexFormat;
import java.util.List;

/**
 * 采集本机信息并生成稳定的机械码.
 */
public final class MachineCodeGenerator {

    private MachineCodeGenerator() {
    }

    public static String generateMachineCode() {
        List<String> factors = new ArrayList<>();
        addEnv(factors, "COMPUTERNAME");
        addEnv(factors, "USERDOMAIN");
        addEnv(factors, "PROCESSOR_IDENTIFIER");
        addEnv(factors, "PROCESSOR_ARCHITECTURE");
        addEnv(factors, "PROCESSOR_REVISION");
        addEnv(factors, "NUMBER_OF_PROCESSORS");

        factors.add(System.getProperty("os.name", ""));
        factors.add(System.getProperty("os.version", ""));
        factors.add(System.getProperty("os.arch", ""));
        factors.add(System.getProperty("user.name", ""));
        factors.add(System.getProperty("user.home", ""));

        factors.addAll(readMacAddresses());

        String raw = String.join("|", factors).trim();
        if (raw.isBlank()) {
            raw = "UNKNOWN_MACHINE";
        }
        return sha256Hex(raw).substring(0, 32).toUpperCase();
    }

    private static void addEnv(List<String> factors, String key) {
        String value = System.getenv(key);
        if (value != null && !value.isBlank()) {
            factors.add(value.trim());
        }
    }

    private static List<String> readMacAddresses() {
        List<String> macs = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }
                byte[] hardwareAddress = ni.getHardwareAddress();
                if (hardwareAddress != null && hardwareAddress.length > 0) {
                    macs.add(HexFormat.of().formatHex(hardwareAddress));
                }
            }
        } catch (SocketException ignored) {
        }
        return macs;
    }

    private static String sha256Hex(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("无法计算SHA-256", e);
        }
    }
}
