package com.astrolucis.utils

import android.os.Bundle
import com.apollographql.apollo.CustomTypeAdapter
import com.astrolucis.di.App
import com.astrolucis.models.natalDate.Chart
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson

class JsonCustomTypeAdapter : CustomTypeAdapter<Chart> {
    override fun decode(value: String): Chart? {
        return try {
            Gson().fromJson<Chart>(value, Chart::class.java)
        } catch (t: Throwable) {
            FirebaseAnalytics.getInstance(App.instance)
                    .logEvent("natal_date_chart_parse_error",
                        Bundle().also {
                            it.putString("JSON", value)
                        }
            )
            null
        }
    }

    override fun encode(value: Chart): String {
        return value.toString()
    }
}