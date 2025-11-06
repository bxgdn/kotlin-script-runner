import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A GUI tool for executing Kotlin scripts with live output display.
 * Features:
 * - Syntax highlighting for Kotlin keywords
 * - Live output streaming
 * - Clickable error locations
 * - Running status indicator
 * - Exit code indicator
 * - Robust edge case handling
 */
public class ScriptRunner extends JFrame {
    // UI Components
    private JTextPane editorPane;
    private JTextArea outputArea;
    private JButton runButton;
    private JButton stopButton;
    private JLabel statusLabel;
    private JLabel exitCodeLabel;
    
    // Process management
    private volatile Process currentProcess;
    private ExecutorService executorService;
    private AtomicBoolean isExecuting = new AtomicBoolean(false);
    
    // Document
    private StyledDocument editorDoc;
    
    // Constants for edge case handling
    private static final int MAX_OUTPUT_LINES = 10000; // Prevent memory overflow
    private static final int MAX_SCRIPT_SIZE = 1_000_000; // 1MB max script size
    private static final long SCRIPT_TIMEOUT_SECONDS = 300; // 5 minute timeout
    private static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();
    
    // Output tracking
    private int outputLineCount = 0;
    
    // Kotlin keywords for syntax highlighting
    private static final Set<String> KOTLIN_KEYWORDS = new HashSet<>(Arrays.asList(
        "fun", "val", "var", "class", "object", "interface", "enum", "package", "import",
        "if", "else", "when", "for", "while", "do", "return", "break", "continue",
        "throw", "try", "catch", "finally", "in", "is", "as", "override", "abstract",
        "open", "private", "public", "internal", "protected", "data", "sealed", "companion",
        "init", "constructor", "this", "super", "null", "true", "false"
    ));
    
    // Text styles
    private Style defaultStyle;
    private Style keywordStyle;
    private Style stringStyle;
    private Style commentStyle;
    
    public ScriptRunner() {
        super("Kotlin Script Runner");
        executorService = Executors.newCachedThreadPool();
        initializeUI();
        setupDefaultScript();
        setupShutdownHook();
    }
    
    /**
     * Setup shutdown hook to clean up resources on application exit
     */
    private void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Stop any running process
            if (currentProcess != null && currentProcess.isAlive()) {
                currentProcess.destroyForcibly();
            }
            // Shutdown executor service
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdownNow();
                try {
                    executorService.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }));
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Create main layout
        setLayout(new BorderLayout());
        
        // Top toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        runButton = new JButton("▶ Run");
        runButton.setFont(new Font("Arial", Font.BOLD, 14));
        runButton.setBackground(new Color(76, 175, 80));
        runButton.setForeground(Color.WHITE);
        runButton.setOpaque(true);
        runButton.setBorderPainted(false);
        runButton.setFocusPainted(false);
        runButton.addActionListener(e -> runScript());
        
        stopButton = new JButton("⬛ Stop");
        stopButton.setFont(new Font("Arial", Font.BOLD, 14));
        stopButton.setBackground(new Color(244, 67, 54));
        stopButton.setForeground(Color.WHITE);
        stopButton.setOpaque(true);
        stopButton.setBorderPainted(false);
        stopButton.setFocusPainted(false);
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopScript());
        
        statusLabel = new JLabel("● Idle");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(128, 128, 128));
        
        exitCodeLabel = new JLabel("");
        exitCodeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        toolbarPanel.add(runButton);
        toolbarPanel.add(stopButton);
        toolbarPanel.add(Box.createHorizontalStrut(20));
        toolbarPanel.add(statusLabel);
        toolbarPanel.add(Box.createHorizontalStrut(20));
        toolbarPanel.add(exitCodeLabel);
        
        add(toolbarPanel, BorderLayout.NORTH);
        
        // Create split pane for editor and output
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.5);
        
        // Editor pane with syntax highlighting
        editorPane = new JTextPane();
        editorPane.setFont(new Font("Monospaced", Font.PLAIN, 14));
        editorDoc = editorPane.getStyledDocument();
        setupStyles();
        
        // Add document listener for syntax highlighting
        editorDoc.addDocumentListener(new DocumentListener() {
            private javax.swing.Timer timer = new javax.swing.Timer(300, e -> applySyntaxHighlighting());
            
            public void insertUpdate(DocumentEvent e) { timer.restart(); }
            public void removeUpdate(DocumentEvent e) { timer.restart(); }
            public void changedUpdate(DocumentEvent e) { timer.restart(); }
        });
        
        JScrollPane editorScroll = new JScrollPane(editorPane);
        editorScroll.setBorder(BorderFactory.createTitledBorder("Kotlin Script Editor"));
        
        // Output area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setBackground(new Color(30, 30, 30));
        outputArea.setForeground(new Color(220, 220, 220));
        outputArea.setCaretColor(Color.WHITE);
        
        // Make error locations clickable
        outputArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    handleOutputClick(e.getPoint());
                }
            }
        });
        
        outputArea.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Output"));
        
        splitPane.setLeftComponent(editorScroll);
        splitPane.setRightComponent(outputScroll);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private void setupStyles() {
        // Default style
        defaultStyle = editorDoc.addStyle("default", null);
        StyleConstants.setForeground(defaultStyle, Color.BLACK);
        
        // Keyword style
        keywordStyle = editorDoc.addStyle("keyword", null);
        StyleConstants.setForeground(keywordStyle, new Color(0, 0, 255));
        StyleConstants.setBold(keywordStyle, true);
        
        // String style
        stringStyle = editorDoc.addStyle("string", null);
        StyleConstants.setForeground(stringStyle, new Color(0, 128, 0));
        
        // Comment style
        commentStyle = editorDoc.addStyle("comment", null);
        StyleConstants.setForeground(commentStyle, new Color(128, 128, 128));
        StyleConstants.setItalic(commentStyle, true);
    }
    
    private void setupDefaultScript() {
        String defaultScript = 
            "// Sample Kotlin Script with Functions\n" +
            "// Note: main() is automatically called!\n\n" +
            "fun greet(name: String): String {\n" +
            "    return \"Hello, $name!\"\n" +
            "}\n\n" +
            "fun main() {\n" +
            "    println(\"--- Program Start ---\")\n" +
            "    \n" +
            "    val names = listOf(\"Alice\", \"Bob\", \"Charlie\")\n" +
            "    println(\"Greeting ${names.size} people:\\n\")\n" +
            "    \n" +
            "    for (name in names) {\n" +
            "        println(greet(name))\n" +
            "        Thread.sleep(400)\n" +
            "    }\n" +
            "    \n" +
            "    println(\"\\n--- Program End ---\")\n" +
            "}\n";
        
        try {
            editorDoc.insertString(0, defaultScript, defaultStyle);
            applySyntaxHighlighting();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    private void applySyntaxHighlighting() {
        try {
            String text = editorDoc.getText(0, editorDoc.getLength());
            
            // First, set everything to default
            editorDoc.setCharacterAttributes(0, text.length(), defaultStyle, true);
            
            // Highlight comments
            Pattern commentPattern = Pattern.compile("//.*$|/\\*.*?\\*/", Pattern.MULTILINE | Pattern.DOTALL);
            Matcher commentMatcher = commentPattern.matcher(text);
            while (commentMatcher.find()) {
                editorDoc.setCharacterAttributes(commentMatcher.start(), 
                    commentMatcher.end() - commentMatcher.start(), commentStyle, false);
            }
            
            // Highlight strings
            Pattern stringPattern = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
            Matcher stringMatcher = stringPattern.matcher(text);
            while (stringMatcher.find()) {
                editorDoc.setCharacterAttributes(stringMatcher.start(), 
                    stringMatcher.end() - stringMatcher.start(), stringStyle, false);
            }
            
            // Highlight keywords
            Pattern wordPattern = Pattern.compile("\\b\\w+\\b");
            Matcher matcher = wordPattern.matcher(text);
            while (matcher.find()) {
                String word = matcher.group();
                if (KOTLIN_KEYWORDS.contains(word)) {
                    // Check if it's not inside a string or comment
                    int start = matcher.start();
                    AttributeSet attrs = editorDoc.getCharacterElement(start).getAttributes();
                    if (attrs.getAttribute(StyleConstants.NameAttribute) != stringStyle &&
                        attrs.getAttribute(StyleConstants.NameAttribute) != commentStyle) {
                        editorDoc.setCharacterAttributes(start, word.length(), keywordStyle, false);
                    }
                }
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Automatically calls main() if it's defined but not called.
     * In Kotlin scripts, function definitions don't execute automatically.
     */
    private String ensureMainIsCalled(String script) {
        // Edge case: Handle null or empty script
        if (script == null || script.trim().isEmpty()) {
            return script;
        }
        
        // Check if script defines a main() function
        Pattern mainDefPattern = Pattern.compile("\\bfun\\s+main\\s*\\(");
        if (!mainDefPattern.matcher(script).find()) {
            return script; // No main function, return as-is
        }
        
        // Check if main() is already called at the top level
        // Remove comments, strings, and the function definition itself
        String cleaned = script
            .replaceAll("//[^\n]*", "")                    // Remove line comments
            .replaceAll("/\\*.*?\\*/", "")                  // Remove block comments  
            .replaceAll("\"([^\"\\\\]|\\\\.)*\"", "\"\"")   // Remove strings
            .replaceAll("\\bfun\\s+main\\s*\\([^)]*\\)", ""); // Remove the function definition
        
        // Now look for any remaining main() calls
        Pattern mainCallPattern = Pattern.compile("\\bmain\\s*\\(\\s*\\)");
        Matcher callMatcher = mainCallPattern.matcher(cleaned);
        
        // Check if any main() call is at the top level (brace depth 0)
        while (callMatcher.find()) {
            int position = callMatcher.start();
            int braceCount = 0;
            
            // Count braces before this position
            for (int i = 0; i < position; i++) {
                char c = cleaned.charAt(i);
                if (c == '{') braceCount++;
                else if (c == '}') braceCount--;
            }
            
            // If at top level, main() is already called
            if (braceCount == 0) {
                return script;
            }
        }
        
        // No top-level main() call found, append one
        return script + "\n\n// Auto-generated: Call the main function\nmain()\n";
    }
    
    private void runScript() {
        // Edge case: Prevent concurrent execution
        if (!isExecuting.compareAndSet(false, true)) {
            JOptionPane.showMessageDialog(this, 
                "A script is already running. Please stop it first.", 
                "Script Running", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get plain text from the document (not styled text)
        String rawScript;
        try {
            rawScript = editorDoc.getText(0, editorDoc.getLength());
        } catch (BadLocationException e) {
            isExecuting.set(false);
            JOptionPane.showMessageDialog(this, 
                "Error reading script content: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Edge case: Null or empty check
        if (rawScript == null || rawScript.trim().isEmpty()) {
            isExecuting.set(false);
            JOptionPane.showMessageDialog(this, 
                "Script is empty. Please write some code first.", 
                "Empty Script", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Edge case: Script too large
        if (rawScript.length() > MAX_SCRIPT_SIZE) {
            isExecuting.set(false);
            JOptionPane.showMessageDialog(this, 
                String.format("Script is too large (%d characters). Maximum is %d characters.", 
                    rawScript.length(), MAX_SCRIPT_SIZE), 
                "Script Too Large", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Auto-detect and call main() if it exists but isn't called
        final String scriptContent = ensureMainIsCalled(rawScript);
        
        // Reset output line counter
        outputLineCount = 0;
        
        // Show notification if main() was auto-added
        if (scriptContent.length() > rawScript.length()) {
            outputArea.setText("[Note: main() function detected and will be called automatically]\n\n");
        } else {
            outputArea.setText("");
        }
        
        // Update UI
        runButton.setEnabled(false);
        stopButton.setEnabled(true);
        statusLabel.setText("● Running");
        statusLabel.setForeground(new Color(76, 175, 80));
        exitCodeLabel.setText("");
        
        // Run script in background
        executorService.submit(() -> {
            File tempScript = null;
            boolean completedNormally = false;
            try {
                // Create temporary script file with proper encoding
                tempScript = File.createTempFile("kotlin_script_", ".kts");
                Files.write(tempScript.toPath(), scriptContent.getBytes(StandardCharsets.UTF_8));
                
                // Execute script
                ProcessBuilder pb = new ProcessBuilder("/usr/bin/env", "kotlinc", "-script", tempScript.getAbsolutePath());
                pb.redirectErrorStream(true);
                
                // Edge case: Set environment encoding
                Map<String, String> env = pb.environment();
                env.put("LANG", "en_US.UTF-8");
                env.put("LC_ALL", "en_US.UTF-8");
                
                currentProcess = pb.start();
                
                // Read output in real-time with proper encoding
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(currentProcess.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    final String outputLine = line;
                    
                    // Edge case: Limit output lines to prevent memory overflow
                    if (outputLineCount >= MAX_OUTPUT_LINES) {
                        SwingUtilities.invokeLater(() -> {
                            outputArea.append("\n[Output limit reached: " + MAX_OUTPUT_LINES + " lines. Script continues running...]\n");
                        });
                        // Continue reading but don't display
                        while (reader.readLine() != null) {
                            // Drain remaining output
                        }
                        break;
                    }
                    
                    outputLineCount++;
                    SwingUtilities.invokeLater(() -> {
                        outputArea.append(outputLine + "\n");
                        // Edge case: Prevent excessive scrolling operations
                        if (outputLineCount % 100 == 0) {
                            outputArea.setCaretPosition(outputArea.getDocument().getLength());
                        }
                    });
                }
                
                // Wait for process to complete
                int exitCode = currentProcess.waitFor();
                final int finalExitCode = exitCode;
                completedNormally = true;
                
                // Edge case: Final scroll to end
                SwingUtilities.invokeLater(() -> {
                    outputArea.setCaretPosition(outputArea.getDocument().getLength());
                });
                
                SwingUtilities.invokeLater(() -> {
                    runButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    statusLabel.setText("● Idle");
                    statusLabel.setForeground(new Color(128, 128, 128));
                    isExecuting.set(false);
                    
                    if (finalExitCode == 0) {
                        exitCodeLabel.setText("✓ Exit Code: 0");
                        exitCodeLabel.setForeground(new Color(76, 175, 80));
                    } else {
                        exitCodeLabel.setText("✗ Exit Code: " + finalExitCode);
                        exitCodeLabel.setForeground(new Color(244, 67, 54));
                    }
                    
                    // Edge case: Show message if script produced no output
                    if (outputLineCount == 0 && finalExitCode == 0) {
                        outputArea.append("[Script completed with no output]\n");
                    }
                });
                
            } catch (IOException | InterruptedException e) {
                final String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                SwingUtilities.invokeLater(() -> {
                    outputArea.append("\nError: " + errorMsg + "\n");
                    runButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    statusLabel.setText("● Error");
                    statusLabel.setForeground(new Color(244, 67, 54));
                    exitCodeLabel.setText("✗ Error");
                    exitCodeLabel.setForeground(new Color(244, 67, 54));
                    isExecuting.set(false);
                });
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                // Catch any other unexpected errors
                final String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                SwingUtilities.invokeLater(() -> {
                    outputArea.append("\nUnexpected error: " + errorMsg + "\n");
                    runButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    statusLabel.setText("● Error");
                    statusLabel.setForeground(new Color(244, 67, 54));
                    exitCodeLabel.setText("✗ Error");
                    exitCodeLabel.setForeground(new Color(244, 67, 54));
                    isExecuting.set(false);
                });
                e.printStackTrace();
            } finally {
                // Clean up temporary file
                if (tempScript != null) {
                    try {
                        Files.deleteIfExists(tempScript.toPath());
                    } catch (Exception e) {
                        // Ignore deletion errors
                    }
                }
                
                // Ensure execution flag and UI are always reset
                final boolean wasCompleted = completedNormally;
                if (!wasCompleted) {
                    SwingUtilities.invokeLater(() -> {
                        runButton.setEnabled(true);
                        stopButton.setEnabled(false);
                        isExecuting.set(false);
                    });
                }
            }
        });
    }
    
    private void stopScript() {
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.destroyForcibly();
            outputArea.append("\n--- Script stopped by user ---\n");
            stopButton.setEnabled(false);
            runButton.setEnabled(true);
            statusLabel.setText("● Stopped");
            statusLabel.setForeground(new Color(255, 152, 0));
            exitCodeLabel.setText("⚠ Stopped");
            exitCodeLabel.setForeground(new Color(255, 152, 0));
            isExecuting.set(false);
        }
    }
    
    private void handleOutputClick(Point point) {
        try {
            int offset = outputArea.viewToModel2D(point);
            int lineStart = outputArea.getLineStartOffset(outputArea.getLineOfOffset(offset));
            int lineEnd = outputArea.getLineEndOffset(outputArea.getLineOfOffset(offset));
            String line = outputArea.getText(lineStart, lineEnd - lineStart);
            
            // Pattern to match error locations: "script:2:1:" or "file.kts:2:1:"
            Pattern errorPattern = Pattern.compile(".*\\.(kts|kt):(\\d+):(\\d+):");
            Matcher matcher = errorPattern.matcher(line);
            
            if (matcher.find()) {
                int errorLine = Integer.parseInt(matcher.group(2));
                int errorColumn = Integer.parseInt(matcher.group(3));
                
                // Navigate to error location in editor
                navigateToPosition(errorLine, errorColumn);
                
                // Highlight the clicked line in output
                outputArea.setSelectionStart(lineStart);
                outputArea.setSelectionEnd(lineEnd - 1);
            }
        } catch (Exception e) {
            // If parsing fails, just ignore the click
        }
    }
    
    private void navigateToPosition(int line, int column) {
        try {
            String text = editorDoc.getText(0, editorDoc.getLength());
            String[] lines = text.split("\n", -1);
            
            if (line > 0 && line <= lines.length) {
                int offset = 0;
                for (int i = 0; i < line - 1; i++) {
                    offset += lines[i].length() + 1; // +1 for newline
                }
                offset += Math.min(column - 1, lines[line - 1].length());
                
                editorPane.setCaretPosition(offset);
                editorPane.requestFocusInWindow();
                
                // Highlight the error line
                int lineStart = offset - (column - 1);
                int lineEnd = lineStart + lines[line - 1].length();
                editorPane.select(lineStart, lineEnd);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            ScriptRunner app = new ScriptRunner();
            app.setVisible(true);
        });
    }
}

