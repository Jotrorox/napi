package com.jotrorox.superAPI

import com.google.gson.Gson
import com.jotrorox.superAPI.Articles.select
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table.Dual.autoIncrement
import org.jetbrains.exposed.sql.Table.Dual.integer
import org.jetbrains.exposed.sql.Table.Dual.varchar
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.HttpURLConnection
import java.net.URI
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

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

fun setupDB() {
    Database.connect("jdbc:sqlite:news.db", "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(Articles)
    }
}

fun getCurrentTime(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
}

fun getDateFromString(date: String): LocalDateTime {
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(date).toInstant().atZone(TimeZone.getDefault().toZoneId()).toLocalDateTime()
}

fun isArticleStored(article: Article): Boolean {
    return transaction {
        Articles.selectAll().where { Articles.title eq article.title }.count() > 0
    }
}

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

fun main() {
    setupDB()

    val countryCode = CountryCode.DE
    val news = getNews(countryCode)

    if (news != null) {
        insertArticles(news.articles, countryCode)
    }
}