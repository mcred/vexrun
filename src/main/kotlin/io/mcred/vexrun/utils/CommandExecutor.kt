package io.mcred.vexrun.utils

import java.io.IOException
import java.util.concurrent.TimeUnit
import org.apache.commons.lang3.SystemUtils
import io.mcred.vexrun.models.Result

class CommandExecutor(private val timeout: Long = 10, private val unit: TimeUnit = TimeUnit.MINUTES) {

    fun exec(command: String): Result {
        val builder = ProcessBuilder()
        builder.command(command.toList())
        val process = builder.start()
        try {
            /**
            Windows has an issue with the process.waitFor being at the beginning of this loop.
            It has been moved to the end with a condition special to Windows to catch this OS
            difference. Linux and MacOS want the process.waitFor at the beginning of the loop.
             */
            if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_LINUX) {
                process.waitFor(timeout, unit)
            }
            if (process.isAlive) {
                throw IOException("Timed out waiting for command: $command")
            }
            if (SystemUtils.IS_OS_WINDOWS) {
                process.waitFor(timeout, TimeUnit.MINUTES)
            }
            return Result (
                process.exitValue(),
                process.inputStream.bufferedReader().readText(),
                process.errorStream.bufferedReader().readText()
            )
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