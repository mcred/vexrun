package io.mcred.vexrun.models

import io.mcred.vexrun.controllers.Variables
import io.mcred.vexrun.utils.CommandExecutor
import io.mcred.vexrun.utils.CommandExecutor.Companion.toList
import io.mcred.vexrun.models.Env.Companion.getVariableFromResult

data class Env(
        val operation: Operation,
        val key: String,
        val type: Type,
        val pattern: Map<String, String>?
) {
    enum class Operation(val value: String) {
        GET("GET"),
        SET("SET")
    }
    enum class Type(val value: String) {
        STRING("STRING"),
        REPLACE("REPLACE")
    }
    companion object{
        fun Env.getVariableFromResult(result: Result): Variable {
            val value = when(this.type) {
                Type.REPLACE -> result.stdout!!.replace("${this.pattern!!["find"]}", "${this.pattern["replace"]}")
                else -> result.stdout!!
            }
            return Variable(this.key, value)
        }
    }
}

data class Test(
        val name: String,
        var status: Status,
        val command: MutableList<String>,
        val exitValue: Int,
        val wait: Int = 0,
        val outputs: List<Output>? = null,
        val envs: List<Env>? = null
){
    enum class Status(val value: String){
        PENDING("PENDING"),
        PASSED("PASSED"),
        FAILED("FAILED")
    }

    companion object {
        fun Test.run(variables: Variables){
            print("${this.name}: ")

            if (!this.envs.isNullOrEmpty()) {
                for (env in this.envs){
                    if (env.operation == Env.Operation.GET) {
                        val variable = variables.getVariablesByKey(env.key)[0]
                        for (item in this.command) {
                            if (item == "$${env.key}") {
                                this.command[this.command.indexOf(item)] = variable.value
                            }
                        }
                        for (output in this.outputs!!) {
                            output.expected = output.expected.replace("$${env.key}", variable.value)
                        }
                    }
                }
            }

            val result = CommandExecutor().exec(this.command)
            if (result.exitValue != this.exitValue) {
                this.status = Status.FAILED
            }
            if (!this.outputs.isNullOrEmpty()) {
                for (output in this.outputs) {
                    val outputCompare = when (output.compare) {
                        Output.Compare.CONTAINS -> result.stdout!!.contains(output.expected)
                        else -> result.stdout.equals(output.expected)
                    }
                    if (!outputCompare) {
                        this.status = Status.FAILED
                    }
                }
            }
            if (this.status != Status.FAILED) {
                this.status = Status.PASSED
                if (!this.envs.isNullOrEmpty()){
                    for (env in this.envs){
                        variables.variables.add(env.getVariableFromResult(result))
                    }
                }
            }
            println(this.status)
            if (this.status == Status.FAILED) {
                println(result)
            }
            Thread.sleep(wait.toLong() * 1000)
        }

        private fun getOutputsFromObj(obj: Map<String, Any>): List<Output> {
            val outputList = mutableListOf<Output>()
            if (obj.containsKey("stdout")) {
                val rawOutput = obj["stdout"]
                if (rawOutput is String) {
                    val output = Output(
                            Output.Type.STDOUT,
                            Output.Compare.EQUALS,
                            rawOutput
                    )
                    outputList.add(output)
                } else {
                    val outputMap = rawOutput as Map<String, Any>
                    if (outputMap["contains"] is String) {
                        val output = Output(
                                Output.Type.STDOUT,
                                Output.Compare.CONTAINS,
                                outputMap["contains"] as String
                        )
                        outputList.add(output)
                    } else {
                        val outputArray = outputMap["contains"] as List<String>
                        for (outputItem in outputArray) {
                            val output = Output(
                                    Output.Type.STDOUT,
                                    Output.Compare.CONTAINS,
                                    outputItem
                            )
                            outputList.add(output)
                        }
                    }
                }
            }
            return outputList
        }

        private fun getCommandFromObj(obj: Map<String, Any>): MutableList<String> {
            val command = mutableListOf<String>()
            if (obj["command"] is String) {
                val commandString = obj["command"] as String
                command.addAll(commandString.toList())
            } else {
                val commandList = obj["command"] as List<String>
                command.addAll(commandList)
            }
            return command
        }

        @JvmStatic
        fun loadFromFile(raw: Map<String, Any>, variables: Variables): Test {
            val name = raw.keys.first()
            val obj = raw[name] as Map<String, Any>

            val envList = mutableListOf<Env>()
            if (obj.containsKey("env")) {
                val rawEnv = obj["env"] as Map<String, Any>
                for (key in rawEnv.keys) {
                    if (key == "set") {
                        val rawSets = rawEnv["set"] as List<Map<String, Any>>
                        for (rawSet in rawSets) {
                            val keys = rawSet.keys
                            for (key in keys) {
                                val set = rawSet[key]
                                if (rawSet[key] is String) {
                                    val env = Env(
                                            Env.Operation.SET,
                                            key,
                                            Env.Type.STRING,
                                            null
                                    )
                                    envList.add(env)
                                } else {
                                    val set = rawSet[key] as Map<String, String>
                                    val replace = set["replace"] as Map<String, String>
                                    val env = Env(
                                        Env.Operation.SET,
                                        key,
                                        Env.Type.REPLACE,
                                        replace
                                    )
                                    envList.add(env)
                                }
                            }
                        }
                    }
                    if (key == "get") {
                        val key = rawEnv["get"] as String
                        val env = Env(
                                Env.Operation.GET,
                                key,
                                Env.Type.STRING,
                                null
                        )
                        envList.add(env)
                    }
                }
            }
            val outputs = getOutputsFromObj(obj)
            return Test(
                name,
                Status.PENDING,
                getCommandFromObj(obj),
                obj["exitValue"] as Int,
                if(obj.containsKey("wait")) obj["wait"] as Int else 0,
                if(outputs.isNotEmpty()) outputs else null,
                if(envList.isNotEmpty()) envList else null
            )
        }
    }
}