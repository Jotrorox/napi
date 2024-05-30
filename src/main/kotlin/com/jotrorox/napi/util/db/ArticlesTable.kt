package com.jotrorox.napi.util.db

import org.jetbrains.exposed.sql.Table

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
    val fetchedAt = varchar("fetchedAt", 1024)
    val countryCode = varchar("countryCode", 2)
    override val primaryKey = PrimaryKey(id)
}