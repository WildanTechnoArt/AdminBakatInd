package com.project.adminklikkerja.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminklikkerja.GlideApp
import com.project.adminklikkerja.R
import com.project.adminklikkerja.adapter.ClassAdapter
import com.project.adminklikkerja.model.ClassResponse
import com.project.adminklikkerja.presenter.AddClassPresenter
import com.project.adminklikkerja.utils.UtilsConstant
import com.project.adminklikkerja.utils.UtilsConstant.CATEGORY_KEY
import com.project.adminklikkerja.utils.UtilsConstant.CATEGORY_NAME
import com.project.adminklikkerja.utils.UtilsConstant.GET_PROFILE
import com.project.adminklikkerja.utils.UtilsConstant.LESSON_KEY
import com.project.adminklikkerja.utils.UtilsConstant.LESSON_NAME
import com.project.adminklikkerja.view.AddClassView
import com.project.adminklikkerja.view.CategoryListener
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.input_class_dialog.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class ClassActivity : AppCompatActivity(), CategoryListener, AddClassView.View {

    private var getLesson: String? = null
    private var getLessonKey: String? = null
    private var alertDialogClass: AlertDialog? = null
    private lateinit var classDialogView: View
    private var imgResult: CropImage.ActivityResult? = null
    private lateinit var presenter: AddClassView.Presenter
    private var getClassImg: String? = null
    private var isSubscribe: Boolean? = false
    private var editWithImage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)
        prepare()
    }

    private fun prepare() {
        setSupportActionBar(toolbar)

        presenter = AddClassPresenter(this, this)

        getLesson = intent.getStringExtra(LESSON_NAME).toString()
        getLessonKey = intent.getStringExtra(LESSON_KEY).toString()

        supportActionBar?.title = getLesson

        rv_category?.layoutManager = GridLayoutManager(this, 2)
        rv_category?.setHasFixedSize(true)
        rv_category.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fab_new_class.hide()
                }

                if (!recyclerView.canScrollVertically(-1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fab_new_class.show()
                }
            }
        })

        setupDatabse()
        getDataCount()

        swipe_refresh?.setOnRefreshListener {
            setupDatabse()
            getDataCount()
        }

        fab_new_class.setOnClickListener {
            addNewClass()
        }
    }

    @SuppressLint("InflateParams")
    private fun addNewClass() {
        val builder = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded)
        classDialogView = (this as AppCompatActivity).layoutInflater.inflate(
            R.layout.input_class_dialog,
            null
        )

        bindProgressButton(classDialogView.btn_save)
        classDialogView.btn_save.attachTextChangeAnimator()

        classDialogView.btn_save.setOnClickListener {
            val getLessonName = classDialogView.input_lesson_name.text.toString()
            val getLink = classDialogView.input_link.text.toString()
            if (getLessonName.isEmpty() || getLink.isEmpty() || imgResult?.uri?.path.toString() == "null") {
                Toast.makeText(this, "Tidak boleh ada data yang kosong", Toast.LENGTH_SHORT)
                    .show()
            } else {
                imgResult?.let { it1 ->
                    presenter.uploadLesson(
                        getLessonKey.toString(),
                        getLessonName,
                        it1, isSubscribe, getLink
                    )
                }
            }
        }

        classDialogView.btn_upload.setOnClickListener {
            getPhotoFromStorage(it.context)
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

        builder.setView(classDialogView)
        builder.setTitle("Tambah Kelas")
        alertDialogClass = builder.create()
        alertDialogClass?.show()
    }

    @SuppressLint("InflateParams")
    override fun onEdit(
        categoryKey: String,
        categoryName: String,
        lessonImg: String,
        subscribe: Boolean,
        link: String
    ) {
        editWithImage = false

        val builder = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded)
        classDialogView = (this as AppCompatActivity).layoutInflater.inflate(
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
        classDialogView.input_lesson_name.setText(categoryName)
        classDialogView.input_link.setText(link)

        classDialogView.btn_upload.setOnClickListener {
            getPhotoFromStorage(it.context)
        }

        classDialogView.btn_save.setOnClickListener {
            val getLessonName = classDialogView.input_lesson_name.text.toString()
            val getLink = classDialogView.input_link.text.toString()
            if (editWithImage) {
                if (getLessonName.isEmpty() || getLink.isEmpty() ||imgResult?.uri?.path.toString() == "null") {
                    Toast.makeText(
                        this,
                        "Tidak boleh ada data yang kosong",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    imgResult?.let { it1 ->
                        isSubscribe?.let { it2 ->
                            presenter.editLesson(
                                editWithImage,
                                getLessonKey.toString(),
                                categoryKey,
                                getLessonName,
                                it1, it2, getLink
                            )
                        }
                    }
                }
            } else {
                if (getLessonName.isEmpty() || getLink.isEmpty()) {
                    Toast.makeText(this, "Tidak boleh ada data yang kosong", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    isSubscribe?.let { it1 ->
                        presenter.editLesson(
                            editWithImage,
                            getLessonKey.toString(),
                            categoryKey,
                            getLessonName,
                            null, it1, getLink
                        )
                    }
                }
            }
        }

        if (subscribe) {
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

        builder.setView(classDialogView)
        builder.setTitle("Edit Kelas")

        alertDialogClass = builder.create()
        alertDialogClass?.show()
    }

    private fun setupDatabse() {
        val query = FirebaseFirestore.getInstance()
            .collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .orderBy("date")

        val options = FirestoreRecyclerOptions.Builder<ClassResponse>()
            .setQuery(query, ClassResponse::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = ClassAdapter(options, this)
        rv_category?.adapter = adapter
    }

    private fun getDataCount() {
        val db = FirebaseFirestore.getInstance()
            .collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")

        db.addSnapshotListener { snapshot, _ ->
            if ((snapshot?.size() ?: 0) > 0) {
                rv_category?.visibility = View.VISIBLE
                tv_no_data?.visibility = View.GONE
            } else {
                rv_category?.visibility = View.GONE
                tv_no_data?.visibility = View.VISIBLE
            }
        }
        swipe_refresh?.isRefreshing = false
    }

    private fun getPhotoFromStorage(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            this.let {
                ActivityCompat.requestPermissions(
                    it, arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    UtilsConstant.PERMISSION_STORAGE
                )
            }

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
            this.let {
                CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(200, 200)
                    .start(it)
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

    override fun onClick(key: String, name: String, teacherId: String) {
        val intent = Intent(this, SubClassActivity::class.java)
        intent.putExtra(LESSON_NAME, getLesson)
        intent.putExtra(LESSON_KEY, getLessonKey)
        intent.putExtra(CATEGORY_NAME, name)
        intent.putExtra(CATEGORY_KEY, key)
        intent.putExtra(GET_PROFILE, teacherId)
        startActivity(intent)
    }

    override fun onDelete(key: String) {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(key)
            .delete()
            .addOnSuccessListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(this, "Kelas Berhasil Dihapus", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSuccessUpload(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT)
            .show()
        alertDialogClass?.dismiss()
    }

    override fun handleResponse(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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