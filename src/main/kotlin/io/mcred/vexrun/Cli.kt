package io.mcred.vexrun

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import io.mcred.vexrun.models.Test
import io.mcred.vexrun.models.Test.Companion.run
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import kotlin.system.exitProcess

object Cli {

    private fun getTestFiles(testDirectory: String): List<File> {
        val testFiles = mutableListOf<File>()
        File(testDirectory).walk().forEach {
            if (it.extension == "yml" || it.extension == "yaml") {
                testFiles.add(it)
            }
        }
        if (testFiles.count() == 0) {
            println("No test files found.")
            exitProcess(1)
        }
        return testFiles
    }

    class Run : CliktCommand() {
        private val testDirectory: String by option("-d", "--directory", help = "Directory containing test files.").default(System.getProperty("user.dir"))

        override fun run() {
            val files = getTestFiles(testDirectory)
            val tests = mutableListOf<Test>()
            for (file in files) {
                val inputStream = FileInputStream(file)
                val obj: Map<String, Any> = Yaml().load(inputStream)
                val rawTests = obj["tests"] as List<Map<String, Any>>
                for (test in rawTests) {
                    tests.add(Test.loadFromFile(test))
                }
            }
            for (test in tests) {
                test.run()
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) = Run().main(args)
}




