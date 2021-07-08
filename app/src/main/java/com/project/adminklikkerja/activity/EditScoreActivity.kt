package com.project.adminklikkerja.activity

import android.content.pm.ActivityInfo
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminklikkerja.GlideApp
import com.project.adminklikkerja.R
import com.project.adminklikkerja.utils.UtilsConstant
import com.project.adminklikkerja.utils.UtilsConstant.CATEGORY_KEY
import com.project.adminklikkerja.utils.UtilsConstant.EMAIL
import com.project.adminklikkerja.utils.UtilsConstant.IMAGE
import com.project.adminklikkerja.utils.UtilsConstant.LESSON_KEY
import com.project.adminklikkerja.utils.UtilsConstant.USERNAME
import kotlinx.android.synthetic.main.activity_give_score.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class EditScoreActivity : AppCompatActivity() {

    private var mImage: String? = null
    private var mUsername: String? = null
    private var mEmail: String? = null
    private var mScore: Long? = null
    private var mMessage: String? = null
    private var mLessonId: String? = null
    private var mClassId: String? = null
    private var mUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_give_score)
        prepare()
        btn_send.setOnClickListener {
            mMessage = input_message.text.toString()
            mScore = input_score.text.toString().toLong()

            if (mScore.toString() == "null" || mMessage == "null") {
                Toast.makeText(this, "Tidak boleh ada data yang kosong", Toast.LENGTH_SHORT)
                    .show()
            } else {
                when {
                    mScore ?: 0 > 100 -> {
                        Toast.makeText(this, "Nilai tidak boleh lebih dari 100", Toast.LENGTH_SHORT)
                            .show()
                    }
                    mScore ?: 0 < 10 -> {
                        Toast.makeText(this, "Nilai tidak boleh kurang dari 10", Toast.LENGTH_SHORT)
                            .show()
                    }
                    else -> {
                        editData()
                    }
                }
            }
        }
    }

    private fun prepare() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Edit Nilai"

        bindProgressButton(btn_send)
        btn_send.attachTextChangeAnimator()

        mImage = intent.getStringExtra(IMAGE).toString()
        mUsername = intent.getStringExtra(USERNAME).toString()
        mEmail = intent.getStringExtra(EMAIL).toString()
        mLessonId = intent.getStringExtra(LESSON_KEY).toString()
        mClassId = intent.getStringExtra(CATEGORY_KEY).toString()
        mUserId = intent.getStringExtra(UtilsConstant.GET_PROFILE).toString()
        mScore = intent.getLongExtra(UtilsConstant.SCORE, 0)
        mMessage = intent.getStringExtra(UtilsConstant.MESSAGE).toString()

        GlideApp.with(applicationContext)
            .load(mImage)
            .placeholder(R.drawable.profile_placeholder)
            .into(img_profile)

        tv_username.text = mUsername
        tv_email.text = mEmail
        input_score.setText(mScore.toString())
        input_message.setText(mMessage)
    }

    private fun editData() {
        btn_send.showProgress { progressColor = Color.WHITE }

        val db = FirebaseFirestore.getInstance()

        val data = HashMap<String, Any?>()
        data["score"] = mScore
        data["message"] = mMessage

        db.collection("student")
            .document(mUserId.toString())
            .collection("score")
            .document(mClassId.toString())
            .update(data)
            .addOnSuccessListener {
                btn_send.hideProgress(R.string.btn_send)
                Toast.makeText(this, "Nilai berhasil diubah", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                btn_send.hideProgress(R.string.btn_send)
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }
}