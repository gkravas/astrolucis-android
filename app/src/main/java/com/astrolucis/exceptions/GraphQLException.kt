package com.astrolucis.exceptions

import com.apollographql.apollo.api.Error

class GraphQLException(val tag: String, val errors: List<Error>): RuntimeException() {
}