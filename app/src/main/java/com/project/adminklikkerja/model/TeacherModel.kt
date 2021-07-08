package com.project.adminklikkerja.model

data class TeacherModel (
    var username: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var experience: String? = null,
    var address: String? = null,
    var teacher: Boolean? = null,
    var approve: Boolean? = null,
    var datetime: String? = null,
    var active: Boolean? = null
)