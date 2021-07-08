package com.project.adminklikkerja.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.project.adminklikkerja.R
import com.project.adminklikkerja.model.ClassResponse
import com.project.adminklikkerja.view.SubclassListener
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.category_item.view.*

class SubCategoryAdapter(
    options: FirestoreRecyclerOptions<ClassResponse>,
    private var listener: SubclassListener
) :
    FirestoreRecyclerAdapter<ClassResponse, SubCategoryAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_item, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: ClassResponse) {
        val getKey = snapshots.getSnapshot(position).id
        val getSubclassName = item.name.toString()
        val getTeacherId = item.teacherid.toString()

        holder.apply {
            containerView.tv_category.text = "${position.plus(1)}. $getSubclassName"
            containerView.card_category.setOnClickListener {
                listener.onClick(getKey, getSubclassName)
            }
            containerView.btn_menu.setOnClickListener {
                val popupMenu = PopupMenu(it.context, it)
                popupMenu.setOnMenuItemClickListener(object :
                    android.widget.PopupMenu.OnMenuItemClickListener,
                    PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(p0: MenuItem?): Boolean {
                        when (p0?.itemId) {
                            R.id.menu_duplicate -> {
                                listener.onDuplicate(getKey, getSubclassName)
                            }
                            R.id.menu_edit -> {
                                listener.onEdit(getTeacherId, getKey, getSubclassName, "", false)
                            }
                            R.id.menu_delete -> {
                                listener.onDelete(getKey, getTeacherId)
                            }
                        }
                        return true
                    }
                })
                popupMenu.inflate(R.menu.subclass_adapter)
                popupMenu.show()
            }
        }
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}