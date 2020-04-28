package com.example.whatsapp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Users(val status : String, val name :String, val uid:String, val image : String) : Parcelable {
    constructor() : this(" ", " ", " ", " ")
}