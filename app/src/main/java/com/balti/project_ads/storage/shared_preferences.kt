package com.balti.project_ads.storage

import android.content.Context
import android.content.SharedPreferences

class shared {
    companion object{
        fun save_id(context: Context, value: String) {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("id", value)
            editor.apply()
        }

        fun get_id(context: Context): String {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            return sharedPreferences.getString("id","") ?: ""
        }
    }
}