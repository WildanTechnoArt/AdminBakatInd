package com.project.adminklikkerja.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import com.project.adminklikkerja.adapter.LessonAdapter
import com.project.adminklikkerja.model.LessonResponse
import com.project.adminklikkerja.presenter.AddLessonPresenter
import com.project.adminklikkerja.utils.UtilsConstant.GALLERY_PICK
import com.project.adminklikkerja.utils.UtilsConstant.PERMISSION_STORAGE
import com.project.adminklikkerja.view.AddLessonView
import com.project.adminklikkerja.view.EditLessonListener
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.edit_lesson_dialog.view.input_lesson_name
import kotlinx.android.synthetic.main.fragment_lesson.*
import kotlinx.android.synthetic.main.fragment_lesson.fab_new_class
import kotlinx.android.synthetic.main.fragment_lesson.swipe_refresh
import kotlinx.android.synthetic.main.fragment_lesson.tv_no_data
import kotlinx.android.synthetic.main.input_lesson_dialog.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class LessonFragment : Fragment(), EditLessonListener, AddLessonView.View {

    private var mContext: Context? = null
    private var imgResult: CropImage.ActivityResult? = null
    private var alertDialogLesson: AlertDialog? = null
    private lateinit var lessonDialogView: View
    private lateinit var presenter: AddLessonView.Presenter
    private var getLessonImg: String? = null
    private var editWithImage = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lesson, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepare(view)
        fab_new_class.setOnClickListener {
            showAddLessonDialog()
        }
    }

    private fun prepare(view: View) {
        setHasOptionsMenu(true)
        mContext = view.context

        presenter = AddLessonPresenter(mContext, this)

        (mContext as AppCompatActivity).setSupportActionBar(toolbar)
        (mContext as AppCompatActivity).supportActionBar?.title = "Materi Skill"

        rv_lesson?.layoutManager = GridLayoutManager(view.context, 2)
        rv_lesson?.setHasFixedSize(true)
        rv_lesson.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
    }

    private fun setupDatabse() {
        val query = FirebaseFirestore.getInstance()
            .collection("lessons")

        val options = FirestoreRecyclerOptions.Builder<LessonResponse>()
            .setQuery(query, LessonResponse::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = LessonAdapter(options, this)
        rv_lesson?.adapter = adapter
    }

    private fun getDataCount() {
        swipe_refresh?.isRefreshing = true
        val db = FirebaseFirestore.getInstance()
            .collection("lessons")

        db.addSnapshotListener { snapshot, _ ->
            if ((snapshot?.size() ?: 0) > 0) {
                rv_lesson?.visibility = View.VISIBLE
                tv_no_data?.visibility = View.GONE
            } else {
                rv_lesson?.visibility = View.GONE
                tv_no_data?.visibility = View.VISIBLE
            }
            swipe_refresh?.isRefreshing = false
        }
    }

    @SuppressLint("InflateParams")
    private fun showAddLessonDialog() {
        val builder = context.let {
            it?.let { it1 ->
                MaterialAlertDialogBuilder(
                    it1,
                    R.style.MaterialAlertDialog_rounded
                )
            }
        }
        lessonDialogView = (context as AppCompatActivity).layoutInflater.inflate(
            R.layout.input_lesson_dialog,
            null
        )

        bindProgressButton(lessonDialogView.btn_save)
        lessonDialogView.btn_save.attachTextChangeAnimator()

        lessonDialogView.btn_save.setOnClickListener {
            val getLessonName = lessonDialogView.input_lesson_name.text.toString()
            if (getLessonName.isEmpty() || imgResult?.uri?.path.toString() == "null") {
                Toast.makeText(context, "Gambar atau Nama Tidak Boleh Kosong", Toast.LENGTH_SHORT)
                    .show()
            } else {
                imgResult?.let { it1 -> presenter.uploadLesson(getLessonName, it1) }
            }
        }

        lessonDialogView.btn_upload.setOnClickListener {
            getPhotoFromStorage(it.context)
        }

        builder?.setView(lessonDialogView)
        builder?.setTitle("Tambah Materi Skill")
        alertDialogLesson = builder?.create()
        alertDialogLesson?.show()
    }

    private fun getPhotoFromStorage(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            activity?.let {
                ActivityCompat.requestPermissions(
                    it, arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    PERMISSION_STORAGE
                )
            }

        } else {
            val galleryIntent = Intent()
            galleryIntent.type = "image/*"
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(galleryIntent, "SELECT IMAGE"),
                GALLERY_PICK
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
            PERMISSION_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val galleryIntent = Intent()
                    galleryIntent.type = "image/*"
                    galleryIntent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(
                        Intent.createChooser(galleryIntent, "SELECT IMAGE"),
                        GALLERY_PICK
                    )
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_PICK && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            context?.let {
                CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(200, 200)
                    .start(it, this@LessonFragment)
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            imgResult = CropImage.getActivityResult(data)
            editWithImage = true
            mContext?.let {
                GlideApp.with(it)
                    .load(imgResult?.uri)
                    .into(lessonDialogView.img_lesson)
            }
        }
    }

    @SuppressLint("InflateParams")
    override fun onEdit(lessonKey: String, lessonName: String, lessonImg: String) {
        editWithImage = false

        val builder = context.let {
            it?.let { it1 ->
                MaterialAlertDialogBuilder(
                    it1,
                    R.style.MaterialAlertDialog_rounded
                )
            }
        }
        lessonDialogView = (context as AppCompatActivity).layoutInflater.inflate(
            R.layout.input_lesson_dialog,
            null
        )

        getLessonImg = lessonImg

        mContext?.let {
            GlideApp.with(it)
                .load(lessonImg)
                .into(lessonDialogView.img_lesson)
        }

        lessonDialogView.btn_save.text = getString(R.string.btn_edit)

        bindProgressButton(lessonDialogView.btn_save)
        lessonDialogView.btn_save.attachTextChangeAnimator()
        lessonDialogView.input_lesson_name.setText(lessonName)

        lessonDialogView.btn_upload.setOnClickListener {
            getPhotoFromStorage(it.context)
        }

        lessonDialogView.btn_save.setOnClickListener {
            val getLessonName = lessonDialogView.input_lesson_name.text.toString()
            if (editWithImage) {
                if (getLessonName.isEmpty() || imgResult?.uri?.path.toString() == "null") {
                    Toast.makeText(
                        context,
                        "Gambar atau Nama Tidak Boleh Kosong",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    imgResult?.let { it1 ->
                        presenter.editLesson(
                            lessonKey,
                            getLessonName,
                            it1,
                            editWithImage
                        )
                    }
                }
            } else {
                if (getLessonName.isEmpty()) {
                    Toast.makeText(context, "Nama Materi Tidak Boleh Kosong", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    presenter.editLesson(
                        lessonKey,
                        getLessonName,
                        null,
                        editWithImage
                    )
                }
            }
        }

        builder?.setView(lessonDialogView)
        builder?.setTitle("Edit Materi Skill")

        alertDialogLesson = builder?.create()
        alertDialogLesson?.show()
    }

    override fun onDelete(lessonKey: String) {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("lessons")
            .document(lessonKey)
            .delete()
            .addOnSuccessListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(context, "Materi Skill Berhasil Dihapus", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSuccessUpload(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT)
            .show()
        alertDialogLesson?.dismiss()
    }

    override fun handleResponse(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun hideProgressBar() {
        lessonDialogView.btn_save.hideProgress(R.string.btn_save)
    }

    override fun hideProgressBarEdit() {
        lessonDialogView.btn_save.hideProgress(R.string.btn_edit)
    }

    override fun showProgressBar() {
        lessonDialogView.btn_save.showProgress { progressColor = Color.WHITE }
    }
}