package com.jotrorox.napi.util.db

import com.jotrorox.napi.fetching.NewsApiFetcher.Article
import com.jotrorox.napi.converters.getCurrentTime
import com.jotrorox.napi.util.CountryCode
import com.jotrorox.napi.util.db.Articles.title
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

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
private fun isArticleStored(article: Article): Boolean {
    return transaction {
        Articles.selectAll().where { title eq article.title }.count() > 0
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
                it[Articles.countryCode] = countryCode.getCode()
            }
        }
    }
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
    transaction {
        SchemaUtils.create(Articles)
    }
}
