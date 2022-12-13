package com.example.vmagardener.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class PlantModel(
    val id: Int,
    val name: String?,
    val image: String?,/* Uri, daher String */
    val description: String?,
    val plantDate: String?,
    val location: String?,
    val latitude: Double,
    val longitude: Double,
    val intvWater: String?,
    val intvCut: String?,
    val intvFertilize: String?


) :Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(image)
        parcel.writeString(description)
        parcel.writeString(plantDate)
        parcel.writeString(location)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(intvWater)
        parcel.writeString(intvCut)
        parcel.writeString(intvFertilize)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlantModel> {
        override fun createFromParcel(parcel: Parcel): PlantModel {
            return PlantModel(parcel)
        }

        override fun newArray(size: Int): Array<PlantModel?> {
            return arrayOfNulls(size)
        }
    }
}



