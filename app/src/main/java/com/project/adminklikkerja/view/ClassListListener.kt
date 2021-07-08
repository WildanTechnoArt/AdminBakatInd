package com.project.adminklikkerja.view

interface ClassListListener {
    fun onDeleteClass(lessonId: String, classId: String)
    fun onEditClass(
        teacherId: String,
        subclassName: String,
        lessonId: String,
        classId: String,
        lessonImg: String,
        subscribe: Boolean?,
        link: String
    )
}