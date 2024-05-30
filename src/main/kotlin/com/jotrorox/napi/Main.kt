package com.jotrorox.napi

import com.google.gson.Gson
import com.jotrorox.napi.util.config.getConfig
import com.jotrorox.napi.util.db.insertArticles
import com.jotrorox.napi.util.db.setupDB
import java.net.HttpURLConnection
import java.net.URI
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Represents a source of news articles.
 *
 * @property id The unique identifier of the source.
 * @property name The name of the source.
 */
data class Source(
    val id: String,
    val name: String
)

/**
 * Represents a news article.
 *
 * @property source The source from which the article originates.
 * @property author The author of the article.
 * @property title The title of the article.
 * @property description A brief description of the article.
 * @property url The URL where the article can be read.
 * @property urlToImage The URL of the image associated with the article.
 * @property publishedAt The date and time when the article was published.
 * @property content The content of the article.
 */
data class Article(
    val source: Source,
    val author: String,
    val title: String,
    val description: String,
    val url: String,
    val urlToImage: String,
    val publishedAt: String,
    val content: String
)

/**
 * Represents the response from the API.
 *
 * @property status The status of the response. This is a string.
 * @property totalResults The total number of results in the response. This is an integer.
 * @property articles The list of articles in the response. This is a list of Article objects.
 */
data class ApiResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)

/**
 * Represents the country codes used in the API.
 *
 * Each enum entry represents a country code, and the associated string value is the actual code used in the API.
 *
 * @property code The actual country code used in the API.
 */
enum class CountryCode(val code: String) {
    AR("ar"),  // Argentina
    AU("au"),  // Australia
    BR("br"),  // Brazil
    CA("ca"),  // Canada
    CN("cn"),  // China
    DE("de"),  // Germany
    FR("fr"),  // France
    GB("gb"),  // United Kingdom
    IN("in"),  // India
    IT("it"),  // Italy
    JP("jp"),  // Japan
    KR("kr"),  // South Korea
    RU("ru"),  // Russia
    US("us")   // United States
}

/**
 * Fetches news from the API.
 *
 * This function sends a GET request to the News API to fetch the top headlines for a specific country.
 * The country is specified by the `countryCode` parameter, and the API key is specified by the `apiKey` parameter.
 * If the request is successful, the function parses the JSON response into an `ApiResponse` object and returns it.
 * If the request is not successful, the function prints an error message and returns null.
 *
 * @param apiKey The API key to use for the request. This is a string.
 * @param countryCode The country code to use for the request. This is a `CountryCode` enum entry.
 * @return An `ApiResponse` object if the request is successful, or null if the request is not successful.
 */
fun getNews(apiKey: String, countryCode: CountryCode): ApiResponse? {
    val url = URI("https://newsapi.org/v2/top-headlines?country=${countryCode.code}&apiKey=$apiKey")

    val connection = url.toURL().openConnection() as HttpURLConnection
    connection.requestMethod = "GET"

    if (connection.responseCode != 200) {
        println("Failed to get news: ${connection.inputStream.bufferedReader().readText()}")
        return null
    }

    return Gson().fromJson(connection.inputStream.bufferedReader().readText(), ApiResponse::class.java)
}

/**
 * Gets the current time in the format "yyyy-MM-dd HH:mm:ss".
 *
 * This function uses the `SimpleDateFormat` class to format the current date and time into a string.
 * The format used is "yyyy-MM-dd HH:mm:ss", which represents the year, month, day, hour, minute, and second.
 *
 * @return A string representing the current date and time.
 */
fun getCurrentTime(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
}

/**
 * Converts a string representation of a date into a `LocalDateTime` object.
 *
 * This function uses the `SimpleDateFormat` class to parse the input string into a `Date` object.
 * The input string is expected to be in the format "yyyy-MM-dd'T'HH:mm:ss'Z'".
 * The function then converts the `Date` object into an `Instant`, adjusts it to the system's default time zone,
 * and finally converts it into a `LocalDateTime` object.
 *
 * @param date A string representing a date and time. The string is expected to be in the format "yyyy-MM-dd'T'HH:mm:ss'Z'".
 * @return A `LocalDateTime` object representing the same date and time as the input string.
 */
fun getDateFromString(date: String): LocalDateTime {
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(date).toInstant().atZone(TimeZone.getDefault().toZoneId())
        .toLocalDateTime()
}

/**
 * The main function of the application.
 *
 * This function parses command-line arguments and environment variables to get the API key, country code, and refresh speed.
 * It then validates these inputs and sets up the database.
 * Finally, it schedules a task to fetch and insert news articles at a fixed rate.
 *
 * @param args The command-line arguments. This is an array of strings.
 */
fun main(args: Array<String>) {
    val config = getConfig(args) ?: return

    // Set up the database
    setupDB()

    // Create a single-threaded scheduled executor
    val executor = Executors.newSingleThreadScheduledExecutor()

    // Schedule a task to fetch and insert news articles at a fixed rate
    executor.scheduleAtFixedRate({
        val news = getNews(config.getApiKey(), config.getCountryCode())
        if (news != null) {
            insertArticles(news.articles, config.getCountryCode())
        }
    }, 0, config.getRefreshInterval(), TimeUnit.MINUTES)
}