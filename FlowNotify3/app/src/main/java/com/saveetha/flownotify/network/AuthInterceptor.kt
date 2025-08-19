package com.saveetha.flownotify.network

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Use the same SharedPreferences name as LoginActivity
        val sharedPreferences = context.getSharedPreferences("FlowNotifyPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("access_token", null)

        val requestBuilder = chain.request().newBuilder()

        if (!token.isNullOrEmpty()) {
            // Attach token with Bearer prefix
            requestBuilder.addHeader("Authorization", "Bearer $token")
            Log.d("AuthInterceptor", "Attaching token: $token")
        } else {
            Log.d("AuthInterceptor", "No token found, request will be sent without Authorization header")
        }

        return chain.proceed(requestBuilder.build())
    }
}
