package com.project.enjoyfood.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

// 코틀린에서 역직렬화 하려면 디폴트 생성자 대신 = "" 등으로 초기화구문 넣어줘야 한다.
@Parcelize
data class SearchResultEntity(
    val imageUri : String = "",
    val name: String = "",
    val addrDong : String = "",
    val desc : String = "", // 음식점 소개 문구
    var numOfHearts : Int = 0,
    // val review : String // 추후 객체로
    var numOfReviews : Int = 0,
    val latLngEntity: LocationLatLngEntity = LocationLatLngEntity(0f,0f)
) : Parcelable
