package com.jotrorox.napi.util.config

import com.akuleshov7.ktoml.file.TomlFileReader
import com.google.gson.Gson
import com.jotrorox.napi.util.CountryCode
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.serialization.serializer
import org.ini4j.Ini
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

/**
 * This function gets the system configuration values.
 *
 * @param args an array of input arguments passed to the function. The arguments could be a News API key,
 * a country code, refresh speed in minutes, and/or a flag to save the current configuration into a file.
 *
 * Depending upon the presence of a particular configuration file (`config.toml`, `config.json`, `config.properties`, `config.ini`)
 * in a standardized location (`$XDG_CONFIG_HOME/napi/` or `~/.config/napi/`) on the user's system, it decodes the configuration from the file.
 *
 * If no such configuration file exists, it processes command-line arguments
 * and checks the presence of environment variables (`NEWS_API_KEY`, `NEWS_COUNTRY_CODE`, `NEWS_REFRESH_SPEED`)
 * to get the necessary configuration values.
 *
 * This function validates the API key and country code. If either is not provided, it prompts error messages and returns `null`.
 *
 * If necessary, it saves the current configuration into a `.toml` file.
 *
 * @return returns a `Config` object containing the configuration properties. If an API key or a country code is not provided, it returns `null`.
 *
 * @throws IllegalArgumentException if the country code supplied does not match any entry in the `CountryCode` enum.
 */
fun getConfig(args: Array<String>): Config? {
    val xdgConfigPath = System.getenv("XDG_CONFIG_HOME") ?: "${System.getProperty("user.home")}/.config"

    if (Files.exists(Paths.get("$xdgConfigPath/napi/config.toml"))) {
        return TomlFileReader.decodeFromFile<Config>(serializer(), "$xdgConfigPath/napi/config.toml")
    } else if (Files.exists(Paths.get("$xdgConfigPath/napi/config.json"))) {
        return Gson().fromJson(
            Files.readString(Paths.get("$xdgConfigPath/napi/config.json")),
            Config::class.java
        )
    } else if (Files.exists(Paths.get("$xdgConfigPath/napi/config.properties"))) {
        val props = Properties()
        props.load(Files.newInputStream(Paths.get("$xdgConfigPath/napi/config.properties")))
        return Config(
            apiKey = props.getProperty("apiKey"),
            countryCode = CountryCode.valueOf(props.getProperty("countryCode")),
            refreshInterval = props.getProperty("refreshInterval").toLong()
        )
    } else if (Files.exists(Paths.get("$xdgConfigPath/napi/config.ini"))) {
        val ini = Ini()
        ini.load(Files.newInputStream(Paths.get("$xdgConfigPath/napi/config.ini")))
        return Config(
            apiKey = ini.get("config", "apiKey"),
            countryCode = CountryCode.valueOf(ini.get("config", "countryCode")),
            refreshInterval = ini.get("config", "refreshInterval").toLong()
        )
    }

    // Create a command-line argument parser
    val parser = ArgParser("NAPI")

    // Define the command-line arguments
    val argApiKey by parser.option(
        ArgType.String,
        shortName = "k",
        fullName = "key",
        description = "News API key"
    )
    val argCountryCode by parser.option(
        ArgType.String,
        shortName = "c",
        fullName = "country-code",
        description = "Country code"
    )
    val argSpeed by parser.option(
        ArgType.Int,
        shortName = "r",
        fullName = "refresh-speed",
        description = "Refresh speed in minutes"
    )
    val saveconfig by parser.option(
        ArgType.Boolean,
        shortName = "s",
        fullName = "save-config",
        description = "Save the current config into a file (default is TOML)"
    )

    // Parse the command-line arguments
    parser.parse(args)

    // Retrieve the environment variables
    val envApiKey = System.getenv("NEWS_API_KEY")
    val envCountryCode = System.getenv("NEWS_COUNTRY_CODE")
    val envSpeed = System.getenv("NEWS_REFRESH_SPEED")?.toIntOrNull()

    // Validate the API key
    argApiKey ?: envApiKey ?: run {
        println("Error: No API key provided. Please set the NEWS_API_KEY environment variable or use the -k/--key option.")
        return null
    }

    // Validate the country code
    argCountryCode ?: envCountryCode ?: run {
        println("Error: No country code provided. Please set the NEWS_COUNTRY_CODE environment variable or use the -c/--country-code option.")
        return null
    }

    // Convert the country code string to a CountryCode enum entry
    val countryCode = try {
        CountryCode.valueOf((argCountryCode ?: envCountryCode).uppercase())
    } catch (e: IllegalArgumentException) {
        println("Error: Invalid country code provided. Please use a valid country code.")
        return null
    }

    // Determine the final API key and refresh speed
    val apiKey = argApiKey ?: envApiKey
    val speed = argSpeed ?: envSpeed ?: 60

    val config = Config(apiKey, countryCode, speed.toLong())

    // If the flag is set save the config
    if (saveconfig == true) config.saveToToml()

    return config
}