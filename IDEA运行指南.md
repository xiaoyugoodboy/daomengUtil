# 在IDEA中运行到梦空间GUI工具

## 🎯 主类（Main Class）

运行这个类：
```
co.xiaoyuboy.gui.DaoMengApp
```

完整路径：
```
src/main/java/co/xiaoyuboy/gui/DaoMengApp.java
```

## 🔧 IDEA运行配置步骤

### 方法1：直接右键运行（推荐）✅

1. 打开 `src/main/java/co/xiaoyuboy/gui/DaoMengApp.java`
2. **右键点击类名或main方法**
3. 选择 `Run 'DaoMengApp.main()'`

### 方法2：创建Run Configuration

1. 点击IDEA右上角 `Add Configuration...`
2. 点击 `+` → 选择 `Application`
3. 配置如下：

```
Name: DaoMeng GUI
Main class: co.xiaoyuboy.gui.DaoMengApp
Working directory: $MODULE_WORKING_DIR$
Use classpath of module: daomengUtil
JRE: 17 或更高版本
```

4. 点击 `Apply` → `OK`
5. 点击绿色播放按钮运行

### 方法3：使用Maven运行

在IDEA的Maven面板中：
```
1. 展开 daomengUtil
2. 展开 Plugins
3. 展开 javafx
4. 双击 javafx:run
```

## ⚙️ JVM参数（如果需要）

如果方法1和方法2运行出错，添加VM options：

```
--module-path ${MAVEN_REPOSITORY}/org/openjfx/javafx-controls/21.0.1/javafx-controls-21.0.1.jar;${MAVEN_REPOSITORY}/org/openjfx/javafx-fxml/21.0.1/javafx-fxml-21.0.1.jar;${MAVEN_REPOSITORY}/org/openjfx/javafx-graphics/21.0.1/javafx-graphics-21.0.1.jar
--add-modules javafx.controls,javafx.fxml,javafx.graphics
```

但通常**不需要**，IDEA会自动处理JavaFX依赖。

## 🐛 常见问题

### Q1: 找不到JavaFX类
**解决**：确保Maven依赖已下载完成
```bash
mvn clean compile
```

### Q2: 模块路径错误
**解决**：使用方法3（Maven运行）

### Q3: Lombok报错
**解决**：安装IDEA的Lombok插件，并启用注解处理
- `File` → `Settings` → `Build, Execution, Deployment` → `Compiler` → `Annotation Processors`
- 勾选 `Enable annotation processing`

## ✅ 验证是否成功

运行后应该看到：
1. 控制台输出日志
2. 弹出登录界面窗口
3. 没有JavaFX相关错误

## 🔐 关于激活码验证

**当前已设置为开发模式**，会跳过激活码验证！

在 `NetworkLicenseService.java` 第19行：
```java
private static final boolean DEV_MODE = true;  // 开发模式
```

- `true` = 跳过验证（开发测试用）
- `false` = 启用真实验证（生产环境）

登录时可以随便输入激活码，系统会自动跳过验证。

---

## 📸 截图示例

正常运行后应该看到：
```
[17:10:23] [系统] 应用启动
[17:10:23] [登录] 开始验证激活码
⚠️ 开发模式已启用，跳过激活码验证
[17:10:24] [登录] 激活码验证成功
[17:10:24] [登录] 正在登录...
```

然后弹出图形界面。
