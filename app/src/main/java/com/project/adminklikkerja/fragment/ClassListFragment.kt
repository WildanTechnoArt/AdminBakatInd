package com.project.adminklikkerja.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminklikkerja.R
import com.project.adminklikkerja.activity.DuplicateActivity
import com.project.adminklikkerja.activity.SubjectListActivity
import com.project.adminklikkerja.adapter.SubCategoryAdapter
import com.project.adminklikkerja.model.ClassResponse
import com.project.adminklikkerja.model.SubjectResponse
import com.project.adminklikkerja.utils.UtilsConstant
import com.project.adminklikkerja.utils.UtilsConstant.FAB_BROADCAST
import com.project.adminklikkerja.utils.UtilsConstant.SUBCLASS_DATA
import com.project.adminklikkerja.utils.UtilsConstant.SUB_CATEGORY_KEY
import com.project.adminklikkerja.utils.UtilsConstant.SUB_CATEGORY_NAME
import com.project.adminklikkerja.view.SubclassListener
import kotlinx.android.synthetic.main.edit_lesson_dialog.view.*
import kotlinx.android.synthetic.main.fragment_tab_list.*
import kotlin.collections.ArrayList

class ClassListFragment : Fragment(), SubclassListener {

    private var getLesson: String? = null
    private var getLessonKey: String? = null
    private var getCategory: String? = null
    private var getCategoryKey: String? = null
    private lateinit var mIntent: Intent
    private lateinit var dialogView: View
    private var alertDialog: AlertDialog? = null

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
            (view.context as AppCompatActivity).getString(R.string.tv_not_teacher_class)

        getLesson = mIntent.getStringExtra(UtilsConstant.LESSON_NAME).toString()
        getLessonKey = mIntent.getStringExtra(UtilsConstant.LESSON_KEY).toString()
        getCategory = mIntent.getStringExtra(UtilsConstant.CATEGORY_NAME).toString()
        getCategoryKey = mIntent.getStringExtra(UtilsConstant.CATEGORY_KEY).toString()

        swipe_refresh?.isEnabled = false
        rv_data_list?.layoutManager = LinearLayoutManager(context)
        rv_data_list?.setHasFixedSize(true)
        rv_data_list?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    sendFabBroadcast(true)
                }

                if (!recyclerView.canScrollVertically(-1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    sendFabBroadcast(false)
                }
            }
        })


        setupDatabse()
        getDataCount()
        swipe_refresh.setOnRefreshListener {
            setupDatabse()
            getDataCount()
        }
    }

    private fun sendFabBroadcast(condition: Boolean) {
        val sendBC = Intent()
        sendBC.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            .setAction(FAB_BROADCAST)
            .putExtra("endScroll", condition)
        activity?.sendBroadcast(sendBC)
    }

    private fun setupDatabse() {
        val query = FirebaseFirestore.getInstance()
            .collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("subCategory")
            .orderBy("date")

        val options = FirestoreRecyclerOptions.Builder<ClassResponse>()
            .setQuery(query, ClassResponse::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = SubCategoryAdapter(options, this)
        rv_data_list?.adapter = adapter
    }

    private fun getDataCount() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
            .collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("subCategory")
            .orderBy("date")

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

    override fun onDuplicate(subClassKey: String, subclassName: String) {
        try {
            swipe_refresh?.isRefreshing = true

            val rootRef = FirebaseFirestore.getInstance()
            val subjectsRef = rootRef.collection("lessons").document(getLessonKey.toString())
                .collection("category").document(getCategoryKey.toString())
                .collection("subCategory").document(subClassKey).collection("listSub")
            val subClassList = ArrayList<SubjectResponse>()

            subjectsRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val data = SubjectResponse()
                        val subjectName = document.getString("name")
                        val subjectDate = document.getDate("date")
                        val subjectPdf = document.getString("pdf")
                        val subjectLink = document.getString("link")
                        val subjectVideo = document.getString("video")
                        data.name = subjectName
                        data.date = subjectDate
                        data.pdf = subjectPdf
                        data.link = subjectLink
                        data.video = subjectVideo
                        subClassList.add(data)
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        swipe_refresh?.isRefreshing = false
                        val intent = Intent(context, DuplicateActivity::class.java)
                        intent.putExtra(SUB_CATEGORY_NAME, subclassName)
                        intent.putExtra(SUBCLASS_DATA, subClassList)
                        intent.putExtra(SUB_CATEGORY_KEY, subClassKey)
                        startActivity(intent)
                    }, 5000)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            swipe_refresh?.isRefreshing = false
        }
    }

    override fun onClick(key: String, name: String) {
        val intent = Intent(context, SubjectListActivity::class.java)
        intent.putExtra(UtilsConstant.LESSON_NAME, getLesson)
        intent.putExtra(UtilsConstant.LESSON_KEY, getLessonKey)
        intent.putExtra(UtilsConstant.CATEGORY_NAME, getCategory)
        intent.putExtra(UtilsConstant.CATEGORY_KEY, getCategoryKey)
        intent.putExtra(SUB_CATEGORY_NAME, name)
        intent.putExtra(SUB_CATEGORY_KEY, key)
        startActivity(intent)
    }

    override fun onDelete(key: String, teacherId: String) {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("subCategory")
            .document(key)
            .delete()
            .addOnSuccessListener {
                deleteSubclassInTeacher(key, teacherId)
            }.addOnFailureListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteSubclassInTeacher(key: String, teacherId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("teacher")
            .document(teacherId)
            .collection("classList")
            .document(getCategoryKey.toString())
            .collection("subCategory")
            .document(key)
            .delete()
            .addOnSuccessListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(context, "Sub Kelas Berhasil Dihapus", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("InflateParams")
    override fun onEdit(
        teacherId: String,
        categoryKey: String,
        categoryName: String,
        lessonImg: String,
        subscribe: Boolean
    ) {
        val builder =
            context?.let { MaterialAlertDialogBuilder(it, R.style.MaterialAlertDialog_rounded) }
        dialogView = (context as AppCompatActivity).layoutInflater.inflate(
            R.layout.edit_lesson_dialog,
            null
        )

        bindProgressButton(dialogView.btn_accept)
        dialogView.btn_accept.attachTextChangeAnimator()
        dialogView.btn_accept.text = getString(R.string.btn_edit)
        dialogView.input_lesson_name.setText(categoryName)

        dialogView.btn_accept.setOnClickListener {
            val subClassName = dialogView.input_lesson_name.text.toString()
            if (subClassName.isEmpty()) {
                Toast.makeText(context, "Tidak Boleh Kosong", Toast.LENGTH_SHORT).show()
            } else {
                dialogView.btn_accept.showProgress { progressColor = Color.WHITE }
                val db = FirebaseFirestore.getInstance()
                db.collection("lessons")
                    .document(getLessonKey.toString())
                    .collection("category")
                    .document(getCategoryKey.toString())
                    .collection("subCategory")
                    .document(categoryKey)
                    .update("name", subClassName)
                    .addOnSuccessListener {
                        editSubclassInTeacher(teacherId, subClassName, categoryKey)
                    }
                    .addOnFailureListener {
                        dialogView.btn_accept.hideProgress(R.string.btn_edit)
                        Toast.makeText(context, it.localizedMessage?.toString(), Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }

        dialogView.btn_cancel.setOnClickListener {
            alertDialog?.dismiss()
        }

        builder?.setView(dialogView)
        builder?.setTitle("Edit Nama Sub Kelas")

        alertDialog = builder?.create()
        alertDialog?.show()
    }

    private fun editSubclassInTeacher(teacherId: String, subClassName: String, key: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("teacher")
            .document(teacherId)
            .collection("classList")
            .document(getCategoryKey.toString())
            .collection("subCategory")
            .document(key)
            .update("name", subClassName)
            .addOnSuccessListener {
                Toast.makeText(context, "Nama Berhasil Diubah", Toast.LENGTH_SHORT)
                    .show()
                dialogView.btn_accept.hideProgress(R.string.btn_edit)
                alertDialog?.dismiss()
            }
            .addOnFailureListener { it1 ->
                dialogView.btn_accept.hideProgress(R.string.btn_edit)
                Toast.makeText(
                    context,
                    it1.localizedMessage?.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}