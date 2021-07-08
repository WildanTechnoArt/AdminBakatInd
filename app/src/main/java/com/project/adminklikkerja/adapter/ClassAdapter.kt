package com.project.adminklikkerja.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.adminklikkerja.GlideApp
import com.project.adminklikkerja.R
import com.project.adminklikkerja.model.ClassResponse
import com.project.adminklikkerja.view.CategoryListener
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.lesson_item.view.*

class ClassAdapter(
    options: FirestoreRecyclerOptions<ClassResponse>,
    private var listener: CategoryListener
) :
    FirestoreRecyclerAdapter<ClassResponse, ClassAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lesson_item, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: ClassResponse) {
        val getKey = snapshots.getSnapshot(position).id
        val getContext = holder.itemView.context
        val getCategoryName = item.name.toString()
        val getLessonImg = item.image.toString()
        val getLink = item.link.toString()
        val getIsSubscribe = item.subscribe
        val getTeacherId = item.teacherid

        holder.apply {
            GlideApp.with(getContext)
                .load(getLessonImg)
                .into(containerView.img_lesson)

            if (getIsSubscribe == true) {
                containerView.tv_subscribe.setTextColor(Color.parseColor("#DB2F31"))
                containerView.tv_subscribe.text = "(${getContext.getString(R.string.rb_subscribe)})"
            } else {
                containerView.tv_subscribe.setTextColor(Color.parseColor("#388E3C"))
                containerView.tv_subscribe.text = "(${getContext.getString(R.string.rb_trial)})"
            }

            containerView.tv_lesson.text = getCategoryName
            containerView.tv_lesson.isSelected = true
            containerView.materialCardView.setOnClickListener {
                listener.onClick(getKey, getCategoryName, getTeacherId.toString())
            }

            if (getTeacherId != null) {
                containerView.btn_menu.visibility = View.GONE
            }

            containerView.btn_menu.setOnClickListener {
                val popupMenu = PopupMenu(it.context, it)
                popupMenu.setOnMenuItemClickListener(object :
                    android.widget.PopupMenu.OnMenuItemClickListener,
                    PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(p0: MenuItem?): Boolean {
                        when (p0?.itemId) {
                            R.id.menu_edit -> {
                                getIsSubscribe?.let { it1 ->
                                    listener.onEdit(
                                        getKey, getCategoryName, getLessonImg,
                                        it1, getLink
                                    )
                                }
                            }
                            R.id.menu_delete -> {
                                val builder = MaterialAlertDialogBuilder(
                                    it.context,
                                    R.style.MaterialAlertDialog_rounded
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