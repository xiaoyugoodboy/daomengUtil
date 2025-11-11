package co.xiaoyuboy.license;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 采集本机信息并生成稳定的机械码.
 */
public final class MachineCodeGenerator {

    private MachineCodeGenerator() {
    }

    public static String generateMachineCode() {
        List<String> factors = new ArrayList<>();

        // Windows注册表MachineGuid（最稳定的硬件标识）
        String machineGuid = getWindowsMachineGuid();
        if (machineGuid != null && !machineGuid.isBlank()) {
            factors.add("GUID:" + machineGuid.trim());
        }

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

        // 获取排序后的MAC地址（确保顺序稳定）
        List<String> macs = readStableMacAddresses();
        factors.addAll(macs);

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

    /**
     * 获取Windows注册表中的MachineGuid（最稳定的机器标识）
     */
    private static String getWindowsMachineGuid() {
        if (!System.getProperty("os.name", "").toLowerCase().contains("windows")) {
            return null;
        }

        try {
            Process process = Runtime.getRuntime().exec(new String[]{
                "reg", "query",
                "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Cryptography",
                "/v", "MachineGuid"
            });

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "GBK"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("MachineGuid")) {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length >= 3) {
                            return parts[parts.length - 1];
                        }
                    }
                }
            }
            process.waitFor();
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 读取稳定排序的物理网卡MAC地址
     */
    private static List<String> readStableMacAddresses() {
        // 使用TreeSet自动排序，确保顺序稳定
        Set<String> macs = new TreeSet<>();

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();

                // 过滤回环接口
                if (ni.isLoopback()) {
                    continue;
                }

                // 过滤虚拟接口（增强判断）
                if (ni.isVirtual() || isVirtualInterface(ni)) {
                    continue;
                }

                // 过滤未启用的接口
                if (!ni.isUp()) {
                    continue;
                }

                byte[] hardwareAddress = ni.getHardwareAddress();
                if (hardwareAddress != null && hardwareAddress.length == 6) {
                    // 标准以太网MAC地址长度为6字节
                    String mac = HexFormat.of().formatHex(hardwareAddress);

                    // 过滤无效MAC地址
                    if (!isInvalidMac(mac)) {
                        macs.add(mac);
                    }
                }
            }
        } catch (SocketException ignored) {
        }

        // 转换为列表（已排序）
        return new ArrayList<>(macs);
    }

    /**
     * 判断是否为虚拟网卡（通过名称和显示名称）
     */
    private static boolean isVirtualInterface(NetworkInterface ni) {
        String name = ni.getName().toLowerCase();
        String displayName = ni.getDisplayName().toLowerCase();

        // 常见虚拟网卡关键字
        String[] virtualKeywords = {
            "virtual", "vmware", "vbox", "virtualbox", "hyper-v",
            "docker", "wsl", "loopback", "pseudo", "tunnel",
            "teredo", "isatap", "6to4", "bluetooth", "vpn",
            "tap", "tun", "bridge"
        };

        for (String keyword : virtualKeywords) {
            if (name.contains(keyword) || displayName.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断是否为无效MAC地址
     */
    private static boolean isInvalidMac(String mac) {
        // 全0地址
        if ("000000000000".equals(mac)) {
            return true;
        }

        // 全F地址
        if ("ffffffffffff".equals(mac)) {
            return true;
        }

        // 多播地址（最低位为1）
        if (!mac.isEmpty()) {
            int firstByte = Integer.parseInt(mac.substring(0, 2), 16);
            if ((firstByte & 0x01) != 0) {
                return true;
            }
        }

        return false;
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
