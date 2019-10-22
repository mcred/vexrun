package io.mcred.vexrun.controllers

import io.mcred.vexrun.models.Test
import io.mcred.vexrun.models.Variable
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import kotlin.system.exitProcess

data class Tests(
        val variables: Variables,
        val tests: MutableList<Test> = mutableListOf(),
        val after: MutableMap<String, Boolean> = mutableMapOf(),
        val order: MutableList<Map<Action, Any>> = mutableListOf()
) {

    enum class Action{
        TEST,
        AFTER
    }

    fun loadFilesFromDirectory(testDirectory: String): List<File> {
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

    companion object{
        fun Tests.addFromFile(file: File, params: Map<String, String>? = null) {
            val inputStream = FileInputStream(file)
            val obj: Map<String, Any> = Yaml().load(inputStream)
            if (obj.containsKey("tests")) {
                this.addTestsFromObj(obj, params)
            }
            if (obj.containsKey("files")){
                val path = file.absolutePath.replace(file.name, "")
                this.addTestsFromFiles(obj, path)
            }
            if (obj.containsKey("after")) {
                val after = obj["after"] as Map<String, Boolean>
                for (item in after) {
                    this.order.add(mapOf(Action.AFTER to mapOf(item.key to item.value)))
                }
            }
        }
        private fun Tests.addTestsFromObj(obj: Map<String, Any>, params: Map<String, String>? = null) {
            val rawTests = obj["tests"] as List<Map<String, Any>>
            for (test in rawTests) {
                this.tests.add(Test.loadFromFile(test, params))
                this.order.add(mapOf(Action.TEST to Test.loadFromFile(test, params)))
            }
        }
        private fun Tests.addTestsFromFiles(obj: Map<String, Any>, path: String) {
            val rawFiles = obj["files"] as List<Map<String, Any>>
            for (file in rawFiles) {
                val params = file[file.keys.first()] as Map<String, Map<String, String>>
                this.addFromFile(File(path + file.keys.first()), params["parameters"])
            }
        }
        fun Tests.processAfter(after: Map<String, Boolean>) {
            if (after.containsKey("clearVars")) {
                if (after["clearVars"]!!) {
                    val rmList = mutableListOf<Variable>()
                    for (variable in this.variables.variables) {
                        if (variable.type == Variable.Type.RESULT) {
                            rmList.add(variable)
                        }
                    }
                    this.variables.variables.removeAll(rmList)
                }
            }
        }
    }
}