package co.xiaoyuboy.license;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 简易授权码生成工具（测试用）.
 */
public class LicenseTool {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("====== 授权码生成工具 ======");
        System.out.print("输入机器码:");
        String machineId = reader.readLine();
        System.out.print("绑定手机号:");
        String phone = reader.readLine();
        System.out.print("有效天数:");
        long days = Long.parseLong(reader.readLine());
        String licenseCode = LicenseManager.createLicenseCode(machineId.trim(), phone.trim(), days);
        System.out.println("生成授权码:\n" + licenseCode);
    }
}
