package com.jotrorox.superAPI

import com.google.gson.Gson
import java.net.HttpURLConnection
import java.net.URI

data class Source(
    val id: String,
    val name: String
)

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

data class ApiResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)

enum class CountryCode(val code: String) {
    AR("ar"),
    AU("au"),
    BR("br"),
    CA("ca"),
    CN("cn"),
    DE("de"),
    FR("fr"),
    GB("gb"),
    IN("in"),
    IT("it"),
    JP("jp"),
    KR("kr"),
    RU("ru"),
    US("us")
}

fun getNews(countryCode: CountryCode): ApiResponse? {
    val params = arrayOf("country" to countryCode, "apiKey" to System.getenv("NEWS_API_KEY"))
    val queryString = params.joinToString("&", prefix = "?") { (key, value) -> "$key=$value" }

    val url = URI("https://newsapi.org/v2/top-headlines$queryString")

    val connection = url.toURL().openConnection() as HttpURLConnection
    connection.requestMethod = "GET"

    val response = connection.inputStream.bufferedReader().use { it.readText() }

    if (connection.responseCode != 200) {
        println("Failed to get news: $response")
        return null
    }

    return Gson().fromJson(response, ApiResponse::class.java)
}

fun main() {
    val news = getNews(CountryCode.DE)
    news?.articles?.forEach { article ->
        println(article.title)
    }
}