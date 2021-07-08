package com.project.adminklikkerja.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminklikkerja.GlideApp
import com.project.adminklikkerja.R
import com.project.adminklikkerja.model.ReviewModel
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.review_item.view.*

class ReviewListAdapter(
    options: FirestoreRecyclerOptions<ReviewModel>
) :
    FirestoreRecyclerAdapter<ReviewModel, ReviewListAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.review_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: ReviewModel) {
        val userid = item.userid.toString()
        val username = item.username.toString()
        val review = item.review.toString()
        val rating = item.rating

        holder.apply {
            val db = FirebaseFirestore.getInstance()
            db.collection("photos")
                .document(userid)
                .addSnapshotListener { snapshot, _ ->
                    val getPhoto = snapshot?.getString("photoUrl").toString()
                    GlideApp.with(containerView.context)
                        .load(getPhoto)
                        .placeholder(R.drawable.profile_placeholder)
                        .into(containerView.img_profile)
                }

            containerView.tv_name.text = username
            containerView.tv_review.text = review
            containerView.ratingBar.rating = rating?.toFloat()!!
        }
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}