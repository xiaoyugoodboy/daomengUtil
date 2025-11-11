package co.xiaoyuboy.test;

import co.xiaoyuboy.license.MachineCodeGenerator;

public class TestMachineCodeStability {
    public static void main(String[] args) {
        System.out.println("=== 机器码稳定性测试 ===\n");

        // 连续生成5次
        for (int i = 1; i <= 5; i++) {
            String code = MachineCodeGenerator.generateMachineCode();
            System.out.println("第" + i + "次: " + code);
        }

        System.out.println("\n✅ 如果以上5次机器码完全相同，说明修复成功！");
        System.out.println("请重启电脑后再次运行，验证重启后机器码是否仍然相同。");
    }
}
