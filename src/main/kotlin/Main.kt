package com.jotrorox

import com.google.gson.Gson
import com.jotrorox.Articles.author
import com.jotrorox.Articles.content
import com.jotrorox.Articles.countryCode
import com.jotrorox.Articles.description
import com.jotrorox.Articles.fetchedAt
import com.jotrorox.Articles.id
import com.jotrorox.Articles.publishedAt
import com.jotrorox.Articles.sourceId
import com.jotrorox.Articles.sourceName
import com.jotrorox.Articles.title
import com.jotrorox.Articles.url
import com.jotrorox.Articles.urlToImage
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
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
 * Represents the "articles" table in the database.
 *
 * @property id The unique identifier of the article. This is an auto-incrementing integer.
 * @property title The title of the article. This is a string with a maximum length of 1024 characters.
 * @property author The author of the article. This is a string with a maximum length of 1024 characters.
 * @property description A brief description of the article. This is a nullable string with a maximum length of 4096 characters.
 * @property url The URL where the article can be read. This is a string with a maximum length of 1024 characters.
 * @property urlToImage The URL of the image associated with the article. This is a nullable string with a maximum length of 1024 characters.
 * @property publishedAt The date and time when the article was published. This is a string with a maximum length of 1024 characters.
 * @property content The content of the article. This is a nullable string with a maximum length of 8192 characters.
 * @property sourceName The name of the source from which the article originates. This is a string with a maximum length of 1024 characters.
 * @property sourceId The unique identifier of the source. This is a string with a maximum length of 1024 characters.
 * @property fetchedAt The date and time when the article was fetched. This is a string with a maximum length of 1024 characters.
 * @property countryCode The country code associated with the article. This is a string with a maximum length of 2 characters.
 */
data object Articles : Table("articles") {
    private val id = integer("id").autoIncrement()
    val title = varchar("title", 1024)
    val author = varchar("author", 1024)
    val description = varchar("description", 4096).nullable()
    val url = varchar("url", 1024)
    val urlToImage = varchar("urlToImage", 1024).nullable()
    val publishedAt = varchar("publishedAt", 1024)
    val content = varchar("content", 8192).nullable()
    val sourceName = varchar("sourceName", 1024)
    val sourceId = varchar("sourceId", 1024)
    val fetchedAt = varchar("fetchedAt", 1024)       // yyyy-MM-dd HH:mm:ss
    val countryCode = varchar("countryCode", 2)
    override val primaryKey = PrimaryKey(id)
}

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
 * Sets up the database connection and creates the articles table if it doesn't exist.
 *
 * This function connects to a SQLite database named "news.db" using the SQLite JDBC driver.
 * Then, it creates the "articles" table in the database if it doesn't already exist.
 * The "articles" table is represented by the `Articles` object.
 */
fun setupDB() {
    Database.connect("jdbc:sqlite:news.db", "org.sqlite.JDBC")
    SchemaUtils.create(Articles)
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
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(date).toInstant().atZone(TimeZone.getDefault().toZoneId()).toLocalDateTime()
}

/**
 * Checks if a given article is already stored in the database.
 *
 * This function starts a transaction and selects all records from the "articles" table.
 * It then filters the records by comparing the title of each record with the title of the given article.
 * If the count of matching records is greater than 0, the function returns true, indicating that the article is already stored.
 * Otherwise, it returns false.
 *
 * @param article The article to check. This is an `Article` object.
 * @return A boolean value indicating whether the article is already stored in the database.
 */
fun isArticleStored(article: Article): Boolean {
    return transaction {
        Articles.selectAll().where { Articles.title eq article.title }.count() > 0
    }
}

/**
 * Inserts a list of articles into the database.
 *
 * This function starts a transaction and iterates over each article in the provided list.
 * For each article, it first checks if the article is already stored in the database by calling the `isArticleStored` function.
 * If the article is already stored, it skips to the next article.
 * Otherwise, it inserts the article into the "articles" table in the database.
 * The values for the columns of the table are taken from the properties of the `Article` object.
 * The `fetchedAt` column is set to the current date and time, and the `countryCode` column is set to the code of the provided `CountryCode` enum entry.
 *
 * @param articles The list of articles to insert. This is a list of `Article` objects.
 * @param countryCode The country code associated with the articles. This is a `CountryCode` enum entry.
 */
fun insertArticles(articles: List<Article>, countryCode: CountryCode) {
    transaction {
        articles.forEach { article ->
            if (isArticleStored(article)) return@forEach
            Articles.insert {
                it[title] = article.title
                it[author] = article.author
                it[description] = article.description
                it[url] = article.url
                it[urlToImage] = article.urlToImage
                it[publishedAt] = article.publishedAt
                it[content] = article.content
                it[sourceName] = article.source.name
                it[sourceId] = article.source.id
                it[fetchedAt] = getCurrentTime()
                it[Articles.countryCode] = countryCode.code
            }
        }
    }
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
    // Create a command-line argument parser
    val parser = ArgParser("NAPI")

    // Define the command-line arguments
    val apiKey by parser.option(ArgType.String, shortName = "k", fullName = "key", description = "News API key")
    val countryCode by parser.option(ArgType.String, shortName = "c", fullName = "country-code", description = "Country code")
    val speed by parser.option(ArgType.Int, shortName = "s", fullName = "speed", description = "Refresh speed in minutes")

    // Parse the command-line arguments
    parser.parse(args)

    // Retrieve the environment variables
    val envApiKey = System.getenv("NEWS_API_KEY")
    val envCountryCode = System.getenv("NEWS_COUNTRY_CODE")
    val envSpeed = System.getenv("NEWS_REFRESH_SPEED")?.toIntOrNull()

    // Validate the API key
    if (apiKey == null && envApiKey == null) {
        println("Error: No API key provided. Please set the NEWS_API_KEY environment variable or use the -k/--key option.")
        return
    }

    // Validate the country code
    if (countryCode == null && envCountryCode == null) {
        println("Error: No country code provided. Please set the NEWS_COUNTRY_CODE environment variable or use the -c/--country-code option.")
        return
    }

    // Convert the country code to upper case
    val finalCountryCodeString = (countryCode ?: envCountryCode).uppercase()

    // Convert the country code string to a CountryCode enum entry
    val finalCountryCode = try {
        CountryCode.valueOf(finalCountryCodeString)
    } catch (e: IllegalArgumentException) {
        println("Error: Invalid country code provided. Please use a valid country code.")
        return
    }

    // Set up the database
    setupDB()

    // Determine the final API key and refresh speed
    val finalApiKey = apiKey ?: envApiKey
    val finalSpeed = speed ?: envSpeed ?: 60

    // Create a single-threaded scheduled executor
    val executor = Executors.newSingleThreadScheduledExecutor()

    // Schedule a task to fetch and insert news articles at a fixed rate
    executor.scheduleAtFixedRate({
        val news = getNews(finalApiKey, finalCountryCode)
        if (news != null) {
            insertArticles(news.articles, finalCountryCode)
        }
    }, 0, finalSpeed.toLong(), TimeUnit.MINUTES)
}