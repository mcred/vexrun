package io.mcred.vexrun.models

data class Result(
        val exitValue: Int,
        val stdout: String?,
        val stderr: String?
)