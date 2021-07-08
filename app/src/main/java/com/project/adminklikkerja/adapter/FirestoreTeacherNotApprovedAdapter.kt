package com.project.adminklikkerja.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminklikkerja.GlideApp
import com.project.adminklikkerja.R
import com.project.adminklikkerja.activity.TeacherProfileActivity
import com.project.adminklikkerja.model.TeacherModel
import com.project.adminklikkerja.utils.UtilsConstant.GET_PROFILE
import com.project.adminklikkerja.utils.UtilsConstant.IS_APPROVE
import com.project.adminklikkerja.view.TeacherApprovedView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.not_approved_item.view.*

class FirestoreTeacherNotApprovedAdapter(
    options: FirestoreRecyclerOptions<TeacherModel>,
    private val view: TeacherApprovedView.Presenter
) :
    FirestoreRecyclerAdapter<TeacherModel, FirestoreTeacherNotApprovedAdapter.ViewHolder>(options) {

    private var name: String? = null
    private var email: String? = null
    private var phone: String? = null
    private var experience: String? = null
    private var address: String? = null
    private var datetime: String? = null
    private var getUserId: String? = null
    private var context: Context? = null
    private val teacher = TeacherModel()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.not_approved_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: TeacherModel) {
        getUserId = snapshots.getSnapshot(position).id
        context = holder.containerView.context

        name = item.username.toString()
        email = item.email.toString()
        phone = item.phone.toString()
        experience = item.experience.toString()
        address = item.address.toString()
        datetime = item.datetime.toString()

        holder.apply {
            val db = FirebaseFirestore.getInstance()
            db.collection("photos")
                .document(getUserId.toString())
                .addSnapshotListener { snapshot, _ ->
                    val getPhoto = snapshot?.getString("photoUrl").toString()
                    GlideApp.with(containerView.context)
                        .load(getPhoto)
                        .placeholder(R.drawable.profile_placeholder)
                        .into(containerView.img_profile)
                }

            containerView.tv_teacher_name.text = name

            containerView.tv_email.text =
                String.format(context?.getString(R.string.show_email).toString(), email)

            containerView.btn_accept.setOnClickListener {
                teacher.address = address
                teacher.email = email
                teacher.username = name
                teacher.address = address
                teacher.experience = experience
                teacher.phone = phone
                teacher.approve = true
                teacher.teacher = true
                teacher.datetime = datetime
                teacher.active = true
                showDialogInput()
            }

            containerView.card_teacher.setOnClickListener {
                toProfileActivity(position)
            }

            containerView.btn_profile.setOnClickListener {
                toProfileActivity(position)
            }
        }
    }

    private fun toProfileActivity(position: Int) {
        getUserId = snapshots.getSnapshot(position).id
        val intent = Intent(context, TeacherProfileActivity::class.java)
        intent.putExtra(GET_PROFILE, getUserId)
        intent.putExtra(IS_APPROVE, false)
        (context as AppCompatActivity).startActivity(intent)
    }

    @SuppressLint("InflateParams")
    private fun showDialogInput() {
        val builder = context?.let {
            MaterialAlertDialogBuilder(it, R.style.MaterialAlertDialog_rounded)
                .setTitle("Konfirmasi")
                .setMessage("Anda yakin ingin menyetujuinya?")
                .setPositiveButton("Ya") { _, _ ->
                    view.postData(getUserId, teacher)
                }
                .setNegativeButton("Tidak"){ dialog, _ ->
                    dialog.dismiss()
                }
        }
        val dialog = builder?.create()
        dialog?.show()
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}