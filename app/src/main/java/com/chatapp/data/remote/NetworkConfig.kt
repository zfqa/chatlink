package com.chatapp.data.remote

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

object NetworkConfig {
    // Use adb reverse tcp:3000 tcp:3000 for real device testing
    var BASE_URL = "http://127.0.0.1:3000"

    val gson = Gson()

    data class ApiResponse<T>(val code: Int, val message: String, val data: T?)

    /**
     * POST request with JSON body, returns raw response string.
     */
    fun postJson(path: String, body: Any, token: String? = null): String {
        val url = URL("$BASE_URL/api/v1$path")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Accept", "application/json")
        if (token != null) conn.setRequestProperty("Authorization", "Bearer $token")
        conn.doOutput = true
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        val json = gson.toJson(body)
        conn.outputStream.use { os: OutputStream -> os.write(json.toByteArray()) }
        return readResponse(conn)
    }

    /**
     * GET request, returns raw response string.
     */
    fun getJson(path: String, token: String? = null): String {
        val url = URL("$BASE_URL/api/v1$path")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Accept", "application/json")
        if (token != null) conn.setRequestProperty("Authorization", "Bearer $token")
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        return readResponse(conn)
    }

    private fun readResponse(conn: HttpURLConnection): String {
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val body = stream?.bufferedReader()?.use(BufferedReader::readText) ?: ""
        if (code !in 200..299) throw ApiException(code, body)
        return body
    }

    inline fun <reified T> parseResponse(raw: String): T {
        return gson.fromJson(raw, object : TypeToken<T>() {}.type)
    }

    class ApiException(val httpCode: Int, val responseBody: String) : Exception("HTTP $httpCode: $responseBody")
}
