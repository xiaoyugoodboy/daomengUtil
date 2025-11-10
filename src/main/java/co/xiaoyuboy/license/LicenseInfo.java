package co.xiaoyuboy.license;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * 保存授权信息.
 */
public class LicenseInfo {
    private final String machineId;
    private final String phone;
    private final long expireAt;
    private final long issuedAt;
    private final String rawCode;

    public LicenseInfo(String machineId, String phone, long expireAt, long issuedAt, String rawCode) {
        this.machineId = machineId;
        this.phone = phone;
        this.expireAt = expireAt;
        this.issuedAt = issuedAt;
        this.rawCode = rawCode;
    }

    public String getMachineId() {
        return machineId;
    }

    public String getPhone() {
        return phone;
    }

    public long getExpireAt() {
        return expireAt;
    }

    public long getIssuedAt() {
        return issuedAt;
    }

    public String getRawCode() {
        return rawCode;
    }

    public boolean isExpired() {
        return Instant.now().toEpochMilli() > expireAt;
    }

    public long getRemainingDays() {
        long diff = expireAt - Instant.now().toEpochMilli();
        if (diff <= 0) {
            return 0;
        }
        return diff / (1000 * 60 * 60 * 24);
    }

    public String getExpireAtText() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("Asia/Shanghai"))
                .format(Instant.ofEpochMilli(expireAt));
    }

    @Override
    public String toString() {
        return "LicenseInfo{" +
                "machineId='" + machineId + '\'' +
                ", phone='" + phone + '\'' +
                ", expireAt=" + getExpireAtText() +
                ", issuedAt=" + issuedAt +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(machineId, phone, expireAt, issuedAt);
    }
}
