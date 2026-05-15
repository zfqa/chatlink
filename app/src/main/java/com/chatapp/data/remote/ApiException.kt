package com.chatapp.data.remote

class ApiException(val httpCode: Int, val responseBody: String) : Exception("HTTP $httpCode: $responseBody")
