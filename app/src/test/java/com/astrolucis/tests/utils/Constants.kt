package com.astrolucis.tests.utils

class Constants {
    companion object {
        const val NON_EXPIRING_JWT: String = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJBc3Ryb0x1Y2lzIiwiaWF0IjoxNTIxNzkwOTc3LCJleHAiOjI1MzE2MzQxODMsImF1ZCI6Imh0dHBzOi8vd3d3LmFzdHJvbHVjaXMuZ3IiLCJzdWIiOiJpbmZvQGFzdHJvbHVjaXMuZ3IifQ.s1Y2BU-TdCdQ83TcfC7kMV_BnZeqcby768F526cVPvg"
        const val EXPIRED_JWT: String = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJBc3Rybzp1Y2lzIiwiaWF0IjoxNTIxNTczMzUyLCJleHAiOjE1MjE2NTk3NjYsImF1ZCI6Imh0dHBzOi8vd3d3LmFzdHJvbHVjaXMuZ3IiLCJzdWIiOiJ0ZXN0QGFzdHJvbHVjaXMuZ3IifQ.OiJ7djzghA1I4jvASoYSOx1wvMzJwWi9QDeCYWel36g"

        const val EMPTY = ""
        const val NATAL_DATE_ID: Long = 1
        const val TEST_EMAIL: String = "test@astrolucis.gr"
        const val VALID_LIVING_LOCATION = "Greece"
        const val VALID_BIRTH_LOCATION = "Greece"
        const val VALID_NAME = "me"
        const val SERVER_SIDE_FULL_DATE = "Thu Aug 16 1984 21:30:00 GMT+0200 (CEST)"
    }
}