package com.astrolucis.models.natalDate

import javax.annotation.Generated
import com.google.gson.annotations.SerializedName

@Generated("com.robohorse.robopojogenerator")
data class Chart(

		@field:SerializedName("planets")
	val planets: List<Planet?>? = null,

		@field:SerializedName("houses")
	val houses: List<House?>? = null,

		@field:SerializedName("aspects")
	val aspects: List<Aspect?>? = null
)