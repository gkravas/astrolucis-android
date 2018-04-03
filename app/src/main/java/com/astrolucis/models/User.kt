package com.astrolucis.models

import com.google.gson.annotations.SerializedName
import java.util.*

public data class User(@SerializedName("id") val id: Long,
                @SerializedName("email") val email: String,
                @SerializedName("natalDates") val natalDates: Array<NatalDate>) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (email != other.email) return false
        if (!Arrays.equals(natalDates, other.natalDates)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + Arrays.hashCode(natalDates)
        return result
    }
}