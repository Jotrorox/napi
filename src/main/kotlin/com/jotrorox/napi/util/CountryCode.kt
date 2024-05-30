package com.jotrorox.napi.util

/**
 * Represents the country codes used in the API.
 *
 * Each enum entry represents a country code, and the associated string value is the actual code used in the API.
 *
 * @property code The actual country code used in the API.
 */
enum class CountryCode(private val code: String) {
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
    US("us");  // United States
    
    fun getCode(): String = code
}