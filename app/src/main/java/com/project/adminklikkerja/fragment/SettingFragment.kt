package com.project.adminklikkerja.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.firebase.ui.auth.AuthUI
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.project.adminklikkerja.GlideApp
import com.project.adminklikkerja.R
import com.project.adminklikkerja.model.AdminModel
import com.project.adminklikkerja.utils.UtilsConstant
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.collections.HashMap

class SettingFragment : Fragment() {

    private lateinit var mContext: Context
    private var imageOption: Int? = null
    private val imageReference = FirebaseStorage.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        prepare(view)
        getPhoneNumber()
        getImageSlider1()
        getImageSlider2()
        getImageSlider3()
    }

    private fun prepare(view: View) {
        mContext = view.context

        (mContext as AppCompatActivity).setSupportActionBar(toolbar)
        (mContext as AppCompatActivity).supportActionBar?.title = "Pengaturan"

        bindProgressButton(btn_save)
        btn_save?.attachTextChangeAnimator()

        swipe_refresh?.setOnRefreshListener {
            getPhoneNumber()
            getImageSlider1()
            getImageSlider2()
            getImageSlider3()
        }

        btn_save.setOnClickListener {
            btn_save?.showProgress { progressColor = Color.WHITE }

            val data = AdminModel()
            data.admin = input_admin?.text.toString()
            data.accounting = input_accounting?.text.toString()
            data.cs = input_cs1?.text.toString()
            data.cs2 = input_cs2?.text.toString()

            val db = FirebaseFirestore.getInstance()
            db.collection("admin")
                .document("nomor WA")
                .set(data)
                .addOnSuccessListener {
                    btn_save.hideProgress(R.string.btn_save)
                    hideEdit()
                    Toast.makeText(context, "Nomor berhasil diubah", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    btn_save.hideProgress(R.string.btn_save)
                    Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
        }

        fab_slider1?.setOnClickListener {
            imageOption = 1
            getPhotoFromStorage()
        }

        fab_slider2?.setOnClickListener {
            imageOption = 2
            getPhotoFromStorage()
        }

        fab_slider3?.setOnClickListener {
            imageOption = 3
            getPhotoFromStorage()
        }
    }

    private fun getPhotoFromStorage() {
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it, android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED && context?.let {
                ContextCompat.checkSelfPermission(
                    it, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {

            activity?.let {
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

    private fun getImageSlider1() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("imageSlider")
            .document("43wAzg9pef4Bueg0mdu2")
            .addSnapshotListener { value, _ ->
                swipe_refresh?.isRefreshing = false
                val getImg = value?.getString("imageUrl").toString()
                context?.let { it1 ->
                    GlideApp.with(it1)
                        .load(getImg)
                        .placeholder(R.drawable.ic_image_200)
                        .into(img_slider1)
                }
            }
    }

    private fun getImageSlider2() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("imageSlider")
            .document("PASLrvx3qwouT3gimNqt")
            .addSnapshotListener { value, _ ->
                swipe_refresh?.isRefreshing = false
                val getImg = value?.getString("imageUrl").toString()
                context?.let { it1 ->
                    GlideApp.with(it1)
                        .load(getImg)
                        .placeholder(R.drawable.ic_image_200)
                        .into(img_slider2)
                }
            }
    }

    private fun getImageSlider3() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("imageSlider")
            .document("lYmGev5Hnq3k5SrJL5jG")
            .addSnapshotListener { value, _ ->
                swipe_refresh?.isRefreshing = false
                val getImg = value?.getString("imageUrl").toString()
                context?.let { it1 ->
                    GlideApp.with(it1)
                        .load(getImg)
                        .placeholder(R.drawable.ic_image_200)
                        .into(img_slider3)
                }
            }
    }

    private fun getPhoneNumber() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("admin")
            .document("nomor WA")
            .get()
            .addOnSuccessListener {
                swipe_refresh?.isRefreshing = false

                val getAdmin = it?.getString("admin")
                val getAccounting = it?.getString("accounting")
                val getCs = it?.getString("cs")
                val getCs2 = it?.getString("cs2")
                input_admin?.setText(getAdmin.toString())
                input_accounting?.setText(getAccounting.toString())
                input_cs1?.setText(getCs.toString())
                input_cs2?.setText(getCs2.toString())
            }
            .addOnFailureListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveEdit() {
        btn_save?.visibility = VISIBLE

        tv_slider1?.visibility = GONE
        img_slider1?.visibility = GONE
        fab_slider1?.visibility = GONE

        tv_slider2?.visibility = GONE
        img_slider2?.visibility = GONE
        fab_slider2?.visibility = GONE

        tv_slider3?.visibility = GONE
        img_slider3?.visibility = GONE
        fab_slider3?.visibility = GONE

        input_admin?.isEnabled = true
        input_accounting?.isEnabled = true
        input_cs1?.isEnabled = true
        input_cs2?.isEnabled = true
    }

    private fun hideEdit() {
        btn_save?.visibility = GONE

        tv_slider1?.visibility = VISIBLE
        img_slider1?.visibility = VISIBLE
        fab_slider1?.visibility = VISIBLE

        tv_slider2?.visibility = VISIBLE
        img_slider2?.visibility = VISIBLE
        fab_slider2?.visibility = VISIBLE

        tv_slider3?.visibility = VISIBLE
        img_slider3?.visibility = VISIBLE
        fab_slider3?.visibility = VISIBLE

        input_admin?.isEnabled = false
        input_accounting?.isEnabled = false
        input_cs1?.isEnabled = false
        input_cs2?.isEnabled = false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.setting_fragment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_edit -> {
                saveEdit()
            }
            R.id.menu_logout -> {
                val builder = MaterialAlertDialogBuilder(mContext, R.style.MaterialAlertDialog_rounded)
                    .setTitle("Konfirmasi")
                    .setMessage("Anda yakin ingin keluar?")
                    .setPositiveButton("Ya") { _, _ ->
                        swipe_refresh?.isRefreshing = true

                        AuthUI.getInstance()
                            .signOut(mContext)
                            .addOnSuccessListener {
                                swipe_refresh?.isRefreshing = false

                                Toast.makeText(
                                    mContext,
                                    getString(R.string.request_logout),
                                    Toast.LENGTH_SHORT
                                ).show()
                                (mContext as AppCompatActivity).finish()
                            }.addOnFailureListener {
                                swipe_refresh?.isRefreshing = false

                                Toast.makeText(
                                    mContext,
                                    getString(R.string.request_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    .setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss()
                    }
                val dialog = builder.create()
                dialog.show()
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UtilsConstant.GALLERY_PICK && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            this.let {
                activity?.let { it1 ->
                    CropImage.activity(imageUri)
                        .setAspectRatio(1, 1)
                        .setMinCropWindowSize(700, 200)
                        .start(it1)
                }
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            swipe_refresh?.isRefreshing = true
            val resultUri = CropImage.getActivityResult(data)?.uri

            val thumbImage = File(resultUri?.path.toString())

            val thumbBitmap = Compressor(context)
                .setMaxHeight(200)
                .setMaxWidth(700)
                .setQuality(100)
                .compressToBitmap(thumbImage)

            val bios = ByteArrayOutputStream()
            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bios)
            val imageByte = bios.toByteArray()

            val thumbURL = "image_slider/$imageOption.jpg"
            val thumbPath = imageReference.child(thumbURL)

            resultUri?.let { it1 ->
                thumbPath.putFile(it1).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        imageReference.child(thumbURL).downloadUrl.addOnSuccessListener { imageUri: Uri? ->

                            val uploadTask: UploadTask = thumbPath.putBytes(imageByte)

                            uploadTask.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val imageUrl = imageUri.toString()
                                    when(imageOption){
                                        1 -> {
                                            saveToDatabase(imageUrl, "43wAzg9pef4Bueg0mdu2")
                                        }
                                        2 -> {
                                            saveToDatabase(imageUrl, "PASLrvx3qwouT3gimNqt")
                                        }
                                        3 -> {
                                            saveToDatabase(imageUrl, "lYmGev5Hnq3k5SrJL5jG")
                                        }
                                    }
                                } else {
                                    swipe_refresh?.isRefreshing = false
                                    Toast.makeText(
                                        context,
                                        task.exception?.localizedMessage.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        }.addOnFailureListener {
                            swipe_refresh?.isRefreshing = false
                            Toast.makeText(
                                context,
                                it.localizedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        swipe_refresh?.isRefreshing = false
                        Toast.makeText(
                            context,
                            task.exception?.localizedMessage.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun saveToDatabase(imageUrl: String, documentId: String) {
        val data = HashMap<String, String>()
        data["imageUrl"] = imageUrl

        val query = FirebaseFirestore.getInstance()
        query.collection("imageSlider")
            .document(documentId)
            .set(data)
            .addOnSuccessListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(
                    context,
                    "Gambar berhasil diubah",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(
                    context,
                    it.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}