package com.project.adminklikkerja.view

import android.content.Context
import com.theartofdev.edmodo.cropper.CropImage

class AddLessonView {

    interface View {
        fun onSuccessUpload(context: Context?, message: String?)
        fun handleResponse(message: String?)
        fun hideProgressBar()
        fun hideProgressBarEdit()
        fun showProgressBar()
    }

    interface Presenter {
        fun uploadLesson(lessonName: String, result: CropImage.ActivityResult)
        fun editLesson(lessonKey: String, lessonName: String, result: CropImage.ActivityResult?, isEditWithImage: Boolean)
    }
}