package io.mcred.vexrun.controllers

import io.mcred.vexrun.models.Test
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream

data class Tests(
        val variables: Variables,
        val tests: MutableList<Test> = mutableListOf<Test>()
) {
    companion object{
        fun Tests.addFromFile(file: File) {
            val inputStream = FileInputStream(file)
            val obj: Map<String, Any> = Yaml().load(inputStream)
            val rawTests = obj["tests"] as List<Map<String, Any>>
            for (test in rawTests) {
                this.tests.add(Test.loadFromFile(test))
            }
        }
    }
}