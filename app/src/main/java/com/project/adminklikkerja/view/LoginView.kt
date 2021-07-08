package com.project.adminklikkerja.view

class LoginView {

    interface View {
        fun handleResponse(message: String)
        fun showProgressBar()
        fun hideProgressBar()
    }

    interface Presenter {
        fun requestLogin(email: String, password: String)
    }
}