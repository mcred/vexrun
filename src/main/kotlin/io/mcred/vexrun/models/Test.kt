package io.mcred.vexrun.models

import io.mcred.vexrun.controllers.Variables
import io.mcred.vexrun.utils.CommandExecutor
import io.mcred.vexrun.utils.CommandExecutor.Companion.toList
import io.mcred.vexrun.models.Env.Companion.getVariableFromResult
import io.mcred.vexrun.models.Output.Companion.compareToResults
import org.json.JSONObject

data class Test(
        val name: String,
        var status: Status,
        val command: MutableList<String>,
        val exitValue: Int = 0,
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
            var outputName = this.name
            if (!this.envs.isNullOrEmpty()) {
                for (env in this.envs){
                    if (env.operation == Env.Operation.GET) {
                        val varList = variables.getVariablesByKey(env.key)
                        for (variable in varList) {
                            for (item in this.command) {
                                if (item.contains("$${env.key}")) {
                                    this.command[this.command.indexOf(item)] = item.replace("$${env.key}", variable.value)
                                }
                            }
                            for (output in this.outputs!!) {
                                output.expected = output.expected.replace("$${env.key}", variable.value)
                            }
                            if (outputName.contains("$${env.key}")) {
                                outputName = outputName.replace("$${env.key}", variable.value)
                            }
                        }
                    }
                }
            }
            print("$outputName: ")
            val result = CommandExecutor().exec(this.command)
            if (result.exitValue != this.exitValue) {
                this.status = Status.FAILED
            }
            for (output in this.outputs!!) {
                val outputCompare = output.compareToResults(result)
                if (!outputCompare) {
                    this.status = Status.FAILED
                }
            }
            if (this.status != Status.FAILED) {
                this.status = Status.PASSED
                if (!this.envs.isNullOrEmpty()){
                    for (env in this.envs){
                        if (env.operation == Env.Operation.SET) {
                            variables.variables.add(env.getVariableFromResult(result, this.outputs.first().type))
                        }
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
            val type = when {
                obj.containsKey("stderr") -> "stderr"
                else -> "stdout"
            }
            val rawOutput = obj[type]
            if (rawOutput is String) {
                val output = Output(
                        Output.Type.valueOf(type.toUpperCase()),
                        Output.Compare.EQUALS,
                        rawOutput
                )
                outputList.add(output)
            } else {
                val outputMap = rawOutput as Map<String, Any>
                val key = outputMap.keys.first()

                if (outputMap[key] is String) {
                    val output = Output(
                            Output.Type.valueOf(type.toUpperCase()),
                            Output.Compare.valueOf(key.toUpperCase()),
                            outputMap[key] as String
                    )
                    outputList.add(output)
                } else {
                    val outputArray = outputMap[key] as List<String>
                    for (outputItem in outputArray) {
                        val output = Output(
                                Output.Type.valueOf(type.toUpperCase()),
                                Output.Compare.valueOf(key.toUpperCase()),
                                outputItem
                        )
                        outputList.add(output)
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
        fun loadFromFile(raw: Map<String, Any>, params: Map<String, String>? = null): Test {
            var name = raw.keys.first()
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
                                    val pattern = JSONObject(set)
                                    val patternKey = pattern.keySet().first()
                                    val env = Env(
                                        Env.Operation.SET,
                                        key,
                                        Env.Type.valueOf(patternKey.toUpperCase()),
                                        pattern.getJSONObject(patternKey).toMap() as Map<String, Any>
                                    )
                                    envList.add(env)
                                }
                            }
                        }
                    }
                    if (key == "get") {
                        val gets = rawEnv["get"] as List<String>
                        for (key in gets) {
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
            }
            val outputs =getOutputsFromObj(obj)
            val command = getCommandFromObj(obj)
            if (!params.isNullOrEmpty()) {
                for (param in params) {
                    name = name.replace("$${param.key}", "${param.value}")
                    for (item in command) {
                        if (item.contains("$${param.key}")) {
                            command[command.indexOf(item)] = item.replace("$${param.key}", "${param.value}")
                        }
                    }
                    for (output in outputs) {
                        output.expected = output.expected.replace("$${param.key}", "${param.value}")
                    }
                }
            }
            return Test(
                name,
                Status.PENDING,
                command,
                if(obj.containsKey("exitValue")) obj["exitValue"] as Int else 0,
                if(obj.containsKey("wait")) obj["wait"] as Int else 0,
                outputs,
                if(envList.isNotEmpty()) envList else null
            )
        }
    }
}