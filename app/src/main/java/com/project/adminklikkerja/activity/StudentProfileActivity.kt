package com.project.adminklikkerja.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminklikkerja.GlideApp
import com.project.adminklikkerja.R
import com.project.adminklikkerja.adapter.ProfileAdapter
import com.project.adminklikkerja.utils.UtilsConstant
import com.project.adminklikkerja.utils.UtilsConstant.EMAIL
import com.project.adminklikkerja.utils.UtilsConstant.GET_PROFILE
import com.project.adminklikkerja.utils.UtilsConstant.IS_TEACHER
import com.project.adminklikkerja.utils.UtilsConstant.USERNAME
import kotlinx.android.synthetic.main.activity_student_profile.*
import kotlinx.android.synthetic.main.activity_student_profile.btn_menu
import kotlinx.android.synthetic.main.activity_student_profile.collapsing_toolbar
import kotlinx.android.synthetic.main.activity_student_profile.fab_add_class
import kotlinx.android.synthetic.main.activity_student_profile.img_profile
import kotlinx.android.synthetic.main.activity_student_profile.profile_tabs
import kotlinx.android.synthetic.main.activity_student_profile.progressBar
import kotlinx.android.synthetic.main.activity_student_profile.tv_username
import kotlinx.android.synthetic.main.activity_student_profile.viewpager
import kotlinx.android.synthetic.main.activity_teacher_profile.*

class StudentProfileActivity : AppCompatActivity() {

    private var mUserId: String? = null
    private var mPhotoUrl: String? = null
    private var mName: String? = null
    private var mEmail: String? = null
    private var getIsApprove: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_profile)
        init()
        getRegistrantStudent()
        showPhotoProfile()
    }

    private fun deleteUser() {
        progressBar.visibility = VISIBLE

        val map = HashMap<String, Any>()
        map["active"] = false
        map["approve"] = false
        map["device"] = 0

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(mUserId.toString())
            .update(map)
            .addOnSuccessListener {
                val db2 = FirebaseFirestore.getInstance()
                db2.collection("klikkerja")
                    .document("student")
                    .collection("studentList")
                    .document(mUserId.toString())
                    .delete()
                    .addOnSuccessListener {
                        progressBar.visibility = GONE
                        Toast.makeText(this, "Pengguna berhasil dihapus", Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener {
                        progressBar.visibility = GONE
                        Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener {
                progressBar.visibility = GONE
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    private fun init() {
        val tabMenus = arrayOf(
            getString(R.string.profile),
            getString(R.string.subscribe_class)
        )

        collapsing_toolbar.title = " "
        getIsApprove = intent.getBooleanExtra(UtilsConstant.IS_APPROVE, false)

        fab_add_class.hide()
        fab_add_class.setOnClickListener {
            val intent = Intent(this, AddSubsClassActivity::class.java)
            intent.putExtra(GET_PROFILE, mUserId)
            intent.putExtra(USERNAME, mName)
            intent.putExtra(EMAIL, mEmail)
            intent.putExtra(IS_TEACHER, false)
            startActivity(intent)
        }

        val pageAdapter = ProfileAdapter(this)

        viewpager.adapter = pageAdapter

        TabLayoutMediator(
            profile_tabs,
            viewpager
        ) { tab, position ->
            tab.text = tabMenus[position]
        }.attach()

        profile_tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (getIsApprove == true) {
                    when (tab?.position) {
                        0 -> fab_add_class.hide()
                        else -> fab_add_class.show()
                    }
                } else {
                    fab_add_class.hide()
                }
            }

        })

        mUserId = intent?.getStringExtra(GET_PROFILE).toString()

        btn_menu.setOnClickListener {
            val popupMenu = PopupMenu(it.context, it)
            popupMenu.setOnMenuItemClickListener(object :
                android.widget.PopupMenu.OnMenuItemClickListener,
                PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(p0: MenuItem?): Boolean {
                    when (p0?.itemId) {
                        R.id.menu_delete -> {
                            val builder = MaterialAlertDialogBuilder(
                                this@StudentProfileActivity,
                                R.style.MaterialAlertDialog_rounded
                            )
                                .setTitle("Konfirmasi")
                                .setMessage("Anda yakin ingin menghapusnya?")
                                .setPositiveButton("Ya") { _, _ ->
                                    deleteUser()
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
            })
            popupMenu.inflate(R.menu.student_profile_fragment)
            popupMenu.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getRegistrantStudent() {
        progressBar.visibility = VISIBLE

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(mUserId.toString())
            .addSnapshotListener { it, error ->
                progressBar.visibility = GONE
                if (error != null) {
                    Toast.makeText(this, error.localizedMessage, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                } else {
                    mName = it?.getString("username").toString()
                    mEmail = it?.getString("email").toString()
                    tv_username.text = mName
                }
            }
    }

    private fun showPhotoProfile() {
        val db = FirebaseFirestore.getInstance()
        db.collection("photos")
            .document(mUserId.toString())
            .get()
            .addOnSuccessListener {
                progressBar.visibility = GONE

                mPhotoUrl = it.getString("photoUrl").toString()

                GlideApp.with(applicationContext)
                    .load(mPhotoUrl)
                    .placeholder(R.drawable.profile_placeholder)
                    .into(img_profile)

            }.addOnFailureListener {
                progressBar.visibility = GONE
                Toast.makeText(this, getString(R.string.request_error), Toast.LENGTH_SHORT).show()
            }
    }
}