package com.project.adminklikkerja.view

import android.content.Context
import com.theartofdev.edmodo.cropper.CropImage

class TeacherClassView {

    interface View {
        fun onSuccessUpload(context: Context?, message: String?)
        fun handleResponse(message: String?)
        fun hideProgressBar()
        fun hideProgressBarEdit()
        fun showProgressBar()
        fun showProgressBarEdit()
    }

    interface Presenter {
        fun editLesson(
            teacherId: String,
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