package com.jotrorox.napi

import com.jotrorox.napi.fetching.NewsApiFetcher
import com.jotrorox.napi.util.config.getConfig
import com.jotrorox.napi.util.db.insertArticles
import com.jotrorox.napi.util.db.setupDB
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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
    val newsApiFetcher = NewsApiFetcher(config)

    // Set up the database
    setupDB()

    // Create a single-threaded scheduled executor
    val executor = Executors.newSingleThreadScheduledExecutor()

    // Schedule a task to fetch and insert news articles at a fixed rate
    executor.scheduleAtFixedRate({
        val news = newsApiFetcher.getNews()
        if (news != null) insertArticles(news.articles, config.getCountryCode())
    }, 0, config.getRefreshInterval(), TimeUnit.MINUTES)
}