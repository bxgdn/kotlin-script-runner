# Kotlin Script Runner

A Java-based GUI application for executing Kotlin scripts with live output display, syntax highlighting, and interactive error navigation.

## Overview

This application provides an integrated development environment for writing and running Kotlin scripts. It features a split-pane interface with a code editor and output console, allowing real-time script execution and debugging.

## Prerequisites

### Required Software

1. **Java Development Kit (JDK) 11 or higher**
   - Check installation: `java -version`
   - Download from: https://adoptium.net/

2. **Kotlin Compiler**
   - Check installation: `kotlinc -version`
   - Install on macOS: `brew install kotlin`
   - Install on Linux: Download from https://kotlinlang.org/docs/command-line.html
   - The tool uses `/usr/bin/env kotlinc` to locate the compiler

## Building and Running

### Option 1: Using the Build Script (Recommended)

**macOS/Linux:**
```bash
cd kotlin-script-runner
chmod +x build.sh
./build.sh
```

**Windows:**
```cmd
cd kotlin-script-runner
build.bat
```

The script compiles the Java source code and launches the application automatically.

### Option 2: Manual Build

```bash
cd kotlin-script-runner
javac -d bin src/ScriptRunner.java
java -cp bin ScriptRunner
```

### Option 3: Using an IDE

1. Open the project in IntelliJ IDEA or any Java IDE
2. Add `src` as a source folder
3. Run `ScriptRunner.java` as a Java application

## Project Structure

```
kotlin-script-runner/
├── src/
│   └── ScriptRunner.java    # Main application (single file)
├── bin/                      # Compiled classes (auto-generated)
├── samples/                  # Example Kotlin scripts
│   ├── hello.kts
│   ├── loop.kts
│   ├── error.kts
│   └── edge-cases/
├── build.sh                  # Build script for Unix systems
├── build.bat                 # Build script for Windows
├── README.md                 # This file
└── UI_DESCRIPTION.md         # Detailed UI documentation
```

## User Interface

The application features a split-pane layout:

**Editor Pane (Left):**
- Editable text area for writing Kotlin scripts
- Syntax highlighting with color-coded keywords, strings, and comments
- Monospaced font optimized for code editing
- Standard text editing shortcuts (copy, paste, undo, select all)

**Output Pane (Right):**
- Read-only console displaying script execution output
- Dark theme with high contrast for readability
- Auto-scrolling to show latest output
- Clickable error messages that navigate to source locations
- Combines both stdout and stderr streams

**Toolbar (Top):**
- **Run Button**: Starts script execution (green, disabled while running)
- **Stop Button**: Terminates running scripts (red, enabled only while running)
- **Status Indicator**: Shows current execution state (Idle, Running, Stopped, Error)
- **Exit Code Display**: Shows success (✓ Exit Code: 0) or failure (✗ Exit Code: N)

For detailed UI specifications, see [UI_DESCRIPTION.md](UI_DESCRIPTION.md).

## Usage

### Basic Workflow

1. **Write Script**: Enter your Kotlin code in the editor pane
   - The editor comes pre-loaded with a sample script
   - Syntax highlighting updates automatically as you type

2. **Execute Script**: Click the "▶ Run" button
   - Status changes to "Running" with a green indicator
   - Output appears in real-time in the output pane
   - The UI remains responsive during execution

3. **View Results**: Watch the output stream live
   - Script output displays line-by-line as it executes
   - Errors show in the output with file location information

4. **Check Status**: After completion, verify the exit code
   - Green "✓ Exit Code: 0" indicates successful execution
   - Red "✗ Exit Code: N" indicates failure with error code N

5. **Stop Execution**: Use the "⬛ Stop" button for long-running scripts
   - Immediately terminates the running process
   - Status changes to "Stopped" with orange indicator

### Interactive Features

**Error Navigation:**
- Click on any error line in the output pane
- The editor automatically jumps to the error location
- Works with Kotlin compiler error format: `file.kts:LINE:COL: error message`

**Syntax Highlighting:**
- Keywords highlighted in blue with bold formatting
- String literals displayed in green
- Comments shown in gray with italic formatting
- Real-time updates with 300ms debounce for performance

## Architecture

### Technical Implementation

**Language:** Java 11+  
**GUI Framework:** Swing (javax.swing)  
**Script Execution:** Kotlin compiler via ProcessBuilder  
**Threading:** ExecutorService for concurrent execution  
**Text Processing:** StyledDocument for syntax highlighting

### Key Components

**ScriptRunner.java** - Main application class containing:
- `initializeUI()`: Constructs the GUI layout with split panes
- `setupStyles()`: Configures text styles for syntax highlighting
- `applySyntaxHighlighting()`: Applies real-time syntax coloring using regex
- `runScript()`: Manages script execution in background threads
- `stopScript()`: Terminates running processes
- `handleOutputClick()`: Parses error locations from output
- `navigateToPosition()`: Moves editor cursor to specific line/column

### How It Works

1. **Script Execution Flow:**
   - User clicks Run button
   - Script content is written to a temporary `.kts` file
   - ProcessBuilder spawns `kotlinc -script` command
   - Output is streamed line-by-line using BufferedReader
   - Temporary file is deleted after execution completes

2. **Live Output Streaming:**
   - Runs in a background thread to keep UI responsive
   - Reads process output continuously
   - Updates output pane in real-time via SwingUtilities.invokeLater
   - Captures both standard output and error streams

3. **Process Management:**
   - Stores reference to running process
   - Stop button calls destroyForcibly() to terminate
   - Proper cleanup of resources in finally blocks
   - Thread-safe execution state management

4. **Syntax Highlighting:**
   - Uses regex patterns to match Kotlin keywords, strings, and comments
   - StyledDocument applies text attributes (color, font style)
   - DocumentListener triggers highlighting on text changes
   - Debounce timer prevents excessive re-rendering during typing

5. **Error Parsing:**
   - Regex pattern matches `file.kts:LINE:COL:` format
   - Extracts line and column numbers from error messages
   - Calculates character offset in editor document
   - Highlights error line and moves cursor to location

## Sample Scripts

The `samples/` directory contains example scripts:
- `hello.kts`: Simple hello world example
- `loop.kts`: Demonstrates live output streaming
- `error.kts`: Shows error handling and clickable navigation
- `features.kts`: Comprehensive demonstration of various capabilities
- `edge-cases/`: Scripts testing various edge cases
