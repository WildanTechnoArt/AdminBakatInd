package com.project.adminklikkerja.activity

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.project.adminklikkerja.GlideApp
import com.project.adminklikkerja.R
import com.project.adminklikkerja.presenter.LoginPresenter
import com.project.adminklikkerja.utils.UtilsConstant.PLAY_SERVICES_RESOLUTION_REQUEST
import com.project.adminklikkerja.view.LoginView
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), LoginView.View {

    private lateinit var presenter: LoginView.Presenter

    private lateinit var mEmail: String
    private lateinit var mPassword: String

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener
    private lateinit var mAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        prepare()
        checkPlayServices()
        setupListener()
    }

    override fun handleResponse(message: String) {
        when (message) {
            "ERROR_USER_NOT_FOUND" -> Toast.makeText(
                this, getString(R.string.error_user_not_found),
                Toast.LENGTH_SHORT
            ).show()

            "ERROR_WRONG_PASSWORD" -> Toast.makeText(
                this, getString(R.string.error_wrong_password),
                Toast.LENGTH_SHORT
            ).show()

            else -> Toast.makeText(
                this, message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun showProgressBar() {
        btn_login.showProgress { progressColor = Color.WHITE }
    }

    override fun hideProgressBar() {
        btn_login.hideProgress(R.string.btn_login)
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener)
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(mAuthListener)
    }

    private fun prepare() {
        login_layout.visibility = View.GONE

        mAnalytics = FirebaseAnalytics.getInstance(this)
        mAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
        mAuth = FirebaseAuth.getInstance()

        GlideApp.with(this)
            .load(R.drawable.logo_bakain_transparan)
            .into(img_logo)

        presenter = LoginPresenter(this, this)
        bindProgressButton(btn_login)
        btn_login.attachTextChangeAnimator()

        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                login_layout.visibility = View.VISIBLE
            }
        }
    }

    private fun setupListener() {
        btn_login.setOnClickListener {
            mEmail = input_email.text.toString().trim()
            mPassword = input_password.text.toString().trim()
            presenter.requestLogin(mEmail, mPassword)
        }
    }

    private fun checkPlayServices() {
        val googleAPI = GoogleApiAvailability.getInstance()
        val result = googleAPI.isGooglePlayServicesAvailable(this)
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(
                    this, result,
                    PLAY_SERVICES_RESOLUTION_REQUEST
                ).show()
            }
        }
    }
}