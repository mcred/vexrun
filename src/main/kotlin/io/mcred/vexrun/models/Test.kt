package io.mcred.vexrun.models

import io.mcred.vexrun.utils.CommandExecutor
import io.mcred.vexrun.utils.CommandExecutor.Companion.toList

data class Test(
        val name: String,
        var status: Status,
        val command: List<String>,
        val exitValue: Int,
        val wait: Int = 0,
        val outputs: List<Output>? = null
){
    enum class Status(val value: String){
        PENDING("PENDING"),
        PASSED("PASSED"),
        FAILED("FAILED")
    }

    companion object {
        fun Test.run(){
            print("${this.name}: ")
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
            }
            println(this.status)
            if (this.status == Status.FAILED) {
                println(result)
            }
            Thread.sleep(wait.toLong() * 1000)
        }

        @JvmStatic
        fun loadFromFile(raw: Map<String, Any>): Test {
            val name = raw.keys.first()
            val obj = raw[name] as Map<String, Any>

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

            val command = mutableListOf<String>()
            if (obj["command"] is String) {
                val commandString = obj["command"] as String
                command.addAll(commandString.toList())
            } else {
                val commandList = obj["command"] as List<String>
                command.addAll(commandList)
            }

            return Test(
                name,
                Status.PENDING,
                command,
                obj["exitValue"] as Int,
                if(obj.containsKey("wait")) obj["wait"] as Int else 0,
                if(outputList.isNotEmpty()) outputList else null
            )
        }
    }
}