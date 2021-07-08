package com.project.adminklikkerja.adapter.teacher

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminklikkerja.GlideApp
import com.project.adminklikkerja.R
import com.project.adminklikkerja.activity.techer.TeacherSubclassListActivity
import com.project.adminklikkerja.model.teacher.ClassResponse
import com.project.adminklikkerja.utils.UtilsConstant
import com.project.adminklikkerja.view.ClassListListener
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.profile_classlist_item.view.*
import kotlinx.android.synthetic.main.subs_class_item.view.img_lesson
import kotlinx.android.synthetic.main.subs_class_item.view.tv_class_name
import kotlinx.android.synthetic.main.subs_class_item.view.tv_lesson_name

class ProfileClassAdapter(
    options: FirestoreRecyclerOptions<ClassResponse>,
    private val listener: ClassListListener
) :
    FirestoreRecyclerAdapter<ClassResponse, ProfileClassAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_classlist_item, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: ClassResponse) {
        val classId = snapshots.getSnapshot(position).id
        val lessonId = item.lessonid.toString()
        val subclass = item.subclassname.toString()
        val subclassKey = item.subclassId.toString()
        val getImage = item.image.toString()
        val mClassName = item.classname.toString()
        val mLink = item.link.toString()
        val mContext = holder.itemView.context
        val subscribe = item.subscribe
        val teacherId = item.teacherid
        var mLessonName: String? = null
        var totalStudent: Int?

        holder.apply {
            GlideApp.with(containerView.context.applicationContext)
                .load(getImage)
                .placeholder(R.drawable.ic_image_200)
                .into(containerView.img_lesson)

            containerView.tv_class_name.text = mClassName

            val db = FirebaseFirestore.getInstance()
            db.collection("lessons")
                .document(lessonId)
                .collection("category")
                .document(classId)
                .collection("studentList")
                .addSnapshotListener { value, _ ->
                    totalStudent = value?.size()

                    containerView.tv_total_students.text =
                        "Jumlah Pelajar: ${totalStudent ?: 0} Orang"
                }

            val db2 = FirebaseFirestore.getInstance()
            db2.collection("lessons")
                .document(lessonId)
                .addSnapshotListener { snapshot, _ ->
                    mLessonName = snapshot?.getString("lesson").toString()

                    containerView.tv_lesson_name.text = mLessonName
                }

            containerView.btn_menu.setOnClickListener {
                val popupMenu = PopupMenu(mContext, it)
                popupMenu.setOnMenuItemClickListener(object :
                    android.widget.PopupMenu.OnMenuItemClickListener,
                    PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(p0: MenuItem?): Boolean {
                        when (p0?.itemId) {
                            R.id.menu_edit -> {
                                listener.onEditClass(teacherId.toString(), mClassName, lessonId, classId, getImage, subscribe, mLink)
                            }
                            R.id.menu_delete -> {
                                val builder = MaterialAlertDialogBuilder(
                                    mContext,
                                    R.style.MaterialAlertDialog_rounded
                                )
                                    .setTitle("Konfirmasi")
                                    .setMessage("Anda yakin ingin menghapusnya?")
                                    .setPositiveButton("Ya") { _, _ ->
                                        listener.onDeleteClass(lessonId, classId)
                                    }
                                    .setNegativeButton("Tidak") { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                val dialog = builder.create()
                                dialog.show()
                            }
                        }
                        return true
                    }
                })

                popupMenu.inflate(R.menu.class_item_menu)
                popupMenu.show()
            }

            containerView.setOnClickListener {
                val intent = Intent(mContext, TeacherSubclassListActivity::class.java)
                intent.putExtra(UtilsConstant.LESSON_NAME, mLessonName)
                intent.putExtra(UtilsConstant.LESSON_KEY, lessonId)
                intent.putExtra(UtilsConstant.CATEGORY_NAME, mClassName)
                intent.putExtra(UtilsConstant.CATEGORY_KEY, classId)
                intent.putExtra(UtilsConstant.SUB_CATEGORY_NAME, subclass)
                intent.putExtra(UtilsConstant.SUB_CATEGORY_KEY, subclassKey)
                intent.putExtra(UtilsConstant.GET_PROFILE, teacherId)
                intent.putExtra(UtilsConstant.CLASS_IMAGE, getImage)
                mContext.startActivity(intent)
            }
        }
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}