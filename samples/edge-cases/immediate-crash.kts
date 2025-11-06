// Edge Case: Script that crashes immediately
// Tests error recovery and UI reset

fun main() {
    println("About to crash...")
    
    // Intentional error
    val x: String = null!!
    
    println("This won't print")
}

