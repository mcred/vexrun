package io.mcred.vexrun

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int

object Cli {
    class Hello : CliktCommand() {
        val count: Int by option(help = "Number of greetings").int().default(1)
        val name: String by option(help = "The person to greet").prompt("Your name")

        override fun run() {
            for (i in 1..count) {
                echo("Hello $name!")
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) = Hello().main(args)
}




