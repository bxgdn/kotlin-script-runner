@echo off
REM Kotlin Script Runner - Build and Run Script for Windows

echo ================================================
echo Kotlin Script Runner - Build Script
echo ================================================
echo.

REM Check for Java
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 11 or higher from https://adoptium.net/
    exit /b 1
)

where javac >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: javac is not installed or not in PATH
    echo Please install JDK 11 or higher from https://adoptium.net/
    exit /b 1
)

echo Java found
java -version

REM Check for Kotlin
where kotlinc >nul 2>nul
if %errorlevel% neq 0 (
    echo Warning: kotlinc is not installed or not in PATH
    echo The GUI will start, but script execution will fail.
    echo Install Kotlin from https://kotlinlang.org/docs/command-line.html
    echo.
)

REM Create bin directory
if not exist bin mkdir bin

echo.
echo Compiling ScriptRunner.java...
javac -d bin src\ScriptRunner.java

if %errorlevel% equ 0 (
    echo Compilation successful
    echo.
    echo Launching Kotlin Script Runner...
    echo ================================================
    echo.
    java -cp bin ScriptRunner
) else (
    echo Compilation failed
    exit /b 1
)

