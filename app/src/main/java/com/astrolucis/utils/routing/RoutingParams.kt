package com.astrolucis.utils.routing

import android.net.Uri
import android.os.Bundle
import kotlin.reflect.KClass

data class RoutingParams(val kClass: KClass<*>, private val uri: Uri?) {
    val bundle: Bundle

    init {
        bundle = Bundle().apply {
            uri?.queryParameterNames?.forEach {
                this.putString(it, uri.getQueryParameter(it))
            }
        }
    }
}
