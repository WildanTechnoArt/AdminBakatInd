package com.project.adminklikkerja.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class SubjectResponse(
    var teacherid: String? = null,
    var lessonid: String? = null,
    var lessonname: String? = null,
    var classid: String? = null,
    var classname: String? = null,
    var subclassname: String? = null,
    var subclassId: String? = null,
    var date: Date? = null,
    var link: String? = null,
    var video: String? = null,
    var pdf: String? = null,
    var name: String? = null,
    var number: Long? = null,
    var audio: String? = null,
    var image: String? = null,
    var subscribe: Boolean? = null
): Parcelable