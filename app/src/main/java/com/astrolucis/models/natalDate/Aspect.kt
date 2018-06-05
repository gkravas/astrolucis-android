package com.astrolucis.models.natalDate

import javax.annotation.Generated
import com.google.gson.annotations.SerializedName

@Generated("com.robohorse.robopojogenerator")
data class Aspect(

	@field:SerializedName("planet2")
	val planet2: String? = null,

	@field:SerializedName("planet1")
	val planet1: String? = null,

	@field:SerializedName("angle")
	val angle: Int? = null
)