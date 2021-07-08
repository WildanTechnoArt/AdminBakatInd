package com.project.adminklikkerja.fragment.teacher

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminklikkerja.GlideApp
import com.project.adminklikkerja.R
import com.project.adminklikkerja.adapter.teacher.ProfileClassAdapter
import com.project.adminklikkerja.model.teacher.ClassResponse
import com.project.adminklikkerja.presenter.TeacherClassPresenter
import com.project.adminklikkerja.utils.UtilsConstant
import com.project.adminklikkerja.view.ClassListListener
import com.project.adminklikkerja.view.TeacherClassView
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.fragment_tab_list.*
import kotlinx.android.synthetic.main.input_class_dialog.view.*

class ProfileClassListFragment : Fragment(), ClassListListener, TeacherClassView.View {

    private var getUserId: String? = null
    private var getClassImg: String? = null
    private var isSubscribe: Boolean? = false
    private var editWithImage = false
    private var alertDialogClass: AlertDialog? = null
    private lateinit var classDialogView: View
    private lateinit var presenter: TeacherClassView.Presenter
    private var imgResult: CropImage.ActivityResult? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tab_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepare(view)
        checkClass()
    }

    @SuppressLint("SetTextI18n")
    private fun prepare(view: View) {
        presenter = TeacherClassPresenter(context, this)

        getUserId =
            (view.context as AppCompatActivity).intent?.getStringExtra(UtilsConstant.GET_PROFILE)
                .toString()
        tv_not_data.text = "Belum ada kelas yang dibuat"
        swipe_refresh?.isEnabled = false
        swipe_refresh?.setOnRefreshListener {
            checkClass()
        }
    }

    private fun requestData() {
        val query = FirebaseFirestore.getInstance()
            .collection("teacher")
            .document(getUserId.toString())
            .collection("classList")
            .orderBy("date")

        val options = FirestoreRecyclerOptions.Builder<ClassResponse>()
            .setQuery(query, ClassResponse::class.java)
            .setLifecycleOwner(this)
            .build()

        rv_data_list?.layoutManager = LinearLayoutManager(context)
        rv_data_list?.setHasFixedSize(true)

        val adapter = ProfileClassAdapter(options, this)
        rv_data_list?.adapter = adapter
    }

    private fun checkClass() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("teacher")
            .document(getUserId.toString())
            .collection("classList")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot?.isEmpty == true) {
                    tv_not_data?.visibility = View.VISIBLE
                    rv_data_list?.visibility = View.GONE
                } else {
                    tv_not_data?.visibility = View.GONE
                    rv_data_list?.visibility = View.VISIBLE
                    requestData()
                }

                swipe_refresh?.isRefreshing = false
            }
    }

    override fun onDeleteClass(lessonId: String, classId: String) {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("teacher")
            .document(getUserId.toString())
            .collection("classList")
            .document(classId)
            .delete()
            .addOnSuccessListener {
                val db2 = FirebaseFirestore.getInstance()
                db2.collection("lessons")
                    .document(lessonId)
                    .collection("category")
                    .document(classId)
                    .get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            deleteClassIfPublished(lessonId, classId)
                        } else {
                            swipe_refresh?.isRefreshing = false
                            Toast.makeText(context, "Kelas berhasil dihapus", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }.addOnFailureListener {
                        swipe_refresh?.isRefreshing = false
                        Toast.makeText(context, it.localizedMessage?.toString(), Toast.LENGTH_SHORT)
                            .show()
                    }
            }.addOnFailureListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(context, it.localizedMessage?.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("InflateParams")
    override fun onEditClass(
        teacherId: String,
        subclassName: String,
        lessonId: String,
        classId: String,
        lessonImg: String,
        subscribe: Boolean?,
        link: String
    ) {
        editWithImage = false

        val builder =
            context?.let { MaterialAlertDialogBuilder(it, R.style.MaterialAlertDialog_rounded) }
        classDialogView = (context as AppCompatActivity).layoutInflater.inflate(
            R.layout.input_class_dialog,
            null
        )

        getClassImg = lessonImg

        this.let {
            GlideApp.with(it)
                .load(lessonImg)
                .into(classDialogView.img_lesson)
        }

        classDialogView.btn_save.text = getString(R.string.btn_edit)

        bindProgressButton(classDialogView.btn_save)
        classDialogView.btn_save.attachTextChangeAnimator()
        classDialogView.input_lesson_name.setText(subclassName)
        classDialogView.input_link.setText(link)

        classDialogView.btn_upload.setOnClickListener {
            getPhotoFromStorage(it.context)
        }

        classDialogView.btn_save.setOnClickListener {
            val getLessonName = classDialogView.input_lesson_name.text.toString()
            val getLink = classDialogView.input_link.text.toString()
            if (editWithImage) {
                if (getLessonName.isEmpty() || getLink.isEmpty() || imgResult?.uri?.path.toString() == "null") {
                    Toast.makeText(
                        context,
                        "Tidak boleh ada data yang kosong",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    imgResult?.let { it1 ->
                        isSubscribe?.let { it2 ->
                            presenter.editLesson(
                                teacherId,
                                editWithImage,
                                lessonId,
                                classId,
                                getLessonName,
                                it1, it2, getLink
                            )
                        }
                    }
                }
            } else {
                if (getLessonName.isEmpty() || getLink.isEmpty()) {
                    Toast.makeText(context, "Tidak boleh ada data yang kosong", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    isSubscribe?.let { it1 ->
                        presenter.editLesson(
                            teacherId,
                            editWithImage,
                            lessonId,
                            classId,
                            getLessonName,
                            null, it1, getLink
                        )
                    }
                }
            }
        }

        if (subscribe == true) {
            classDialogView.rb_subscribe.isChecked = true
            classDialogView.rb_trial.isChecked = false
            isSubscribe = true
        } else {
            classDialogView.rb_subscribe.isChecked = false
            classDialogView.rb_trial.isChecked = true
            isSubscribe = false
        }

        classDialogView.rg_subscribe.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_trial -> {
                    isSubscribe = false
                }
                R.id.rb_subscribe -> {
                    isSubscribe = true
                }
            }
        }

        builder?.setView(classDialogView)
        builder?.setTitle("Edit Kelas")

        alertDialogClass = builder?.create()
        alertDialogClass?.show()
    }

    private fun getPhotoFromStorage(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                (context as AppCompatActivity), arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                UtilsConstant.PERMISSION_STORAGE
            )

        } else {
            val galleryIntent = Intent()
            galleryIntent.type = "image/*"
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(galleryIntent, "SELECT IMAGE"),
                UtilsConstant.GALLERY_PICK
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            UtilsConstant.PERMISSION_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val galleryIntent = Intent()
                    galleryIntent.type = "image/*"
                    galleryIntent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(
                        Intent.createChooser(galleryIntent, "SELECT IMAGE"),
                        UtilsConstant.GALLERY_PICK
                    )
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UtilsConstant.GALLERY_PICK && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            context.let {
                it?.let { it1 ->
                    CropImage.activity(imageUri)
                        .setAspectRatio(1, 1)
                        .setMinCropWindowSize(200, 200)
                        .start(it1, this)
                }
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            imgResult = CropImage.getActivityResult(data)
            editWithImage = true
            this.let {
                GlideApp.with(it)
                    .load(imgResult?.uri)
                    .into(classDialogView.img_lesson)
            }
        }
    }

    private fun deleteClassIfPublished(lessonId: String, classId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("lessons")
            .document(lessonId)
            .collection("category")
            .document(classId)
            .delete()
            .addOnSuccessListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(context, "Kelas berhasil dihapus", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(context, it.localizedMessage?.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSuccessUpload(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT)
            .show()
        alertDialogClass?.dismiss()
    }

    override fun handleResponse(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun hideProgressBar() {
        classDialogView.btn_save.hideProgress(R.string.btn_save)
    }

    override fun hideProgressBarEdit() {
        classDialogView.btn_save.hideProgress(R.string.btn_edit)
    }

    override fun showProgressBar() {
        classDialogView.btn_save.showProgress { progressColor = Color.WHITE }
    }

    override fun showProgressBarEdit() {
        classDialogView.btn_save.showProgress { progressColor = Color.WHITE }
    }
}