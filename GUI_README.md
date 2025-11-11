# 到梦空间抢单工具 - GUI版本 v2.0

## 📋 项目概述

这是到梦空间自动抢单工具的**现代化GUI版本**，采用JavaFX框架开发，拥有精美的macOS风格界面设计。

## ✨ 主要特性

### 🎨 界面设计
- **macOS风格设计语言**：简洁、优雅、现代
- **苹果式圆角和阴影**：精致的UI细节
- **响应式布局**：自适应不同分辨率
- **动画效果**：流畅的交互体验

### 🔐 安全认证
- **网络激活码验证**：简化的纯在线授权系统
- **加密通信**：AES+RSA双重加密
- **会话管理**：安全的token管理

### 📊 功能模块
1. **登录界面**
   - 账号密码登录
   - 激活码验证
   - 实时状态提示

2. **主界面**
   - 活动列表展示
   - 活动卡片式设计
   - 一键报名按钮

3. **日志系统**
   - 实时日志输出
   - 登录日志
   - 活动获取日志
   - 提交操作日志

## 🚀 技术栈

### 核心框架
- **JavaFX 21.0.1** - 现代GUI框架
- **Java 17** - LTS版本

### 依赖库
- **Hutool 5.8.16** - HTTP、JSON、加密工具
- **BouncyCastle 1.70** - 加密库
- **ONNX Runtime 1.16.3** - AI验证码识别
- **Logback 1.4.14** - 日志系统
- **Lombok 1.18.34** - 代码生成

### 构建工具
- **Maven** - 项目管理和构建
- **Maven Shade Plugin** - 打包可执行JAR

## 📦 项目结构

```
src/main/java/co/xiaoyuboy/
├── gui/                          # GUI模块
│   ├── DaoMengApp.java              # 主应用入口
│   ├── component/                   # UI组件
│   │   ├── ActivityCard.java           # 活动卡片组件
│   │   └── LogPanel.java               # 日志面板组件
│   ├── service/                     # 业务服务
│   │   ├── ApiResult.java              # 统一返回结果
│   │   ├── AuthService.java            # 登录服务
│   │   ├── ActivityService.java        # 活动服务
│   │   └── NetworkLicenseService.java  # 网络验证服务
│   ├── util/                        # 工具类
│   │   ├── AlertUtil.java              # 弹窗工具
│   │   └── LogManager.java             # 日志管理器
│   └── view/                        # 视图层
│       ├── LoginView.java              # 登录界面
│       └── MainView.java               # 主界面
├── entity/                       # 实体类（复用）
├── util/                         # 加密工具（复用）
├── captcha/                      # 验证码识别（复用）
└── ...                          # 其他业务逻辑

src/main/resources/
├── css/
│   └── macos-style.css          # macOS风格样式
├── logback.xml                  # 日志配置
└── captcha/                     # AI模型资源
```

## 🔧 编译和运行

### 编译项目
```bash
mvn clean compile
```

### 打包应用
```bash
mvn package
```
生成的JAR文件：`target/daomengUtil-2.0-GUI.jar`

### 运行应用
```bash
java -jar target/daomengUtil-2.0-GUI.jar
```

或使用JavaFX插件运行：
```bash
mvn javafx:run
```

## 📸 界面预览

### 登录界面
- 简洁的登录表单
- 实时状态提示
- 加载动画

### 主界面
- 左侧：活动列表（可滚动）
- 右侧：实时日志面板
- 顶部：工具栏和用户信息

## 🎯 使用流程

1. **启动应用**
   - 运行JAR文件或通过Maven启动

2. **登录系统**
   - 输入手机号
   - 输入密码
   - 输入激活码
   - 点击登录按钮

3. **选择活动**
   - 查看可报名活动列表
   - 点击"立即报名"按钮

4. **查看日志**
   - 右侧日志面板实时显示操作记录

## 🔑 授权说明

本版本采用**纯网络验证**方式：
- 无需本地授权文件
- 激活码在线验证
- 验证服务器：`http://38.207.176.57:5666`

## ⚠️ 注意事项

1. **网络要求**
   - 需要稳定的网络连接
   - 激活码验证需要联网

2. **系统要求**
   - Java 17 或更高版本
   - 支持Windows/macOS/Linux

3. **依赖说明**
   - 已移除Spring Boot全家桶（轻量化）
   - 保留必要的Jackson和FastJSON（业务需要）

## 🆚 与CLI版本对比

| 特性 | CLI版本 | GUI版本 |
|------|--------|--------|
| 界面 | 命令行 | 图形化 |
| 授权方式 | 本地+在线 | 纯在线 |
| 用户体验 | 技术向 | 友好 |
| 日志查看 | 控制台 | 图形化面板 |
| 依赖大小 | 较大（Spring Boot） | 中等（JavaFX） |
| 启动速度 | 快 | 中等 |

## 🛠️ 开发说明

### 添加新功能
1. 在`gui/view/`下创建新视图
2. 在`gui/service/`下创建业务服务
3. 在`gui/component/`下创建UI组件

### 修改样式
编辑 `src/main/resources/css/macos-style.css`

### 修改配置
编辑 `src/main/resources/logback.xml`

## 📝 更新日志

### v2.0-GUI (当前版本)
- ✅ JavaFX图形界面
- ✅ macOS风格设计
- ✅ 纯网络验证
- ✅ 实时日志系统
- ✅ 简化依赖（移除Spring Boot）
- ✅ 修复机器码重启变化bug

### v1.0 (CLI版本)
- 命令行界面
- 本地+在线混合授权
- 支持无验证码和验证码识别模式

## 📄 许可声明

本程序仅供逆向学习交流使用，请于24小时内删除。

---

**开发者**: xiaoyuboy
**版本**: 2.0-GUI
**构建日期**: 2025-11-11
**辅助开发**: Claude Code
