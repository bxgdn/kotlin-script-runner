#!/bin/bash

# Kotlin Script Runner - Build and Run Script

echo "================================================"
echo "Kotlin Script Runner - Build Script"
echo "================================================"
echo ""

# Check for Java
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java is not installed or not in PATH"
    echo "Please install Java 11 or higher from https://adoptium.net/"
    exit 1
fi

if ! command -v javac &> /dev/null; then
    echo "❌ Error: javac is not installed or not in PATH"
    echo "Please install JDK 11 or higher from https://adoptium.net/"
    exit 1
fi

echo "✓ Java found: $(java -version 2>&1 | head -n 1)"

# Check for Kotlin
if ! command -v kotlinc &> /dev/null; then
    echo "⚠️  Warning: kotlinc is not installed or not in PATH"
    echo "The GUI will start, but script execution will fail."
    echo "Install Kotlin with: brew install kotlin (macOS)"
    echo ""
fi

# Create bin directory
mkdir -p bin

echo ""
echo "Compiling ScriptRunner.java..."
javac -d bin src/ScriptRunner.java

if [ $? -eq 0 ]; then
    echo "✓ Compilation successful"
    echo ""
    echo "Launching Kotlin Script Runner..."
    echo "================================================"
    echo ""
    java -cp bin ScriptRunner
else
    echo "❌ Compilation failed"
    exit 1
fi

