package com.project.adminklikkerja.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.adminklikkerja.GlideApp
import com.project.adminklikkerja.R
import com.project.adminklikkerja.activity.ClassActivity
import com.project.adminklikkerja.model.LessonResponse
import com.project.adminklikkerja.utils.UtilsConstant.LESSON_KEY
import com.project.adminklikkerja.utils.UtilsConstant.LESSON_NAME
import com.project.adminklikkerja.view.EditLessonListener
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.lesson_item.view.*

class LessonAdapter(
    options: FirestoreRecyclerOptions<LessonResponse>,
    private val listener: EditLessonListener
) :
    FirestoreRecyclerAdapter<LessonResponse, LessonAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lesson_item, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: LessonResponse) {
        val getKey = snapshots.getSnapshot(position).id
        val getContext = holder.itemView.context
        val getLessonName = item.lesson.toString()
        val getLessonImg = item.image.toString()

        holder.apply {
            GlideApp.with(getContext)
                .load(getLessonImg)
                .into(containerView.img_lesson)

            containerView.tv_subscribe.visibility = View.GONE

            containerView.tv_lesson.text = getLessonName
            containerView.tv_lesson.isSelected = true

            containerView.materialCardView.setOnClickListener {
                val intent = Intent(getContext, ClassActivity::class.java)
                intent.putExtra(LESSON_NAME, getLessonName)
                intent.putExtra(LESSON_KEY, getKey)
                (getContext as AppCompatActivity).startActivity(intent)
            }

            containerView.btn_menu.setOnClickListener {
                val popupMenu = PopupMenu(it.context, it)
                popupMenu.setOnMenuItemClickListener(object :
                    android.widget.PopupMenu.OnMenuItemClickListener,
                    PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(p0: MenuItem?): Boolean {
                        when (p0?.itemId) {
                            R.id.menu_edit -> {
                                listener.onEdit(getKey, getLessonName, getLessonImg)
                            }
                            R.id.menu_delete -> {
                                val builder = MaterialAlertDialogBuilder(
                                    it.context, R.style.MaterialAlertDialog_rounded
                                )
                                    .setTitle("Konfirmasi")
                                    .setMessage("Anda yakin ingin menghapusnya?")
                                    .setPositiveButton("Ya") { _, _ ->
                                        listener.onDelete(getKey)
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
                popupMenu.inflate(R.menu.item_list)
                popupMenu.show()
            }
        }
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}