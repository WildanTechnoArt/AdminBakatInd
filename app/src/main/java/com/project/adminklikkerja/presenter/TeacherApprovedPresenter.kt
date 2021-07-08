package com.project.adminklikkerja.presenter

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminklikkerja.R
import com.project.adminklikkerja.model.StudentModel
import com.project.adminklikkerja.model.TeacherModel
import com.project.adminklikkerja.view.ApprovedView
import com.project.adminklikkerja.view.TeacherApprovedView
import java.lang.Exception

class TeacherApprovedPresenter(
    private val context: Context,
    private val view: TeacherApprovedView.View
) : TeacherApprovedView.Presenter {

    override fun postData(userId: String?, data: TeacherModel) {
        try {
            view.showProgressBar()

            val db = FirebaseFirestore.getInstance()
            db.collection("klikkerja")
                .document("teacher")
                .collection("teacherList")
                .document(userId.toString())
                .set(data)
                .addOnSuccessListener {
                    val db2 = FirebaseFirestore.getInstance()
                    db2.collection("users")
                        .document(userId.toString())
                        .set(data)
                        .addOnSuccessListener {
                            deleteTeacher(userId.toString())
                        }.addOnFailureListener {
                            view.hideProgressBar()
                            view.handleError(it.message.toString())
                        }
                }.addOnFailureListener {
                    view.hideProgressBar()
                    view.handleError(it.message.toString())
                }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun deleteTeacher(userId: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("klikkerja")
                .document("teacher")
                .collection("newRegistrants")
                .document(userId)
                .delete()
                .addOnSuccessListener {
                    view.onSuccess(context.getString(R.string.success_add_teacher))
                }.addOnFailureListener {
                    view.hideProgressBar()
                    view.handleError(it.message.toString())
                }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}