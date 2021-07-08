package com.project.adminklikkerja.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminklikkerja.GlideApp
import com.project.adminklikkerja.R
import com.project.adminklikkerja.activity.TeacherProfileActivity
import com.project.adminklikkerja.model.TeacherModel
import com.project.adminklikkerja.utils.UtilsConstant
import com.project.adminklikkerja.utils.UtilsConstant.GET_PROFILE
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.approved_item.view.*

class FirestoreTeacherApprovedAdapter(options: FirestoreRecyclerOptions<TeacherModel>) :
    FirestoreRecyclerAdapter<TeacherModel, FirestoreTeacherApprovedAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.approved_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: TeacherModel) {
        val getUserId = snapshots.getSnapshot(position).id
        val context = holder.containerView.context
        val email = item.email.toString()

        holder.apply {
            val db = FirebaseFirestore.getInstance()
            db.collection("photos")
                .document(getUserId)
                .addSnapshotListener { snapshot, _ ->
                    val getPhoto = snapshot?.getString("photoUrl").toString()
                    GlideApp.with(containerView.context)
                        .load(getPhoto)
                        .placeholder(R.drawable.profile_placeholder)
                        .into(containerView.img_profile)
                }

            containerView.tv_teacher_name.text = item.username.toString()

            containerView.tv_email.text =
                String.format(context?.getString(R.string.show_email).toString(), email)

            containerView.card_teacher.setOnClickListener {
                toProfileActivity(position, context)
            }
        }
    }

    private fun toProfileActivity(position: Int, context: Context) {
        val getUserId = snapshots.getSnapshot(position).id
        val intent = Intent(context, TeacherProfileActivity::class.java)
        intent.putExtra(GET_PROFILE, getUserId)
        intent.putExtra(UtilsConstant.IS_APPROVE, true)
        (context as AppCompatActivity).startActivity(intent)
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}