package com.project.adminklikkerja.activity.techer

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminklikkerja.R
import com.project.adminklikkerja.adapter.teacher.TeacherSubjectListAdapter
import com.project.adminklikkerja.model.SubjectResponse
import com.project.adminklikkerja.utils.UtilsConstant
import com.project.adminklikkerja.utils.UtilsConstant.CATEGORY_KEY
import com.project.adminklikkerja.utils.UtilsConstant.CATEGORY_NAME
import com.project.adminklikkerja.utils.UtilsConstant.LESSON_KEY
import com.project.adminklikkerja.utils.UtilsConstant.LESSON_NAME
import com.project.adminklikkerja.utils.UtilsConstant.SUB_CATEGORY_KEY
import com.project.adminklikkerja.utils.UtilsConstant.SUB_CATEGORY_NAME
import kotlinx.android.synthetic.main.activity_subclass_list.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class TeacherSubjectListActivity : AppCompatActivity() {

    private var getLesson: String? = null
    private var getLessonKey: String? = null
    private var getCategory: String? = null
    private var getCategoryKey: String? = null
    private var getSubCategory: String? = null
    private var getSubCategoryKey: String? = null
    private var teacherId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subclass_list)
        prepare()
    }

    private fun prepare() {
        setSupportActionBar(toolbar)

        getLesson = intent.getStringExtra(LESSON_NAME).toString()
        getLessonKey = intent.getStringExtra(LESSON_KEY).toString()
        getCategory = intent.getStringExtra(CATEGORY_NAME).toString()
        getCategoryKey = intent.getStringExtra(CATEGORY_KEY).toString()
        getSubCategory = intent.getStringExtra(SUB_CATEGORY_NAME).toString()
        getSubCategoryKey = intent.getStringExtra(SUB_CATEGORY_KEY).toString()
        teacherId = intent.getStringExtra(UtilsConstant.GET_PROFILE).toString()

        supportActionBar?.title = getSubCategory

        rv_category?.layoutManager = LinearLayoutManager(this)
        rv_category?.setHasFixedSize(true)

        setupDatabse()
        getDataCount()

        swipe_refresh?.setOnRefreshListener {
            setupDatabse()
            getDataCount()
        }

        fab_new_class.visibility = GONE
    }

    private fun setupDatabse() {
        val query = FirebaseFirestore.getInstance()
            .collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("subCategory")
            .document(getSubCategoryKey.toString())
            .collection("listSub")
            .orderBy("date")

        val options = FirestoreRecyclerOptions.Builder<SubjectResponse>()
            .setQuery(query, SubjectResponse::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = TeacherSubjectListAdapter(options)
        rv_category?.adapter = adapter
    }

    private fun getDataCount() {
        val db = FirebaseFirestore.getInstance()
            .collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("subCategory")
            .document(getSubCategoryKey.toString())
            .collection("listSub")

        db.addSnapshotListener { snapshot, _ ->
            if ((snapshot?.size() ?: 0) > 0) {
                rv_category?.visibility = View.VISIBLE
                tv_no_data?.visibility = GONE
            } else {
                rv_category?.visibility = GONE
                tv_no_data?.visibility = View.VISIBLE
            }
        }
        swipe_refresh?.isRefreshing = false
    }
}