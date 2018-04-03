package com.astrolucis.services.repsonses

import com.astrolucis.models.User
import com.google.gson.annotations.SerializedName

data class LoginResponse(@SerializedName("user") val user: User,
                         @SerializedName("token") val token: String)