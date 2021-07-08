package com.project.adminklikkerja.presenter

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.project.adminklikkerja.R
import com.project.adminklikkerja.view.PostView

class UploadPdfPresenter(
    private val context: Context,
    private val view: PostView.View
) : PostView.Presenter {

    private lateinit var db: FirebaseFirestore
    private lateinit var fileUrl: String
    private var resultUri: Uri? = null
    private val fileReference = FirebaseStorage.getInstance().reference

    override fun uploadFileDocument(result: Uri) {
        resultUri = result
    }

    override fun uploadToDatabase(
        lessonKey: String,
        categoryKey: String,
        subCategoryKey: String,
        listSubKey: String?
    ) {
        val fileURL = "document_pdf/$listSubKey" + "_" + "${resultUri?.lastPathSegment}"
        val filePath = fileReference.child(fileURL)

        filePath.putFile(resultUri!!)
            .addOnProgressListener {
                val progress: Double =
                    100.0 * (it.bytesTransferred / it.totalByteCount)
                val progressBar = progress.toInt()
                view.showProgressBar(progressBar)
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    fileReference.child(fileURL).downloadUrl
                        .addOnSuccessListener { imageUri: Uri? ->
                            fileUrl = imageUri.toString()

                            db = FirebaseFirestore.getInstance()
                            db.collection("lessons")
                                .document(lessonKey)
                                .collection("category")
                                .document(categoryKey)
                                .collection("subCategory")
                                .document(subCategoryKey)
                                .collection("listSub")
                                .document(listSubKey.toString())
                                .update("pdf", fileUrl)
                                .addOnSuccessListener {
                                    view.hideProgressBar()
                                    view.onSuccessPost(context.getString(R.string.success_upload_post))
                                }
                                .addOnFailureListener {
                                    view.handleResponse(it.localizedMessage?.toString().toString())
                                    view.hideProgressBar()
                                }

                        }.addOnFailureListener {
                            view.hideProgressBar()
                            view.handleResponse(it.localizedMessage?.toString().toString())
                        }

                } else {
                    view.hideProgressBar()
                    view.handleResponse(context.getString(R.string.upload_failed))
                }
            }
    }
}