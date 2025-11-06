// Example: Using Java imports in Kotlin scripts
import java.util.Date
import java.text.SimpleDateFormat

class Greeter(val name: String) {
    fun greet() {
        val timeOfDay = when {
            Date().hours < 12 -> "Morning"
            Date().hours < 18 -> "Afternoon"
            else -> "Evening"
        }
        println("Good $timeOfDay, $name!")
    }
}

fun main() {
    println("=== Script with Imports Demo ===\n")
    
    // Use the imported Date class
    val now = Date()
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    println("Current time: ${formatter.format(now)}")
    
    // Use our custom class
    println()
    val person = Greeter("Developer")
    person.greet()
    
    // Demonstrate keywords
    println("\nCounting to 3:")
    for (i in 1..3) {
        println("- $i")
    }
    
    // When expression
    val x = 2
    when (x) {
        1 -> println("\nx is 1")
        2 -> println("\nx is 2")
        else -> println("\nx is not 1 or 2")
    }
    
    println("\nScript finished successfully.")
}

