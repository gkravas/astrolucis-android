package com.astrolucis

import com.apollographql.apollo.api.Error
import com.astrolucis.exceptions.GraphQLException
import com.astrolucis.services.interfaces.NatalDateService
import com.astrolucis.services.interfaces.UserService
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response

class ErrorFactory {
    companion object {
        fun createInvalidCredentialsError(): HttpException {
            val errorBody = ""
            val error: Response<String> = Response.error(
                    401,
                    ResponseBody.create(MediaType.parse("application/json"), errorBody)
            )
            return HttpException(error)
        }

        fun createDuplicateUserError(): HttpException {
            val errorBody = "{\"error\":{\"name\":\"ServiceError\",\"type\":\"unique violation\",\"message\":\"email must be unique\",\"field\":\"email\"}}"
            val error: Response<String> = Response.error(
                    400,
                    ResponseBody.create(MediaType.parse("application/json"), errorBody)
            )
            return HttpException(error)
        }

        fun createEmailEmptyError(): HttpException {
            val errorBody = "{\"error\":{\"name\":\"ServiceError\",\"type\":\"notNull violation\",\"message\":\"email must not be null\",\"field\":\"email\"}}"
            val error: Response<String> = Response.error(
                    400,
                    ResponseBody.create(MediaType.parse("application/json"), errorBody)
            )
            return HttpException(error)
        }

        fun createServerError(): HttpException {
            val errorBody = ""
            val error: Response<String> = Response.error(
                    500,
                    ResponseBody.create(MediaType.parse("application/json"), errorBody)
            )
            return HttpException(error)
        }

        fun createBirthLocationError(): GraphQLException {
            val errorBody = "{\"error\":{\"name\":\"ExternalServiceError\",\"type\":\"timezone error\",\"message\":\"email must not be null\",\"field\":\"email\"}}"
            return createGraphQLError(NatalDateService.TAG, errorBody)
        }

        fun createLivingLocationError(): GraphQLException {
            val errorBody = "{\"error\":{\"name\":\"ExternalServiceError\",\"type\":\"timezone error\",\"message\":\"email must not be null\",\"field\":\"email\"}}"
            return createGraphQLError(UserService.TAG, errorBody)
        }

        fun createBirthDateError(): GraphQLException {
            val errorBody = "{\"error\":{\"name\":\"ServiceError\",\"type\":\"notNull violation\",\"message\":\"invalid birth date\",\"field\":\"birthDate\"}}"
            return createGraphQLError(NatalDateService.TAG, errorBody)
        }

        fun createGraphQLError(tag:String, json: String): GraphQLException {
            val attributes: HashMap<String, Any> = HashMap()
            JSONObject(json).getJSONObject("error").let {
                for (key in it.keys()) {
                    attributes[key] = it[key]
                }
            }
            return GraphQLException(tag, listOf(Error(null, null, attributes)))
        }
    }
}