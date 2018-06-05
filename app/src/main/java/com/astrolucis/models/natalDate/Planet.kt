package com.astrolucis.models.natalDate

import javax.annotation.Generated
import com.google.gson.annotations.SerializedName

@Generated("com.robohorse.robopojogenerator")
data class Planet(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("angle")
	val angle: Double? = null,

	@field:SerializedName("time")
	val time: String? = null,

	@field:SerializedName("house")
	val house: Int? = null,

	@field:SerializedName("retrogate")
	val retrogate: Boolean? = null,

	@field:SerializedName("latitude")
	val latitude: Double? = null,

	@field:SerializedName("sign")
	val sign: String? = null,

	@field:SerializedName("speed")
	val speed: Double? = null,

	@field:SerializedName("longitude")
	val longitude: Double? = null
)