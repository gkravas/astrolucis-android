package com.astrolucis.models

import android.content.Context
import com.astrolucis.R

public enum class NatalType(val value: String, val resourceId: Int) {
    UNKNOWN("unknown", R.string.natalType_unknown),
    MALE("male", R.string.natalType_male),
    FEMALE("female", R.string.natalType_female),
    FREE_SPIRIT("freeSpirit", R.string.natalType_freeSpirit), ;

    companion object {
        fun findBy(valueToFind: String): NatalType {
            return NatalType.values().find { it.value == valueToFind }?.let {
                return it
            } ?: UNKNOWN
        }

        fun findBy(resourceValueToFind: String, context: Context): NatalType? {
            return NatalType.values().find { context.getString(it.resourceId) == resourceValueToFind }
        }
    }
}
