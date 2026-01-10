package com.bizcard.note.data

import java.util.Date

data class BizCard(
    val id: String? = null,
    val name: String = "",
    val company: String = "",
    val email: String = "",
    val phone: String = "",
    val memo: String = "",
    val meetingPlace: String = "",
    val meetingDate: Date? = null,
    val cardImageUri: String? = null,
    val facePhotoUri: String? = null,
    val registrationDate: Date? = null
)

