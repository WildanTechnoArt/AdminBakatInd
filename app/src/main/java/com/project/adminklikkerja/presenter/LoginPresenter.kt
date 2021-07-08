package com.project.adminklikkerja.presenter

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.project.adminklikkerja.R
import com.project.adminklikkerja.utils.Validation.Companion.validateEmail
import com.project.adminklikkerja.utils.Validation.Companion.validateFields
import com.project.adminklikkerja.view.LoginView

class LoginPresenter(
    private val context: Context,
    private val view: LoginView.View
) : LoginView.Presenter {

    override fun requestLogin(email: String, password: String) {
        if (validateFields(email) || validateFields(password)) {
            view.handleResponse(context.getString(R.string.email_password_null))
        } else if (validateEmail(email)) {
            view.handleResponse(context.getString(R.string.email_not_valid))
        } else {
            if (email == "adminklikkerja@gmail.com") {
                view.showProgressBar()
                login(email, password)
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_user_not_found),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun login(email: String, password: String) {
        val mAuth = FirebaseAuth.getInstance()

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    Log.i("LoginActivity", "Login Success")

                } else {
                    view.hideProgressBar()
                    view.handleResponse((task.exception as FirebaseAuthException).errorCode)
                }
            }
    }
}