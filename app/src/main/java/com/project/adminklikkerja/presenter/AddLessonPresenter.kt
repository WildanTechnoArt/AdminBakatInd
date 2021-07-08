package com.project.adminklikkerja.presenter

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.project.adminklikkerja.R
import com.project.adminklikkerja.model.LessonResponse
import com.project.adminklikkerja.view.AddLessonView
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

class AddLessonPresenter(
    private val context: Context?,
    private val view: AddLessonView.View
) : AddLessonView.Presenter {

    private val imageReference = FirebaseStorage.getInstance().reference
    private val database = FirebaseFirestore.getInstance()

    override fun uploadLesson(lessonName: String, result: CropImage.ActivityResult) {
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

        val thumbURL = "lesson_image/${UUID.randomUUID()}.jpg"
        val thumbPath = imageReference.child(thumbURL)

        thumbPath.putFile(resultUri).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imageReference.child(thumbURL).downloadUrl.addOnSuccessListener { imageUri: Uri? ->

                    val uploadTask: UploadTask = thumbPath.putBytes(imageByte)

                    uploadTask.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val lessonImgUrl = imageUri.toString()

                            val data = LessonResponse()
                            data.lesson = lessonName
                            data.image = lessonImgUrl

                            database.collection("lessons")
                                .document()
                                .set(data)
                                .addOnCompleteListener {
                                    if (task.isSuccessful) {
                                        view.onSuccessUpload(
                                            context,
                                            context?.getString(R.string.upload_lesson_success)
                                        )
                                    }
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
        lessonKey: String,
        lessonName: String,
        result: CropImage.ActivityResult?,
        isEditWithImage: Boolean
    ) {
        view.showProgressBar()

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

            val thumbURL = "lesson_image/${UUID.randomUUID()}.jpg"
            val thumbPath = imageReference.child(thumbURL)

            resultUri?.let { it1 ->
                thumbPath.putFile(it1).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        imageReference.child(thumbURL).downloadUrl.addOnSuccessListener { imageUri: Uri? ->

                            val uploadTask: UploadTask = thumbPath.putBytes(imageByte)

                            uploadTask.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val lessonImgUrl = imageUri.toString()

                                    val data = LessonResponse()
                                    data.lesson = lessonName
                                    data.image = lessonImgUrl

                                    database.collection("lessons")
                                        .document(lessonKey)
                                        .set(data)
                                        .addOnCompleteListener {
                                            if (task.isSuccessful) {
                                                view.onSuccessUpload(
                                                    context,
                                                    context?.getString(R.string.edit_lesson_success)
                                                )
                                            }
                                        }
                                } else {
                                    view.hideProgressBarEdit()
                                    view.handleResponse(task.exception?.localizedMessage.toString())
                                }
                            }

                        }.addOnFailureListener {
                            view.hideProgressBarEdit()
                            view.handleResponse(it.localizedMessage?.toString().toString())
                        }

                    } else {
                        view.hideProgressBarEdit()
                        view.handleResponse(task.exception?.localizedMessage.toString())
                    }
                }
            }
        } else {
            database.collection("lessons")
                .document(lessonKey)
                .update("lesson", lessonName)
                .addOnSuccessListener {
                    view.onSuccessUpload(
                        context,
                        context?.getString(R.string.edit_lesson_success)
                    )
                }.addOnFailureListener {
                    view.handleResponse(it.localizedMessage)
                }
        }
    }
}