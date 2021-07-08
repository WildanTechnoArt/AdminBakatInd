package com.project.adminklikkerja.presenter

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.project.adminklikkerja.R
import com.project.adminklikkerja.view.TeacherClassView
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import java.io.ByteArrayOutputStream
import java.io.File

class TeacherClassPresenter(
    private val context: Context?,
    private val view: TeacherClassView.View
) : TeacherClassView.Presenter {

    private val imageReference = FirebaseStorage.getInstance().reference
    private val database = FirebaseFirestore.getInstance()

    override fun editLesson(
        teacherId: String,
        isEditWithImage: Boolean,
        lessonKey: String,
        classKey: String,
        className: String,
        result: CropImage.ActivityResult?,
        subscribe: Boolean,
        link: String
    ) {
        view.showProgressBarEdit()

        if (isEditWithImage) {
            val resultUri = result?.uri

            val thumbImage = File(resultUri?.path.toString())

            val thumbBitmap = Compressor(context)
                .setMaxHeight(200)
                .setMaxWidth(200)
                .setQuality(100)
                .compressToBitmap(thumbImage)

            val baos = ByteArrayOutputStream()
            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageByte = baos.toByteArray()

            val thumbURL = "class_image/$className.jpg"
            val thumbPath = imageReference.child(thumbURL)

            resultUri?.let {
                thumbPath.putFile(it).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        imageReference.child(thumbURL).downloadUrl.addOnSuccessListener { imageUri: Uri? ->

                            val uploadTask: UploadTask = thumbPath.putBytes(imageByte)

                            uploadTask.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val lessonImgUrl = imageUri.toString()

                                    val data = HashMap<String, Any>()
                                    data["classname"] = className
                                    data["image"] = lessonImgUrl
                                    data["subscribe"] = subscribe
                                    data["link"] = link

                                    database.collection("teacher")
                                        .document(teacherId)
                                        .collection("classList")
                                        .document(classKey)
                                        .update(data)
                                        .addOnSuccessListener {
                                            database.collection("lessons")
                                                .document(lessonKey)
                                                .collection("category")
                                                .document(classKey)
                                                .get()
                                                .addOnSuccessListener { it1 ->
                                                    if (it1.exists()) {
                                                        val data2 = HashMap<String, Any>()
                                                        data2["name"] = className
                                                        data2["image"] = lessonImgUrl
                                                        data2["subscribe"] = subscribe
                                                        data2["link"] = link

                                                        database.collection("lessons")
                                                            .document(lessonKey)
                                                            .collection("category")
                                                            .document(classKey)
                                                            .update(data2)
                                                            .addOnSuccessListener {
                                                                view.onSuccessUpload(
                                                                    context,
                                                                    context?.getString(R.string.edit_class_success)
                                                                )
                                                            }.addOnFailureListener { it2 ->
                                                                view.hideProgressBarEdit()
                                                                view.handleResponse(
                                                                    it2.localizedMessage?.toString()
                                                                        .toString()
                                                                )
                                                            }
                                                    } else {
                                                        view.onSuccessUpload(
                                                            context,
                                                            context?.getString(R.string.edit_class_success)
                                                        )
                                                    }
                                                }.addOnFailureListener { it1 ->
                                                    view.hideProgressBarEdit()
                                                    view.handleResponse(it1.localizedMessage?.toString())
                                                }
                                        }.addOnFailureListener { it1 ->
                                            view.hideProgressBarEdit()
                                            view.handleResponse(it1.localizedMessage?.toString())
                                        }
                                } else {
                                    view.hideProgressBarEdit()
                                    view.handleResponse(task.exception?.localizedMessage.toString())
                                }
                            }

                        }.addOnFailureListener { it1 ->
                            view.hideProgressBarEdit()
                            view.handleResponse(it1.localizedMessage?.toString().toString())
                        }

                    } else {
                        view.hideProgressBarEdit()
                        view.handleResponse(task.exception?.localizedMessage.toString())
                    }
                }
            }
        } else {
            val data = HashMap<String, Any>()
            data["classname"] = className
            data["subscribe"] = subscribe
            data["link"] = link

            database.collection("teacher")
                .document(teacherId)
                .collection("classList")
                .document(classKey)
                .update(data)
                .addOnSuccessListener {
                    database.collection("lessons")
                        .document(lessonKey)
                        .collection("category")
                        .document(classKey)
                        .get()
                        .addOnSuccessListener { it1 ->
                            if (it1.exists()) {
                                val data2 = HashMap<String, Any>()
                                data2["name"] = className
                                data2["subscribe"] = subscribe
                                data2["link"] = link

                                database.collection("lessons")
                                    .document(lessonKey)
                                    .collection("category")
                                    .document(classKey)
                                    .update(data2)
                                    .addOnSuccessListener {
                                        view.onSuccessUpload(
                                            context,
                                            context?.getString(R.string.edit_class_success)
                                        )
                                    }.addOnFailureListener {
                                        view.hideProgressBarEdit()
                                        view.handleResponse(
                                            it.localizedMessage?.toString().toString()
                                        )
                                    }
                            } else {
                                view.onSuccessUpload(
                                    context,
                                    context?.getString(R.string.edit_class_success)
                                )
                            }
                        }.addOnFailureListener {
                            view.hideProgressBarEdit()
                            view.handleResponse(it.localizedMessage?.toString().toString())
                        }

                }.addOnFailureListener {
                    view.hideProgressBarEdit()
                    view.handleResponse(it.localizedMessage?.toString().toString())
                }
        }
    }
}