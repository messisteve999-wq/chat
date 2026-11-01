package com.steve.chat

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object NotificationStore {
    private const val PREFS = "chat_notifications"
    private const val KEY = "items"

    @Synchronized
    fun add(context: Context, item: ChatMessage) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val list = JSONArray(prefs.getString(KEY, "[]"))
        list.put(JSONObject().apply {
            put("packageName", item.packageName)
            put("contact", item.contact)
            put("message", item.message)
            put("timestamp", item.timestamp)
        })
        // Limiter la taille locale pour protéger les appareils à faible mémoire.
        val trimmed = JSONArray()
        val start = (list.length() - 500).coerceAtLeast(0)
        for (i in start until list.length()) trimmed.put(list.getJSONObject(i))
        prefs.edit().putString(KEY, trimmed.toString()).apply()
    }

    fun read(context: Context): JSONArray {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return JSONArray(prefs.getString(KEY, "[]"))
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY).apply()
    }
}
