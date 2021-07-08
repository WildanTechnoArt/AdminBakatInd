package com.project.adminklikkerja.activity

import android.app.Activity
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminklikkerja.R
import com.project.adminklikkerja.adapter.SubCategoryListAdapter
import com.project.adminklikkerja.model.ClassResponse
import com.project.adminklikkerja.presenter.UploadPdfPresenter
import com.project.adminklikkerja.utils.UtilsConstant.CATEGORY_KEY
import com.project.adminklikkerja.utils.UtilsConstant.CATEGORY_NAME
import com.project.adminklikkerja.utils.UtilsConstant.FILE_PICK
import com.project.adminklikkerja.utils.UtilsConstant.LESSON_KEY
import com.project.adminklikkerja.utils.UtilsConstant.LESSON_NAME
import com.project.adminklikkerja.utils.UtilsConstant.PERMISSION_STORAGE
import com.project.adminklikkerja.utils.UtilsConstant.SUB_CATEGORY_KEY
import com.project.adminklikkerja.utils.UtilsConstant.SUB_CATEGORY_NAME
import com.project.adminklikkerja.view.PostView
import com.project.adminklikkerja.view.SubCategoryListener
import kotlinx.android.synthetic.main.activity_subclass_list.*
import kotlinx.android.synthetic.main.add_link_dialog.view.*
import kotlinx.android.synthetic.main.edit_lesson_dialog.view.*
import kotlinx.android.synthetic.main.edit_lesson_dialog.view.btn_cancel
import kotlinx.android.synthetic.main.edit_lesson_dialog.view.input_lesson_name
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.util.*

class SubjectListActivity : AppCompatActivity(), SubCategoryListener, PostView.View {

    private var getLesson: String? = null
    private var getLessonKey: String? = null
    private var getCategory: String? = null
    private var getCategoryKey: String? = null
    private var getSubCategory: String? = null
    private var getSubCategoryKey: String? = null
    private var listSubKey: String? = null
    private lateinit var presenter: PostView.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subclass_list)
        prepare()
    }

    private fun prepare() {
        setSupportActionBar(toolbar)

        getLesson = intent.getStringExtra(LESSON_NAME).toString()
        getLessonKey = intent.getStringExtra(LESSON_KEY).toString()
        getCategory = intent.getStringExtra(CATEGORY_NAME).toString()
        getCategoryKey = intent.getStringExtra(CATEGORY_KEY).toString()
        getSubCategory = intent.getStringExtra(SUB_CATEGORY_NAME).toString()
        getSubCategoryKey = intent.getStringExtra(SUB_CATEGORY_KEY).toString()

        supportActionBar?.title = getSubCategory

        presenter = UploadPdfPresenter(this, this)

        rv_category?.layoutManager = LinearLayoutManager(this)
        rv_category?.setHasFixedSize(true)
        rv_category?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
            addNewSubClassList()
        }
    }

    private fun addNewSubClassList() {
        var alertDialog: AlertDialog? = null

        val builder = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded)
        val dialogView = (this as AppCompatActivity).layoutInflater.inflate(
            R.layout.edit_lesson_dialog,
            null
        )

        bindProgressButton(dialogView.btn_accept)
        dialogView.btn_accept.attachTextChangeAnimator()

        dialogView.btn_accept.setOnClickListener {
            val getCategoryName = dialogView.input_lesson_name.text.toString()
            if (getCategoryName.isEmpty()) {
                Toast.makeText(this, "Tidak Boleh Kosong", Toast.LENGTH_SHORT).show()
            } else {
                dialogView.btn_accept.showProgress { progressColor = Color.WHITE }

                val data = ClassResponse()
                data.name = getCategoryName
                data.date = Calendar.getInstance().time

                val db = FirebaseFirestore.getInstance()
                db.collection("lessons")
                    .document(getLessonKey.toString())
                    .collection("category")
                    .document(getCategoryKey.toString())
                    .collection("subCategory")
                    .document(getSubCategoryKey.toString())
                    .collection("listSub")
                    .document()
                    .set(data)
                    .addOnSuccessListener {
                        setupDatabse()
                        getDataCount()
                        Toast.makeText(
                            this,
                            "Materi Kelas Berhasil Ditambahkan",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        dialogView.btn_accept.hideProgress(R.string.btn_save)
                        alertDialog?.dismiss()
                    }
                    .addOnFailureListener {
                        dialogView.btn_accept.hideProgress(R.string.btn_save)
                        Toast.makeText(this, it.localizedMessage?.toString(), Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }

        dialogView.btn_cancel.setOnClickListener {
            alertDialog?.dismiss()
        }

        builder.setView(dialogView)
        builder.setTitle("Buat Materi Baru")

        alertDialog = builder.create()
        alertDialog.show()
    }

    private fun setupDatabse() {
        val query = FirebaseFirestore.getInstance()
            .collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("subCategory")
            .document(getSubCategoryKey.toString())
            .collection("listSub")
            .orderBy("date")

        val options = FirestoreRecyclerOptions.Builder<ClassResponse>()
            .setQuery(query, ClassResponse::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = SubCategoryListAdapter(options, this)
        rv_category?.adapter = adapter
    }

    private fun getDataCount() {
        val db = FirebaseFirestore.getInstance()
            .collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("subCategory")
            .document(getSubCategoryKey.toString())
            .collection("listSub")

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

    private fun showDialogInputLink(field: String, subKey: String) {
        var alertDialog: AlertDialog? = null

        val builder = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded)
        val dialogView = (this as AppCompatActivity).layoutInflater.inflate(
            R.layout.add_link_dialog,
            null
        )

        bindProgressButton(dialogView.btn_add_link)
        dialogView.btn_add_link.attachTextChangeAnimator()

        dialogView.btn_add_link.setOnClickListener {
            val getLink = dialogView.input_link.text.toString()
            if (getLink.isEmpty()) {
                Toast.makeText(this, "Tidak Boleh Kosong", Toast.LENGTH_SHORT).show()
            } else {
                dialogView.btn_add_link.showProgress { progressColor = Color.WHITE }

                val db = FirebaseFirestore.getInstance()
                db.collection("lessons")
                    .document(getLessonKey.toString())
                    .collection("category")
                    .document(getCategoryKey.toString())
                    .collection("subCategory")
                    .document(getSubCategoryKey.toString())
                    .collection("listSub")
                    .document(subKey)
                    .update(field, getLink)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Link Berhasil Ditambahkan", Toast.LENGTH_SHORT)
                            .show()
                        dialogView.btn_add_link.hideProgress(R.string.btn_add_link)
                        alertDialog?.dismiss()
                    }
                    .addOnFailureListener {
                        dialogView.btn_add_link.hideProgress(R.string.btn_add_link)
                        Toast.makeText(this, it.localizedMessage?.toString(), Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }

        dialogView.btn_cancel.setOnClickListener {
            alertDialog?.dismiss()
        }

        builder.setView(dialogView)
        builder.setTitle("Tambah Link")

        alertDialog = builder.create()
        alertDialog.show()
    }

    override fun onUploadPdf(listSubKey: String) {
        this.listSubKey = listSubKey
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this, arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                PERMISSION_STORAGE
            )

        } else {

            val mimeTypes = arrayOf(
                "application/pdf"
            )

            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)

            intent.type = if (mimeTypes.size == 1) mimeTypes[0] else "*/*"
            if (mimeTypes.isNotEmpty()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                }
            }

            startActivityForResult(Intent.createChooser(intent, "SELECT FILE"), FILE_PICK)
        }
    }

    override fun uploadLink(listSubKey: String) {
        showDialogInputLink("link", listSubKey)
    }

    override fun uploadVideo(listSubKey: String) {
        showDialogInputLink("video", listSubKey)
    }

    override fun onDelete(key: String) {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("subCategory")
            .document(getSubCategoryKey.toString())
            .collection("listSub")
            .document(key)
            .delete()
            .addOnSuccessListener {
                setupDatabse()
                getDataCount()
                swipe_refresh?.isRefreshing = false
                Toast.makeText(this, "Materi Kelas Berhasil Dihapus", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onEdit(subClassKey: String, subClassName: String) {
        var alertDialog: AlertDialog? = null

        val builder = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded)
        val dialogView = (this as AppCompatActivity).layoutInflater.inflate(
            R.layout.edit_lesson_dialog,
            null
        )

        bindProgressButton(dialogView.btn_accept)
        dialogView.btn_accept.attachTextChangeAnimator()
        dialogView.btn_accept.text = getString(R.string.btn_edit)
        dialogView.input_lesson_name.setText(subClassName)

        dialogView.btn_accept.setOnClickListener {
            val getSubClassName = dialogView.input_lesson_name.text.toString()
            if (getSubClassName.isEmpty()) {
                Toast.makeText(this, "Tidak Boleh Kosong", Toast.LENGTH_SHORT).show()
            } else {
                dialogView.btn_accept.showProgress { progressColor = Color.WHITE }
                val db = FirebaseFirestore.getInstance()
                db.collection("lessons")
                    .document(getLessonKey.toString())
                    .collection("category")
                    .document(getCategoryKey.toString())
                    .collection("subCategory")
                    .document(getSubCategoryKey.toString())
                    .collection("listSub")
                    .document(subClassKey)
                    .update("name", getSubClassName)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Nama Berhasil Diubah", Toast.LENGTH_SHORT)
                            .show()
                        dialogView.btn_accept.hideProgress(R.string.btn_edit)
                        alertDialog?.dismiss()
                    }
                    .addOnFailureListener {
                        dialogView.btn_accept.hideProgress(R.string.btn_edit)
                        Toast.makeText(this, it.localizedMessage?.toString(), Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }

        dialogView.btn_cancel.setOnClickListener {
            alertDialog?.dismiss()
        }

        builder.setView(dialogView)
        builder.setTitle("Edit Nama Materi Skill")

        alertDialog = builder.create()
        alertDialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    val mimeTypes = arrayOf(
                        "application/pdf"
                    )

                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)

                    intent.type = if (mimeTypes.size == 1) mimeTypes[0] else "*/*"
                    if (mimeTypes.isNotEmpty()) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                        }
                    }

                    startActivityForResult(
                        Intent.createChooser(intent, "SELECT FILE"),
                        FILE_PICK
                    )
                }
                return
            }
        }
    }

    override fun handleResponse(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onSuccessPost(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        tv_progress.visibility = View.GONE
        progressBar.visibility = View.GONE
        shadow.visibility = View.GONE
    }

    override fun hideProgressBar() {
        tv_progress.visibility = View.GONE
        progressBar.visibility = View.GONE
        shadow.visibility = View.GONE
    }

    override fun showProgressBar(progress: Int) {
        tv_progress.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        shadow.visibility = View.VISIBLE
        progressBar.progress = progress
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_PICK && resultCode == Activity.RESULT_OK) {

            val fileData = data?.data
            fileData?.let { presenter.uploadFileDocument(it) }
            presenter.uploadToDatabase(
                getLessonKey.toString(),
                getCategoryKey.toString(), getSubCategoryKey.toString(), listSubKey
            )
        }
    }
}