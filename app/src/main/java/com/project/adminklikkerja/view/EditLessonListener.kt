package com.project.adminklikkerja.view

interface EditLessonListener {
    fun onEdit(lessonKey: String, lessonName: String, lessonImg: String)
    fun onDelete(lessonKey: String)
}