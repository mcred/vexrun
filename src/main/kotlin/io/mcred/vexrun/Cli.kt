package io.mcred.vexrun

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import io.mcred.vexrun.controllers.Tests
import io.mcred.vexrun.controllers.Tests.Companion.addFromFile
import io.mcred.vexrun.controllers.Tests.Companion.processAfter
import io.mcred.vexrun.controllers.Variables
import io.mcred.vexrun.models.Test
import io.mcred.vexrun.models.Test.Companion.run
import io.mcred.vexrun.models.Variable
import java.io.File
import kotlin.system.exitProcess

object Cli {

    class Vexrun : CliktCommand() {
        private val testFile: String? by option("-f", "-file", help = "Individual test file to run.")
        private val testDirectory: String by option("-d", "--directory", help = "Directory containing test files.")
                .default(System.getProperty("user.dir"))
        private val parameters by option("-p", "--parameters").pair().multiple()

        override fun run() {
            val tests = Tests(Variables())
            if (testFile.isNullOrEmpty()) {
                val files = tests.loadFilesFromDirectory(testDirectory)
                for (file in files) {
                    tests.addFromFile(file)
                }
            } else {
                val file = File(testFile)
                tests.addFromFile(file)
            }
            for (env in System.getenv()) {
                tests.variables.variables.add(Variable(env.key, env.value, Variable.Type.SYSTEM))
            }
            for (param in parameters) {
                tests.variables.variables.add(Variable(param.first, param.second, Variable.Type.PARAM))
            }
            var failedTests = 0
            for (order in tests.order) {
                val action = order.keys.first()
                if (action == Tests.Action.TEST) {
                    val test = order[Tests.Action.TEST] as Test
                    test.run(tests.variables)
                    if (test.status == Test.Status.FAILED) {
                        failedTests++
                    }
                }
                if (action == Tests.Action.AFTER) {
                    val after = order[Tests.Action.AFTER] as Map<String, Boolean>
                    tests.processAfter(after)
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




