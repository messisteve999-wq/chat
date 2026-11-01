package com.steve.chat

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object GeminiClient {

    private const val API_URL =
        "http://127.0.0.1:8787/v1/generate"

    fun generate(prompt: String): String {
        val connection = URL(API_URL).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 15000
            connection.readTimeout = 60000
            connection.doOutput = true
            connection.setRequestProperty(
                "Content-Type", "application/json"
            )

            val body = JSONObject()
                .put("prompt", prompt)
                .toString()

            connection.outputStream.use {
                it.write(body.toByteArray(Charsets.UTF_8))
            }

            val status = connection.responseCode
            val stream = if (status in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val response = stream.bufferedReader().use {
                it.readText()
            }

            val json = JSONObject(response)

            if (status !in 200..299) {
                return "Erreur serveur : " +
                    json.optString("details",
                        json.optString("error", "Erreur inconnue"))
            }

            if (!json.optBoolean("success", false)) {
                return json.optString("error", "Génération impossible")
            }

            return json.optString("text", "Aucune réponse reçue.")

        } finally {
            connection.disconnect()
        }
    }
}
