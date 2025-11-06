# UI Description and Layout

A detailed description of the application's user interface components and layout.

## Overall Layout

```
┌─────────────────────────────────────────────────────────────────────┐
│  Kotlin Script Runner                                      [─][□][×]│
├─────────────────────────────────────────────────────────────────────┤
│  [▶ Run]  [⬛ Stop]     ● Idle     ✓ Exit Code: 0                   │
├─────────────────────────────────────────────────────────────────────┤
│                                    │                                │
│  ┌──────────────────────────────┐  │  ┌──────────────────────────┐  │
│  │ Kotlin Script Editor         │  │  │ Output                   │  │
│  ├──────────────────────────────┤  │  ├──────────────────────────┤  │
│  │                              │  │  │                          │  │
│  │ 1 │ // Sample Kotlin Script  │  │  │  Hello from Kotlin!      │  │
│  │ 2 │ println("Hello from...") │  │  │  Count: 1                │  │
│  │ 3 │                          │  │  │  Count: 2                │  │
│  │ 4 │ // Loop example          │  │  │  Count: 3                │  │
│  │ 5 │ for (i in 1..5) {        │  │  │  Count: 4                │  │
│  │ 6 │     println("Count: $i") │  │  │  Count: 5                │  │
│  │ 7 │     Thread.sleep(500)    │  │  │  Hello, User!            │  │
│  │ 8 │ }                        │  │  │  Script completed...     │  │
│  │ 9 │                          │  │  │                          │  │
│  │10 │ // Variables & functions │  │  │                          │  │
│  │11 │ val message = "Script.." │  │  │                          │  │
│  │12 │ fun greet(name: String)  │  │  │                          │  │
│  │13 │     "Hello, $name!"      │  │  │                          │  │
│  │14 │                          │  │  │                          │  │
│  │15 │ println(greet("User"))   │  │  │                          │  │
│  │16 │ println(message)         │  │  │                          │  │
│  │17 │                          │  │  │                          │  │
│  │18 │                          │  │  │                          │  │
│  │19 │                          │  │  │                          │  │
│  └──────────────────────────────┘  │  └──────────────────────────┘  │
│                                    │                                │
└─────────────────────────────────────────────────────────────────────┘
```

## Component Descriptions

### 1. Title Bar
- **Text**: "Kotlin Script Runner"
- Standard window controls (minimize, maximize, close)

### 2. Toolbar (Top Section)

#### Run Button
- **Text**: "▶ Run"
- **Color**: Green (#4CAF50)
- **Text Color**: White
- **Font**: Bold, 14pt
- **State**: Enabled when idle, Disabled when running

#### Stop Button  
- **Text**: "⬛ Stop"
- **Color**: Red (#F44336)
- **Text Color**: White
- **Font**: Bold, 14pt
- **State**: Disabled when idle, Enabled when running

#### Status Label
- **Format**: "● [State]"
- **States**:
  - "● Idle" (Gray #808080) - No script running
  - "● Running" (Green #4CAF50) - Script executing
  - "● Stopped" (Orange #FF9800) - User terminated script
  - "● Error" (Red #F44336) - Execution failed

#### Exit Code Label
- **Format**: "[Icon] Exit Code: [N]"
- **States**:
  - "✓ Exit Code: 0" (Green) - Success
  - "✗ Exit Code: N" (Red) - Failure  
  - "⚠ Stopped" (Orange) - Terminated

### 3. Split Pane (Main Area)

#### Left Side: Editor Pane
- **Title**: "Kotlin Script Editor"
- **Background**: White
- **Font**: Monospaced, 14pt
- **Line Numbers**:
  - Displayed in left gutter (gray background)
  - Gray text color (#646464)
  - Right border separator line
  - Automatically updates with content changes
- **Features**:
  - Editable text area
  - Syntax highlighting with colors:
    - **Keywords** (fun, val, var, class, if, for, etc.): Blue, Bold
    - **Strings** ("..." or '...'): Green
    - **Comments** (// or /* */): Gray, Italic
    - **Default text**: Black
  - Scrollbar when content overflows
  - Standard text editing (copy, paste, select, undo)

#### Right Side: Output Pane
- **Title**: "Output"
- **Background**: Dark gray (#1E1E1E)
- **Text Color**: Light gray (#DCDCDC)
- **Font**: Monospaced, 12pt
- **Features**:
  - Read-only display
  - Auto-scroll to bottom
  - Clickable text (cursor changes to hand)
  - Selection highlighting
  - Shows script output in real-time
  - Displays error messages
  - Combines stdout and stderr

#### Split Pane Divider
- **Position**: 50% (600px in 1200px window)
- **Resizable**: Yes, drag to adjust
- **Visual**: Thin gray line

## Color Palette

### Primary Colors
- **Success Green**: #4CAF50 (RGB: 76, 175, 80)
- **Error Red**: #F44336 (RGB: 244, 67, 54)
- **Warning Orange**: #FF9800 (RGB: 255, 152, 0)
- **Neutral Gray**: #808080 (RGB: 128, 128, 128)

### Syntax Highlighting
- **Keywords**: #0000FF (Blue)
- **Strings**: #008000 (Green)
- **Comments**: #808080 (Gray)
- **Default**: #000000 (Black)

### Output Pane
- **Background**: #1E1E1E (Dark gray)
- **Text**: #DCDCDC (Light gray)
- **Selection**: System default

## Accessibility Features

- **High Contrast**: Dark output pane reduces eye strain
- **Color Coding**: Status uses both color and text (color-blind friendly)
- **Clear Labels**: All buttons have descriptive text
- **Cursor Feedback**: Changes to hand over clickable areas
- **Visual States**: Multiple indicators for script status
- **Keyboard Support**: Standard shortcuts work in editor

## Responsive Behavior

- **Window Resize**: Components scale proportionally
- **Split Pane**: Maintains ratio but can be manually adjusted
- **Auto-Scroll**: Output automatically shows latest content
- **Text Wrapping**: Output wraps long lines
- **Overflow**: Scrollbars appear when content exceeds viewport