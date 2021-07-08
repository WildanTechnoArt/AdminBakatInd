package com.project.adminklikkerja.view

import com.project.adminklikkerja.model.TeacherModel

class TeacherApprovedView {

    interface View {
        fun onSuccess(message: String)
        fun handleError(message: String)
        fun showProgressBar()
        fun hideProgressBar()
    }

    interface Presenter {
        fun postData(userId: String?, data: TeacherModel)
    }
}