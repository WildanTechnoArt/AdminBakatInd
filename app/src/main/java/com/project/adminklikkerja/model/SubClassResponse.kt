package com.project.adminklikkerja.model

data class SubClassResponse(
    var classid: String? = null,
    var lessonid: String? = null,
    var lessonname: String? = null,
    var classname: String? = null,
    var image: String? = null,
    var subscribe: String? = null,
    var bonus: String? = null,
    var teacher: Boolean? = null
)