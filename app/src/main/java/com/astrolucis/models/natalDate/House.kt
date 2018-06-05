package com.astrolucis.models.natalDate

import javax.annotation.Generated
import com.google.gson.annotations.SerializedName

@Generated("com.robohorse.robopojogenerator")
data class House(

	@field:SerializedName("start")
	val start: Double? = null,

	@field:SerializedName("sign")
	val sign: String? = null,

	@field:SerializedName("index")
	val index: Int? = null,

	@field:SerializedName("end")
	val end: Double? = null
)