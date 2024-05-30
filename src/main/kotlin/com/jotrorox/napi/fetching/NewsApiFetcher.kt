package com.jotrorox.napi.fetching

import com.google.gson.Gson
import com.jotrorox.napi.util.config.Config
import java.net.HttpURLConnection
import java.net.URI

class NewsApiFetcher(private val config: Config) {
    /**
     * Fetches the top headlines from the NewsAPI, based on the country provided in the config.
     *
     * It constructs the URL using the country code and the API key from the config. It then
     * opens an HTTP connection to this URL and fetches the news. If the status code is not 200,
     * it logs the error message and returns null. Otherwise, it converts the received JSON
     * into an ApiResponse object and returns it.
     *
     * @return ApiResponse object if the fetch was successful, null otherwise
     */
    fun getNews(): ApiResponse? {
        val url = URI("https://newsapi.org/v2/top-headlines?country=${config.getCountryCode().getCode()}&apiKey=${config.getApiKey()}")

        val connection = url.toURL().openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        if (connection.responseCode != 200) {
            println("Failed to get news: ${connection.inputStream.bufferedReader().readText()}")
            return null
        }

        return Gson().fromJson(connection.inputStream.bufferedReader().readText(), ApiResponse::class.java)
    }

    fun getConfig(): Config = config

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
}