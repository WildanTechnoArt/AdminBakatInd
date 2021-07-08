package com.project.adminklikkerja.activity

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminklikkerja.R
import com.project.adminklikkerja.model.SubClassResponse
import com.project.adminklikkerja.model.StudentModel
import com.project.adminklikkerja.utils.UtilsConstant
import kotlinx.android.synthetic.main.activity_add_subs_class.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class AddSubsClassActivity : AppCompatActivity() {

    private var mClassId: String? = null
    private var mLessonId: String? = null
    private var mUserId: String? = null
    private var mUsername: String? = null
    private var mUserEmail: String? = null
    private var isTeacher: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_subs_class)
        init()
        getLessonList()
    }

    private fun init() {
        try {
            setSupportActionBar(toolbar)
            supportActionBar?.title = "Tambah Kelas"

            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

            mUserId = intent?.getStringExtra(UtilsConstant.GET_PROFILE).toString()
            mUsername = intent?.getStringExtra(UtilsConstant.USERNAME).toString()
            mUserEmail = intent?.getStringExtra(UtilsConstant.EMAIL).toString()
            isTeacher = intent?.getBooleanExtra(UtilsConstant.IS_TEACHER, false)

            bindProgressButton(btn_add_class)
            btn_add_class.attachTextChangeAnimator()

            val subscribeList = listOf("1 Bulan", "3 Bulan", "6 Bulan", "12 Bulan", "Life Time")
            val subsAdapter =
                ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, subscribeList)
            (input_subscribe as? AutoCompleteTextView)?.setAdapter(subsAdapter)

            val bonusList = listOf("1 Bulan", "3 Bulan", "6 Bulan")
            val bonusAdapter =
                ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, bonusList)
            (input_bonus as? AutoCompleteTextView)?.setAdapter(bonusAdapter)

            btn_add_class.setOnClickListener {
                val subscribe = input_subscribe.text.toString()
                val bonus = input_bonus.text.toString()

                if (mClassId.toString() == "null" || mLessonId.toString() == "null" || subscribe == "null") {
                    Toast.makeText(this, "Tidak boleh ada data yang kosong", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val data = SubClassResponse()
                    data.classname = input_class.text.toString()
                    data.lessonname = input_lesson.text.toString()
                    data.classid = mClassId
                    data.lessonid = mLessonId
                    data.subscribe = subscribe
                    data.bonus = bonus
                    data.teacher = isTeacher

                    btn_add_class.showProgress { progressColor = Color.WHITE }
                    val query = FirebaseFirestore.getInstance()
                    query.collection("subscribe")
                        .document(mUserId.toString())
                        .collection("class")
                        .document(mClassId.toString())
                        .set(data)
                        .addOnSuccessListener {
                            saveToClassList()
                        }.addOnFailureListener {
                            btn_add_class.hideProgress(R.string.btn_add_class)
                            Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun saveToClassList() {
        val data = StudentModel()
        data.email = mUserEmail
        data.username = mUsername
        data.classid = mClassId
        data.lessonid = mLessonId
        data.teacher = isTeacher

        val query = FirebaseFirestore.getInstance()
        query.collection("lessons")
            .document(mLessonId.toString())
            .collection("category")
            .document(mClassId.toString())
            .collection("studentList")
            .document(mUserId.toString())
            .set(data)
            .addOnSuccessListener {
                btn_add_class.hideProgress(R.string.btn_add_class)
                Toast.makeText(this, "Kelas berhasil ditambahkan", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }.addOnFailureListener {
                btn_add_class.hideProgress(R.string.btn_add_class)
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    private fun getLessonList() {
        try {
            val rootRef = FirebaseFirestore.getInstance()
            val subjectsRef = rootRef.collection("lessons")
            val lessonList: MutableList<String?> = ArrayList()
            val lessonIdList: MutableList<String?> = ArrayList()
            val adapter =
                ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, lessonList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            input_lesson.setAdapter(adapter)
            input_lesson.setOnItemClickListener { _, _, position, _ ->
                input_class.setText("")
                mLessonId = lessonIdList[position].toString()
                getClassList(lessonIdList[position].toString())
            }

            subjectsRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val lessonName = document.getString("lesson")
                        lessonList.add(lessonName)
                        lessonIdList.add(document.id)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun getClassList(lessonId: String) {
        try {
            val rootRef = FirebaseFirestore.getInstance()
            val subjectsRef =
                rootRef.collection("lessons").document(lessonId).collection("category")
            val classList: MutableList<String?> = ArrayList()
            val classIdList: MutableList<String?> = ArrayList()
            val adapter =
                ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, classList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            input_class.setAdapter(adapter)
            input_class.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    mClassId = classIdList[position].toString()
                }

            classIdList.clear()
            classList.clear()

            subjectsRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val className = document.getString("name")
                        classList.add(className)
                        classIdList.add(document.id)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}