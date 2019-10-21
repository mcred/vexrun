package io.mcred.vexrun

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import io.mcred.vexrun.controllers.Tests
import io.mcred.vexrun.controllers.Tests.Companion.addFromFile
import io.mcred.vexrun.controllers.Variables
import io.mcred.vexrun.models.Test
import io.mcred.vexrun.models.Test.Companion.run
import io.mcred.vexrun.models.Variable
import java.io.File
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

    class Vexrun : CliktCommand() {
        private val testFile: String? by option("-f", "-file", help = "Individual test file to run.")
        private val testDirectory: String by option("-d", "--directory", help = "Directory containing test files.")
                .default(System.getProperty("user.dir"))

        override fun run() {
            val tests = Tests(Variables())
            if (testFile.isNullOrEmpty()) {
                val files = getTestFiles(testDirectory)
                for (file in files) {
                    tests.addFromFile(file)
                }
            } else {
                val file = File(testFile)
                tests.addFromFile(file)
            }

            for (env in System.getenv()) {
                tests.variables.variables.add(Variable(env.key, env.value))
            }
            var failedTests = 0
            for (test in tests.tests) {
                test.run(tests.variables)
                if (test.status == Test.Status.FAILED) {
                    failedTests++
                }
            }
            if (failedTests != 0 ) {
                exitProcess(1)
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) = Vexrun().main(args)
}




