package com.astrolucis.utils

import com.auth0.android.jwt.DecodeException
import com.auth0.android.jwt.JWT

class JWTUtils {
    companion object {
        private const val MIN_EXPIRED_TIME_SPAN: Long = 60 * 60

        fun isLoggedIn(token: String): Boolean {
            return try {
                !JWT(token).isExpired(MIN_EXPIRED_TIME_SPAN)
            } catch (e: Throwable) {
                when(e) {
                    is DecodeException,
                    is NullPointerException -> {
                        return false
                    } else -> {
                    throw  e
                }
                }
            }
        }
    }
}