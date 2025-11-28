@echo off
echo Starting DaoMeng GUI v2.0...
echo.

REM Check Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java not found. Please install Java 17+
    pause
    exit /b 1
)

REM Set module path
set MODULE_PATH=target\libs\javafx-base-21.0.1.jar;target\libs\javafx-base-21.0.1-win.jar;target\libs\javafx-controls-21.0.1.jar;target\libs\javafx-controls-21.0.1-win.jar;target\libs\javafx-graphics-21.0.1.jar;target\libs\javafx-graphics-21.0.1-win.jar;target\libs\javafx-fxml-21.0.1.jar;target\libs\javafx-fxml-21.0.1-win.jar

REM Launch app
java --module-path "%MODULE_PATH%" --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "target\daomengUtil-2.0-GUI.jar;target\libs\*" co.xiaoyuboy.gui.DaoMengApp

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Failed to start. Please run: mvn clean package
    echo.
    pause
)
