package com.project.adminklikkerja.activity

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminklikkerja.R
import com.project.adminklikkerja.model.ClassResponse
import com.project.adminklikkerja.model.SubjectResponse
import com.project.adminklikkerja.utils.UtilsConstant.SUBCLASS_DATA
import com.project.adminklikkerja.utils.UtilsConstant.SUB_CATEGORY_KEY
import com.project.adminklikkerja.utils.UtilsConstant.SUB_CATEGORY_NAME
import kotlinx.android.synthetic.main.activity_duplicate.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.util.*
import kotlin.collections.ArrayList

class DuplicateActivity : AppCompatActivity() {

    private var mLessonId: String? = null
    private var mSubclassName: String? = null
    private var mClassId: String? = null
    private var mSubclassId: String? = null
    private var subClassList = ArrayList<SubjectResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_duplicate)
        prepare()
        getLessonList()
    }

    private fun prepare() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Duplikat Kelas"
        }

        subClassList = intent?.getParcelableArrayListExtra(SUBCLASS_DATA)!!
        mSubclassName = intent?.getStringExtra(SUB_CATEGORY_NAME).toString()
        mSubclassId = intent?.getStringExtra(SUB_CATEGORY_KEY).toString()

        input_subclass.setText(mSubclassName)

        bindProgressButton(btn_duplicate_class)
        btn_duplicate_class.attachTextChangeAnimator()

        btn_duplicate_class.setOnClickListener {
            if (mClassId.toString() == "null" || mLessonId.toString() == "null" || mSubclassName.toString() == "null") {
                Toast.makeText(this, "Tidak boleh ada data yang kosong", Toast.LENGTH_SHORT)
                    .show()
            } else {
                btn_duplicate_class.showProgress { progressColor = Color.WHITE }
                createSubClass()
            }
        }
    }

    private fun createSubClass() {
        val data = ClassResponse()
        data.name = input_subclass.text.toString()
        data.date = Calendar.getInstance().time

        val db = FirebaseFirestore.getInstance()
        db.collection("lessons")
            .document(mLessonId.toString())
            .collection("category")
            .document(mClassId.toString())
            .collection("subCategory")
            .document(mSubclassId.toString())
            .set(data)
            .addOnSuccessListener {
                pasteAllSubject()
            }
            .addOnFailureListener {
                btn_duplicate_class.hideProgress(R.string.btn_duplicate_class)
                Toast.makeText(
                    this,
                    it.localizedMessage?.toString(),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
    }

    private fun pasteAllSubject() {
        for (document in subClassList) {
            val data = SubjectResponse()
            data.date = document.date
            data.link = document.link
            data.pdf = document.pdf
            data.video = document.video
            data.name = document.name

            val db = FirebaseFirestore.getInstance()
            db.collection("lessons")
                .document(mLessonId.toString())
                .collection("category")
                .document(mClassId.toString())
                .collection("subCategory")
                .document(mSubclassId.toString())
                .collection("listSub")
                .document()
                .set(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "Kelas berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    btn_duplicate_class.hideProgress(R.string.btn_duplicate_class)
                    Toast.makeText(
                        this,
                        it.localizedMessage?.toString(),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
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
            val subjectsRef = rootRef.collection("lessons").document(lessonId)
                .collection("category")
            val classList: MutableList<String?> = ArrayList()
            val classIdList: MutableList<String?> = ArrayList()
            val adapter =
                ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, classList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            input_class.setAdapter(adapter)
            input_class.setOnItemClickListener { _, _, position, _ ->
                mClassId = classIdList[position].toString()
            }

            classIdList.clear()
            classList.clear()

            subjectsRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val lessonName = document.getString("name")
                        classList.add(lessonName)
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