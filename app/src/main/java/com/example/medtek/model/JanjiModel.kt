package com.example.medtek.model

import com.example.medtek.network.response.JanjiResponse
import com.google.gson.Gson

/**
 * Model untuk get Janji dari Socket
 */

class JanjiModel(
        val janji: JanjiResponse
) {
    companion object {
        fun parseFrom(value: Array<Any>): JanjiModel? {
            val messageData = value[1] as org.json.JSONObject
            try {
                return Gson().fromJson(messageData.toString(), JanjiModel::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}