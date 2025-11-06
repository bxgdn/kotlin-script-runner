// Loop with Live Output
// This demonstrates real-time output streaming

println("Starting countdown...")
println()

for (i in 10 downTo 1) {
    println("$i...")
    Thread.sleep(500)  // Wait 500ms between counts
}

println()
println("Blast off! 🚀")

