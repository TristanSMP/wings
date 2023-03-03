package com.tristansmp.wings.lib

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlIndentation
import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.TomlOutputConfig
import com.tristansmp.wings.Wings
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File
import java.io.IOException

@Serializable
data class Config(
    val token: String? = null,
    val wingsApiEndpoint: String? = null,
)

class ConfigManager {
    val config: Config
    private val outputConfig = TomlOutputConfig(
        indentation = TomlIndentation.FOUR_SPACES,
        ignoreNullValues = false
    )

    init {
        if (!Wings.instance.dataFolder.exists()) {
            Wings.instance.dataFolder.mkdirs()
        }

        val file = File(Wings.instance.dataFolder, "config.toml")

        if (!file.exists()) {
            try {
                Toml(outputConfig = outputConfig).encodeToString(Config()).toByteArray()
                    .also { file.writeBytes(it) }
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }

        config = Toml(
            inputConfig = TomlInputConfig(
                ignoreUnknownNames = false,
                allowEmptyValues = true,
                allowNullValues = true,
                allowEscapedQuotesInLiteralStrings = true,
                allowEmptyToml = true,
            ),
            outputConfig = outputConfig
        ).decodeFromString(
            file.readText()
        )

        Wings.instance.logger.info("Loaded config")
        Wings.instance.logger.info("Authentication is ${if (config.token == null) "disabled" else "enabled"}")
    }
}