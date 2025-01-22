package com.example.quickcook_project.authentications

import java.io.IOException
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.JsonArray


object EmailService {

    private const val API_KEY = "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" // API SendGrid key
    private const val SENDER_EMAIL = "19carlito09@gmail.com" // good email address to send emails
    private const val SENDGRID_URL = "https://api.sendgrid.com/v3/mail/send"

    private val client = OkHttpClient() // OkHttpClient instanciate


    fun sendVerificationCode(email: String, code: String): Boolean {
        val jsonBody = JsonObject().apply {
            // personalizations array (list of recipients)
            add("personalizations", JsonArray().apply {
                add(JsonObject().apply {
                    add("to", JsonArray().apply {
                        add(JsonObject().apply {
                            addProperty("email", email)
                        })
                    })
                    addProperty("subject", "Your Verification Code")
                })
            })

            // from address
            add("from", JsonObject().apply {
                addProperty("email", SENDER_EMAIL)
            })

            // content array
            add("content", JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("type", "text/plain")
                    addProperty("value", "Your verification code is: $code. It will expire in 10 minutes.")
                })
            })
        }

        // convert the JSON to RequestBody
        val mediaType = "application/json".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        // build the HTTP request
        val request = Request.Builder()
            .url(SENDGRID_URL)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $API_KEY")
            .addHeader("Content-Type", "application/json")
            .build()

        // execute the request
        return try {
            client.newCall(request).execute().use { response ->
                println("SendGrid Response Code: ${response.code}")
                println("SendGrid Response Body: ${response.body?.string()}")
                response.isSuccessful
            }
        } catch (e: IOException) {
            e.printStackTrace()
            println("SendGrid Exception: ${e.message}")
            false
        }
    }
}