// Edge Case: Unicode and special characters
// Tests UTF-8 encoding support

fun main() {
    println("=== Unicode Test ===\n")
    
    // Emoji support
    println("Emojis: 🎉 🚀 ✨ 💻 🔥")
    
    // Different languages
    println("\nMultilingual:")
    println("English: Hello World")
    println("Spanish: Hola Mundo")
    println("Japanese: こんにちは世界")
    println("Arabic: مرحبا بالعالم")
    println("Russian: Привет мир")
    println("Chinese: 你好世界")
    
    // Special symbols
    println("\nSymbols: © ® ™ € £ ¥ ∞ ≈ ≠")
    
    // Mathematical symbols
    println("\nMath: π ≈ 3.14159, √2 ≈ 1.414, ∑ ∫ ∂")
    
    println("\n✓ Unicode test completed!")
}

