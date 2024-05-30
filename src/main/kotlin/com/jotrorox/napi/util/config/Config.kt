package com.jotrorox.napi.util.config

import com.akuleshov7.ktoml.Toml
import com.google.gson.Gson
import com.jotrorox.napi.util.CountryCode
import kotlinx.serialization.serializer
import org.ini4j.Ini
import java.io.File
import java.util.*

/**
 * This class `Config` is responsible for holding the configuration data required for the main application.
 *
 * @property apiKey String: The Key provided by the API to authorize interactions with it. This key is private and thus only accessible through a getter method.
 * @property countryCode CountryCode: The Country Code to be used with the API. This is private and can only be accessed through its associated getter.
 * @property refreshInterval Long: The refresh interval for the main application in minutes. Available through a getter as it is private.
 *
 * The class also includes getter methods:
 *
 * - getApiKey: A method that returns the `apiKey`.
 * - getCountryCode: A method that returns `countryCode`.
 * - getRefreshInterval: A method that returns `refreshInterval`.
 *
 * This class also contains a `Companion object` which includes a method `getConfig` that is responsible for creating a `Config` instance from
 * the command-line arguments, environment variables, and a configuration file. This function is crucial for processing and validating the configuration
 * parameters like API key, Country code and Refresh interval. It can return null in case of missing or invalid mandatory parameters.
 *
 */
data class Config(
    private val apiKey: String,
    private val countryCode: CountryCode,
    private val refreshInterval: Long
) {
    /**
     * This function returns the API key in String format. This API key is used to authorize interactions with the API.
     * It is invoked on an instance of the Config class.
     *
     * @return apiKey: The API key used for interactions with the API.
     */
    fun getApiKey() = apiKey

    /**
     * This function returns the Country Code in `CountryCode` format. This Country Code is used to specify the region for the API.
     * It is invoked on an instance of the Config class.
     *
     * @return countryCode: The Country Code used for region specification with the API.
     */
    fun getCountryCode() = countryCode

    /**
     * This function returns the refresh interval in Long format. This interval is used to specify the refresh rate for the main application.
     * It is invoked on an instance of the Config class.
     *
     * @return refreshInterval: The refresh interval for the main application in minutes.
     */
    fun getRefreshInterval() = refreshInterval

    /**
     * This function saves the config data to a TOML file. The created TOML file can then be used for future
     * configurations. The function abstracts the intricacies of working with TOML files, providing a simplified
     * interaction layer for configuration management.
     *
     * It is invoked on an instance of the Config class.
     */
    fun saveToToml() {
        val xdgConfigPath = System.getenv("XDG_CONFIG_HOME") ?: "${System.getProperty("user.home")}/.config"
        val tomlFile = File("$xdgConfigPath/napi/config.toml")
        tomlFile.parentFile.mkdirs()
        tomlFile.createNewFile()
        val tomlString = Toml.encodeToString(serializer(), this)
        tomlFile.writeText(tomlString)
    }

    /**
     * This function saves the configuration data to a JSON file. The JSON file can then be used for future
     * configurations. The function abstracts the intricacies of working with JSON files, providing a simplified
     * interaction layer for configuration management.
     *
     * It's invoked on an instance of the Config class.
     */
    fun saveToJson() {
        val xdgConfigPath = System.getenv("XDG_CONFIG_HOME") ?: "${System.getProperty("user.home")}/.config"
        val jsonFile = File("$xdgConfigPath/napi/config.json")
        jsonFile.parentFile.mkdirs()
        jsonFile.createNewFile()
        val jsonString = Gson().toJson(this)
        jsonFile.writeText(jsonString)
    }

    /**
     * This function saves the configuration data to a Properties file. The Properties file can then be used for future
     * configurations. The function abstracts the intricacies of working with Properties files, providing a simplified
     * interaction layer for configuration management.
     *
     * It's invoked on an instance of the Config class.
     */
    fun saveToProperties() {
        val xdgConfigPath = System.getenv("XDG_CONFIG_HOME") ?: "${System.getProperty("user.home")}/.config"
        val propsFile = File("$xdgConfigPath/napi/config.properties")
        propsFile.parentFile.mkdirs()
        propsFile.createNewFile()
        val props = Properties()
        props.setProperty("apiKey", apiKey)
        props.setProperty("countryCode", countryCode.name)
        props.setProperty("refreshInterval", refreshInterval.toString())
        props.store(propsFile.outputStream(), null)
    }

    /**
     * This function saves the configuration data to an INI file. The INI file can then be used for future
     * configurations. The function abstracts the intricacies of working with INI files, providing a simplified
     * interaction layer for configuration management.
     *
     * It's invoked on an instance of the Config class.
     */
    fun saveToIni() {
        val xdgConfigPath = System.getenv("XDG_CONFIG_HOME") ?: "${System.getProperty("user.home")}/.config"
        val iniFile = File("$xdgConfigPath/napi/config.ini")
        iniFile.parentFile.mkdirs()
        iniFile.createNewFile()
        val ini = Ini()
        ini.put("config", "apiKey", apiKey)
        ini.put("config", "countryCode", countryCode.name)
        ini.put("config", "refreshInterval", refreshInterval.toString())
        ini.store(iniFile.outputStream())
    }
}