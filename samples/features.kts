// Kotlin Features Demo
// This script demonstrates various Kotlin features
// and syntax highlighting

// Data class
data class Person(val name: String, val age: Int)

// Function
fun greet(person: Person): String {
    return "Hello, ${person.name}! You are ${person.age} years old."
}

// Create instances
val alice = Person("Alice", 30)
val bob = Person("Bob", 25)

// Print greetings
println(greet(alice))
println(greet(bob))

// When expression
fun describe(obj: Any): String = when (obj) {
    is String -> "This is a string: $obj"
    is Int -> "This is an integer: $obj"
    is Person -> "This is a person: ${obj.name}"
    else -> "Unknown type"
}

// Test when expression
println()
println(describe("Kotlin"))
println(describe(42))
println(describe(alice))

// Collections
val numbers = listOf(1, 2, 3, 4, 5)
val doubled = numbers.map { it * 2 }
println()
println("Original: $numbers")
println("Doubled: $doubled")

// For loop
println()
for (i in 1..3) {
    println("Iteration $i")
}

// Nullable types
var nullable: String? = "Not null"
println()
println("Nullable value: ${nullable?.length}")

nullable = null
println("Nullable is now: $nullable")

// Try-catch
try {
    val result = 10 / 2
    println()
    println("Division result: $result")
} catch (e: Exception) {
    println("Error: ${e.message}")
}

println()
println("✓ Script completed successfully!")

