package com.project.adminklikkerja.view

import android.net.Uri

class PostView {

    interface View {
        fun handleResponse(message: String)
        fun onSuccessPost(message: String)
        fun hideProgressBar()
        fun showProgressBar(progress: Int)
    }

    interface Presenter {
        fun uploadToDatabase(
            lessonKey: String,
            categoryKey: String,
            subCategoryKey: String,
            listSubKey: String?
        )
        fun uploadFileDocument(result: Uri)
    }
}