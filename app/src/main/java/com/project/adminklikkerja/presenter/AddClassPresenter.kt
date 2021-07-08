package com.project.adminklikkerja.presenter

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.project.adminklikkerja.R
import com.project.adminklikkerja.model.ClassResponse
import com.project.adminklikkerja.view.AddClassView
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import java.io.ByteArrayOutputStream
import java.io.File

class AddClassPresenter(
    private val context: Context?,
    private val view: AddClassView.View
) : AddClassView.Presenter {

    private val imageReference = FirebaseStorage.getInstance().reference
    private val database = FirebaseFirestore.getInstance()

    override fun uploadLesson(
        lessonKey: String,
        className: String,
        result: CropImage.ActivityResult,
        isSubscribe: Boolean?,
        link: String
    ) {
        view.showProgressBar()

        val resultUri = result.uri

        val thumbImage = File(resultUri.path.toString())

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

        thumbPath.putFile(resultUri).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imageReference.child(thumbURL).downloadUrl.addOnSuccessListener { imageUri: Uri? ->

                    val uploadTask: UploadTask = thumbPath.putBytes(imageByte)

                    uploadTask.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val lessonImgUrl = imageUri.toString()

                            val data = ClassResponse()
                            data.name = className
                            data.image = lessonImgUrl
                            data.subscribe = isSubscribe
                            data.link = link

                            database.collection("lessons")
                                .document(lessonKey)
                                .collection("category")
                                .document()
                                .set(data)
                                .addOnSuccessListener {
                                    view.onSuccessUpload(
                                        context,
                                        context?.getString(R.string.upload_class_success)
                                    )
                                }.addOnFailureListener {
                                    view.hideProgressBar()
                                    view.handleResponse(it.localizedMessage?.toString())
                                }
                        } else {
                            view.hideProgressBar()
                            view.handleResponse(task.exception?.localizedMessage.toString())
                        }
                    }

                }.addOnFailureListener {
                    view.hideProgressBar()
                    view.handleResponse(it.localizedMessage?.toString().toString())
                }

            } else {
                view.hideProgressBar()
                view.handleResponse(task.exception?.localizedMessage.toString())
            }
        }
    }

    override fun editLesson(
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

                                    val data = ClassResponse()
                                    data.name = className
                                    data.image = lessonImgUrl
                                    data.subscribe = subscribe
                                    data.link = link

                                    database.collection("lessons")
                                        .document(lessonKey)
                                        .collection("category")
                                        .document(classKey)
                                        .set(data)
                                        .addOnSuccessListener {
                                            view.onSuccessUpload(
                                                context,
                                                context?.getString(R.string.edit_class_success)
                                            )
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
            data["name"] = className
            data["subscribe"] = subscribe
            data["link"] = link

            database.collection("lessons")
                .document(lessonKey)
                .collection("category")
                .document(classKey)
                .update(data)
                .addOnSuccessListener {
                    view.onSuccessUpload(
                        context,
                        context?.getString(R.string.edit_class_success)
                    )
                }.addOnFailureListener {
                    view.hideProgressBarEdit()
                    view.handleResponse(it.localizedMessage?.toString().toString())
                }

        }
    }
}