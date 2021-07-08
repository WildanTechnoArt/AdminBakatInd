package com.project.adminklikkerja.view

interface SubCategoryListener {
    fun onUploadPdf(listSubKey: String)
    fun uploadLink(listSubKey: String)
    fun uploadVideo(listSubKey: String)
    fun onDelete(key: String)
    fun onEdit(subClassKey: String, subClassName: String)
}