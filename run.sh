#!/bin/bash

echo "========================================"
echo "  到梦空间抢单工具 - GUI版本 v2.0"
echo "========================================"
echo ""

# 检查Java版本
if ! command -v java &> /dev/null; then
    echo "[错误] 未检测到Java环境，请先安装Java 17或更高版本"
    exit 1
fi

echo "[信息] Java版本："
java -version
echo ""
echo "[信息] 正在启动应用..."
echo ""

# 根据操作系统设置JavaFX平台
if [[ "$OSTYPE" == "darwin"* ]]; then
    PLATFORM="mac"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    PLATFORM="linux"
else
    PLATFORM="win"
fi

# 设置JavaFX模块路径
JAVAFX_PATH="target/libs/javafx-base-21.0.1.jar:target/libs/javafx-controls-21.0.1.jar:target/libs/javafx-graphics-21.0.1.jar:target/libs/javafx-fxml-21.0.1.jar:target/libs/javafx-base-21.0.1-${PLATFORM}.jar:target/libs/javafx-controls-21.0.1-${PLATFORM}.jar:target/libs/javafx-graphics-21.0.1-${PLATFORM}.jar"

# 启动应用
java --module-path "$JAVAFX_PATH" --add-modules javafx.controls,javafx.fxml,javafx.graphics -jar target/daomengUtil-2.0-GUI.jar

if [ $? -ne 0 ]; then
    echo ""
    echo "[错误] 应用启动失败"
    echo ""
    echo "可能的原因："
    echo "1. JAR文件不存在（请先运行 mvn package 编译）"
    echo "2. JavaFX依赖缺失（请检查 target/libs 目录）"
    echo "3. Java版本不兼容（需要Java 17+）"
    echo ""
    exit 1
fi
