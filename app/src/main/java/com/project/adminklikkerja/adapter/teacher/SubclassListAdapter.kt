package com.project.adminklikkerja.adapter.teacher

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminklikkerja.R
import com.project.adminklikkerja.activity.techer.TeacherSubjectListActivity
import com.project.adminklikkerja.model.ClassResponse
import com.project.adminklikkerja.model.teacher.SubClassResponse
import com.project.adminklikkerja.utils.UtilsConstant
import com.project.adminklikkerja.view.TeacherSubclassListener
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.subclass_item.view.*
import java.util.*
import kotlin.collections.HashMap

class SubclassListAdapter(
    options: FirestoreRecyclerOptions<SubClassResponse>,
    private val listener: TeacherSubclassListener,
    private val teacherId: String?,
    private val classImage: String?
) :
    FirestoreRecyclerAdapter<SubClassResponse, SubclassListAdapter.ViewHolder>(options) {

    private lateinit var getContext: Context
    private var classId: String? = null
    private var lessonId: String? = null
    private var className: String? = null
    private var lessonName: String? = null
    private var subclassName: String? = null
    private var subclassKey: String? = null
    private var getStatus: Boolean? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.subclass_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: SubClassResponse) {
        getContext = holder.itemView.context
        classId = item.classid.toString()
        lessonId = item.lessonid.toString()
        className = item.classname.toString()
        lessonName = item.lessonname.toString()
        getStatus = item.approve
        subclassName = item.name.toString()
        subclassKey = snapshots.getSnapshot(position).id

        holder.apply {
            containerView.tv_subclass_name.text = "${position.plus(1)}. $subclassName"

            if (getStatus == true) {
                containerView.btn_menu.visibility = GONE
                containerView.tv_approve?.text = "Aktif"
                containerView.tv_approve.setTextColor(
                    ContextCompat.getColor(
                        getContext,
                        R.color.colorActive
                    )
                )

                containerView.setOnClickListener {
                    val intent = Intent(getContext, TeacherSubjectListActivity::class.java)
                    intent.putExtra(UtilsConstant.LESSON_NAME, lessonName)
                    intent.putExtra(UtilsConstant.LESSON_KEY, lessonId)
                    intent.putExtra(UtilsConstant.CATEGORY_NAME, className)
                    intent.putExtra(UtilsConstant.CATEGORY_KEY, classId)
                    intent.putExtra(UtilsConstant.SUB_CATEGORY_NAME, subclassName)
                    intent.putExtra(UtilsConstant.SUB_CATEGORY_KEY, subclassKey)
                    intent.putExtra(UtilsConstant.GET_PROFILE, teacherId)
                    getContext.startActivity(intent)
                }
            } else {
                containerView.tv_approve?.text = "Tidak Aktif"
                containerView.tv_approve.setTextColor(
                    ContextCompat.getColor(
                        getContext,
                        R.color.colorAccent
                    )
                )

                containerView.btn_menu.setOnClickListener {
                    showDropdownMenu(it)
                }
                containerView.setOnClickListener {
                    containerView.btn_menu.callOnClick()
                }
            }
        }
    }

    private fun showDropdownMenu(view: View) {
        val popupMenu = PopupMenu(getContext, view)
        popupMenu.setOnMenuItemClickListener(object :
            android.widget.PopupMenu.OnMenuItemClickListener,
            PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(p0: MenuItem?): Boolean {
                when (p0?.itemId) {
                    R.id.menu_approve -> {
                        val builder = MaterialAlertDialogBuilder(getContext, R.style.MaterialAlertDialog_rounded)
                            .setTitle("Menyetujui")
                            .setMessage("Anda yakin ingin menyetujuinya?")
                            .setPositiveButton("Iya") { _, _ ->
                                saveToClass()
                            }
                            .setNegativeButton("Lain Kali") { dialog, _ ->
                                dialog.dismiss()
                            }
                        val dialog = builder.create()
                        dialog.show()
                    }
                }
                return true
            }
        })
        popupMenu.inflate(R.menu.teacher_subject_adapter)
        popupMenu.show()
    }

    private fun saveToClass() {
        listener.showProgress()

        val data = ClassResponse()
        data.name = className
        data.image = classImage
        data.subscribe = true
        data.teacherid = teacherId

        val db = FirebaseFirestore.getInstance()
        db.collection("lessons")
            .document(lessonId.toString())
            .collection("category")
            .document(classId.toString())
            .set(data)
            .addOnSuccessListener {
                saveToClassTeacher()
            }
            .addOnFailureListener {
                listener.hideProgress()
                Toast.makeText(getContext, it.localizedMessage?.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun saveToClassTeacher() {
        listener.showProgress()

        val data = HashMap<String, Any>()
        data["classid"] = classId.toString()

        val db = FirebaseFirestore.getInstance()
        db.collection("teacher")
            .document(teacherId.toString())
            .collection("classList")
            .document(classId.toString())
            .update(data)
            .addOnSuccessListener {
                saveToSubclass()
            }
            .addOnFailureListener {
                listener.hideProgress()
                Toast.makeText(getContext, it.localizedMessage?.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun saveToSubclass() {
        listener.showProgress()

        val data = SubClassResponse()
        data.classid = classId
        data.lessonid = lessonId
        data.subclassId = subclassKey
        data.name = subclassName
        data.teacherid = teacherId
        data.date = Calendar.getInstance().time

        val db = FirebaseFirestore.getInstance()
        db.collection("lessons")
            .document(lessonId.toString())
            .collection("category")
            .document(classId.toString())
            .collection("subCategory")
            .document(subclassKey.toString())
            .set(data)
            .addOnSuccessListener {
                changerStatus()
            }
            .addOnFailureListener {
                listener.hideProgress()
                Toast.makeText(getContext, it.localizedMessage?.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun changerStatus() {
        val db = FirebaseFirestore.getInstance()
        db.collection("teacher")
            .document(teacherId.toString())
            .collection("classList")
            .document(classId.toString())
            .collection("subCategory")
            .document(subclassKey.toString())
            .update("approve", true)
            .addOnSuccessListener {
                listener.hideProgress()
                Toast.makeText(
                    getContext,
                    "Kelas berhasil ditambahkan",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            .addOnFailureListener {
                listener.hideProgress()
                Toast.makeText(getContext, it.localizedMessage?.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}