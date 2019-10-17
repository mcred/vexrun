package io.mcred.vexrun.utils

import java.io.IOException
import java.util.concurrent.TimeUnit
import org.apache.commons.lang3.SystemUtils

class CommandExecutor(val timeout: Long = 10) {

    fun exec(command: String, expectExitValue: Int, debug: Boolean = false): Boolean {
        val builder = ProcessBuilder()
        if (debug) println(command)
        builder.command(command.toList())
        val process = builder.start()
        try {
            /**
            Windows has an issue with the process.waitFor being at the beginning of this loop.
            It has been moved to the end with a condition special to Windows to catch this OS
            difference. Linux and MacOS want the process.waitFor at the beginning of the loop.
             */
            if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_LINUX) {
                process.waitFor(timeout, TimeUnit.MINUTES)
            }
            val output = process.inputStream.bufferedReader().readText()
            if (process.isAlive) {
                throw IOException("Timed out waiting for command: $command")
            }
            if (debug) println("Exit Value: ${process.exitValue()}")

            if (process.exitValue() != 0) {
                val errOutput = process.errorStream.bufferedReader().readText()
                if (debug) println(errOutput)
            }
            if (SystemUtils.IS_OS_WINDOWS) {
                process.waitFor(timeout, TimeUnit.MINUTES)
            }
            return (process.exitValue() == expectExitValue)
        } finally {
            process.destroy()
        }
    }

    companion object {
        fun String.toList(): List<String> {
            return this.split(" ")
        }
    }

}