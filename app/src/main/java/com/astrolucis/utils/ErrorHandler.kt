package com.astrolucis.utils

import com.astrolucis.R
import com.astrolucis.exceptions.GraphQLException
import com.astrolucis.services.interfaces.NatalDateService
import com.astrolucis.services.interfaces.UserService
import org.json.JSONObject
import retrofit2.HttpException

class ErrorHandler {
    companion object {
        private const val NAME: String = "name"
        private const val TYPE: String = "type"
        private const val FIELD: String = "field"
        private const val ERROR: String = "error"
        private const val NO_VALUE: String = ""

        private const val NAME_EXTERNAL_SERVICE_ERROR: String = "ExternalServiceError"
        private const val NAME_SERVICE_ERROR: String = "ServiceError"

        private const val TYPE_TIMEZONE_ERROR: String = "timezone error"
        private const val UNIQUE_VIOLATION: String = "unique violation"
        private const val NOT_NULL_VIOLATION: String = "notNull violation"

        private const val FIELD_BIRTH_DATE: String = "birthDate"
        private const val FIELD_EMAIL: String = "email"

        fun handleLoginRegister(throwable: Throwable): ErrorPresentation {
            var result = ErrorPresentation(R.string.error_defaultTitle, R.string.error_general)
            if (throwable is HttpException) {
                val httpCode = throwable.code()
                when (httpCode) {
                    401 -> result = ErrorPresentation(R.string.error_defaultTitle, R.string.error_invalidCredential)
                    400 -> {
                        val json = JSONObject(throwable.response().errorBody()?.string()).optJSONObject(ERROR)
                        when (json.optString(NAME, NO_VALUE)) {
                            NAME_SERVICE_ERROR ->
                                when (json.optString(FIELD)) {
                                    FIELD_EMAIL -> {
                                        when (json.optString(TYPE)) {
                                            UNIQUE_VIOLATION -> result = ErrorPresentation(R.string.error_defaultTitle, R.string.error_notUniqueEmail)
                                            NOT_NULL_VIOLATION -> result = ErrorPresentation(R.string.error_defaultTitle, R.string.error_nullEmail)
                                        }
                                    }
                                }
                        }
                    }
                }
            }
            return result
        }

        fun handleNatalDateError(throwable: Throwable): ErrorPresentation {
            var result = ErrorPresentation(R.string.error_defaultTitle, R.string.error_general)
            if (throwable is GraphQLException) {
                val errorAttributes = throwable.errors.first().customAttributes()
                when(throwable.tag) {
                    UserService.TAG -> {
                        when (errorAttributes[NAME]) {
                            NAME_EXTERNAL_SERVICE_ERROR ->
                                when (errorAttributes[TYPE]) {
                                    TYPE_TIMEZONE_ERROR -> result = ErrorPresentation(R.string.error_defaultTitle, R.string.error_invalidLivingLocation)
                                }
                        }
                    }
                    NatalDateService.TAG -> {
                        when (errorAttributes[NAME]) {
                            NAME_EXTERNAL_SERVICE_ERROR ->
                                when (errorAttributes[TYPE]) {
                                    TYPE_TIMEZONE_ERROR -> result = ErrorPresentation(R.string.error_defaultTitle, R.string.error_invalidBirthLocation)
                                }
                            NAME_SERVICE_ERROR ->
                                when (errorAttributes[FIELD]) {
                                    FIELD_BIRTH_DATE -> result = ErrorPresentation(R.string.error_defaultTitle, R.string.error_invalidBirthDate)
                                }
                        }
                    }
                }
            }
            return result
        }
    }
}