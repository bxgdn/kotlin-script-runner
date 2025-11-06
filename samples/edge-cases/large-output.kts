// Edge Case: Large output test
// Tests the 10,000 line output limit

fun main() {
    println("=== Large Output Test ===")
    println("Generating 500 lines of output...\n")
    
    for (i in 1..500) {
        println("Line $i: This is a test line to generate large output and verify the application handles it gracefully.")
    }
    
    println("\n✓ Large output test completed!")
    println("If you see this, output limiting is working correctly.")
}

// Note: To test the 10,000 line limit, change 500 to 11000

