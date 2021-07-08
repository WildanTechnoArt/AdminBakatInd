package com.project.adminklikkerja.view

interface CategoryListener {
    fun onClick(key: String, name: String, teacherId: String)
    fun onDelete(key: String)
    fun onEdit(categoryKey: String, categoryName: String, lessonImg: String, subscribe: Boolean, link: String)
}