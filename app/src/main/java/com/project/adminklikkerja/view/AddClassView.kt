package com.project.adminklikkerja.view

import android.content.Context
import com.theartofdev.edmodo.cropper.CropImage

class AddClassView {

    interface View {
        fun onSuccessUpload(context: Context?, message: String?)
        fun handleResponse(message: String?)
        fun hideProgressBar()
        fun hideProgressBarEdit()
        fun showProgressBar()
        fun showProgressBarEdit()
    }

    interface Presenter {
        fun uploadLesson(
            lessonKey: String,
            className: String,
            result: CropImage.ActivityResult,
            isSubscribe: Boolean?,
            link: String
        )

        fun editLesson(
            isEditWithImage: Boolean,
            lessonKey: String,
            classKey: String,
            className: String,
            result: CropImage.ActivityResult?,
            subscribe: Boolean,
            link: String
        )
    }
}