// Example: Functions with main()
// Note: main() is automatically called - no need to call it manually!

fun getGreeting(name: String): String {
    // We use a string template to insert the 'name' variable
    return "Hello, $name! Welcome."
}

fun calculateSum(a: Int, b: Int): Int {
    return a + b
}

// The main function where the program starts
fun main() {
    println("--- Program Start ---")

    // 1. Create a list of names
    val names = listOf("Alice", "Bob", "Charlie")
    println("Created a list with ${names.size} names.")

    // 2. Loop through each name in the list
    println("\n--- Starting Loop ---")
    for (name in names) {
        // 3. For each name, call our function to get a greeting
        val greeting = getGreeting(name)

        // 4. Print the greeting to the console
        println(greeting)
    }
    println("--- Loop Finished ---")

    // 5. Test the math function
    println("\n--- Math Test ---")
    val result = calculateSum(15, 27)
    println("15 + 27 = $result")

    println("\n--- Program End ---")
}

// No need to call main() - the script runner does it automatically!

