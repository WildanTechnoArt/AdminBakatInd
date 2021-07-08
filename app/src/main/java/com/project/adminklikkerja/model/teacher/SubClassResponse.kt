package com.project.adminklikkerja.model.teacher

import java.util.*

data class SubClassResponse (
    var lessonid: String? = null,
    var teacherid: String? = null,
    var image: String? = null,
    var lessonname: String? = null,
    var classid: String? = null,
    var classname: String? = null,
    var name: String? = null,
    var subclassId: String? = null,
    var subjectname: String? = null,
    var date: Date? = null,
    var approve: Boolean? = null,
)