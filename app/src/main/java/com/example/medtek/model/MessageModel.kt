package com.example.medtek.model

import com.example.medtek.network.response.ChatResponse
import com.google.gson.Gson

/**
 * Model untuk get Message dari Socket
 */

class MessageModel(
        val chat: ChatResponse
) {

    companion object {
        fun parseFrom(value: Array<Any>): MessageModel? {
            val messageData = value[1] as org.json.JSONObject
            try {
                return Gson().fromJson(messageData.toString(), MessageModel::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }

}