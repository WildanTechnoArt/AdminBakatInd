package com.project.adminklikkerja.model

data class ScoreResponse (
    var lessonid: String? = null,
    var classid: String? = null,
    var score: Long? = null,
    var message: String? = null,
    var rated: Boolean? = null,
    var userId: String? = null
)