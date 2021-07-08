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
import com.project.adminklikkerja.adapter.FirestoreStudentAdapter
import com.project.adminklikkerja.model.StudentModel
import com.project.adminklikkerja.utils.UtilsConstant
import kotlinx.android.synthetic.main.fragment_tab_list.*

class StudentListFragment : Fragment() {

    private var getLessonKey: String? = null
    private var getCategoryKey: String? = null
    private var teacherId: String? = null
    private lateinit var mIntent: Intent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tab_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepare(view)
        checkClass()
        requestData()
    }

    private fun prepare(view: View) {
        mIntent = (view.context as AppCompatActivity).intent

        tv_not_data.text =
            (view.context as AppCompatActivity).getString(R.string.tv_no_student)

        getLessonKey = mIntent.getStringExtra(UtilsConstant.LESSON_KEY).toString()
        getCategoryKey = mIntent.getStringExtra(UtilsConstant.CATEGORY_KEY).toString()
        teacherId = mIntent.getStringExtra(UtilsConstant.GET_PROFILE)

        swipe_refresh?.isEnabled = false

        swipe_refresh?.setOnRefreshListener {
            checkClass()
            requestData()
        }
    }

    private fun requestData() {
        val query = FirebaseFirestore.getInstance()
            .collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("studentList")
            .orderBy("username")

        val options = FirestoreRecyclerOptions.Builder<StudentModel>()
            .setQuery(query, StudentModel::class.java)
            .setLifecycleOwner(this)
            .build()

        rv_data_list?.layoutManager = LinearLayoutManager(context)
        rv_data_list?.setHasFixedSize(true)

        val adapter = teacherId?.let { FirestoreStudentAdapter(options, it) }
        rv_data_list?.adapter = adapter
    }

    private fun checkClass() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("studentList")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot?.isEmpty == true) {
                    tv_not_data?.visibility = View.VISIBLE
                    rv_data_list?.visibility = View.GONE
                } else {
                    tv_not_data?.visibility = View.GONE
                    rv_data_list?.visibility = View.VISIBLE
                }

                swipe_refresh?.isRefreshing = false
            }
    }
}