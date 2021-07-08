package com.project.adminklikkerja.fragment.teacher

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminklikkerja.R
import com.project.adminklikkerja.adapter.teacher.SubclassListAdapter
import com.project.adminklikkerja.model.teacher.SubClassResponse
import com.project.adminklikkerja.utils.UtilsConstant
import com.project.adminklikkerja.view.TeacherSubclassListener
import kotlinx.android.synthetic.main.fragment_tab_list.*

class SubclassListFragment : Fragment(), TeacherSubclassListener {

    private var getLesson: String? = null
    private var getLessonKey: String? = null
    private var getCategory: String? = null
    private var getCategoryKey: String? = null
    private var getSubCategory: String? = null
    private var getSubCategoryKey: String? = null
    private var classImage: String? = null
    private var teacherid: String? = null
    private lateinit var mIntent: Intent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tab_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepare(view)
    }

    private fun prepare(view: View) {
        mIntent = (view.context as AppCompatActivity).intent

        tv_not_data.text =
            (view.context as AppCompatActivity).getString(R.string.tv_not_teacher_subclass)

        getLesson = mIntent.getStringExtra(UtilsConstant.LESSON_NAME).toString()
        getLessonKey = mIntent.getStringExtra(UtilsConstant.LESSON_KEY).toString()
        getCategory = mIntent.getStringExtra(UtilsConstant.CATEGORY_NAME).toString()
        getCategoryKey = mIntent.getStringExtra(UtilsConstant.CATEGORY_KEY).toString()
        getSubCategory = mIntent.getStringExtra(UtilsConstant.SUB_CATEGORY_NAME).toString()
        getSubCategoryKey = mIntent.getStringExtra(UtilsConstant.SUB_CATEGORY_KEY).toString()
        classImage = mIntent.getStringExtra(UtilsConstant.CLASS_IMAGE).toString()
        teacherid = mIntent.getStringExtra(UtilsConstant.GET_PROFILE).toString()

        rv_data_list?.layoutManager = LinearLayoutManager(context)
        rv_data_list?.setHasFixedSize(true)
        swipe_refresh?.isEnabled = false

        setupDatabse()
        getDataCount()

        swipe_refresh?.setOnRefreshListener {
            setupDatabse()
            getDataCount()
        }
    }

    private fun setupDatabse() {
        val query = FirebaseFirestore.getInstance()
            .collection("teacher")
            .document(teacherid.toString())
            .collection("classList")
            .document(getCategoryKey.toString())
            .collection("subCategory")
            .orderBy("date")

        val options = FirestoreRecyclerOptions.Builder<SubClassResponse>()
            .setQuery(query, SubClassResponse::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = SubclassListAdapter(options, this, teacherid, classImage)
        rv_data_list?.adapter = adapter
    }

    private fun getDataCount() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
            .collection("teacher")
            .document(teacherid.toString())
            .collection("classList")
            .document(getCategoryKey.toString())
            .collection("subCategory")

        db.addSnapshotListener { snapshot, _ ->
            if ((snapshot?.size() ?: 0) > 0) {
                rv_data_list?.visibility = View.VISIBLE
                tv_not_data?.visibility = View.GONE
            } else {
                rv_data_list?.visibility = View.GONE
                tv_not_data?.visibility = View.VISIBLE
            }

            swipe_refresh?.isRefreshing = false
        }
    }

    override fun showProgress() {
        swipe_refresh?.isRefreshing = true
    }

    override fun hideProgress() {
        swipe_refresh?.isRefreshing = false
    }
}