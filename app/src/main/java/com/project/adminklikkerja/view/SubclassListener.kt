package com.project.adminklikkerja.view

interface SubclassListener {
    fun onDuplicate(subClassKey: String, subclassName: String)
    fun onClick(key: String, name: String)
    fun onDelete(key: String, teacherId: String)
    fun onEdit(teacherId: String, categoryKey: String, categoryName: String, lessonImg: String, subscribe: Boolean)
}