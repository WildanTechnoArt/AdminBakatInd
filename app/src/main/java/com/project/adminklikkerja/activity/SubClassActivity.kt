package com.project.adminklikkerja.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminklikkerja.R
import com.project.adminklikkerja.adapter.PagerAdapterSubclass
import com.project.adminklikkerja.model.ClassResponse
import com.project.adminklikkerja.utils.UtilsConstant.CATEGORY_KEY
import com.project.adminklikkerja.utils.UtilsConstant.CATEGORY_NAME
import com.project.adminklikkerja.utils.UtilsConstant.FAB_BROADCAST
import com.project.adminklikkerja.utils.UtilsConstant.LESSON_KEY
import com.project.adminklikkerja.utils.UtilsConstant.LESSON_NAME
import kotlinx.android.synthetic.main.activity_subclass.*
import kotlinx.android.synthetic.main.edit_lesson_dialog.view.*
import java.util.*

class SubClassActivity : AppCompatActivity() {

    private var getLesson: String? = null
    private var getLessonKey: String? = null
    private var getCategory: String? = null
    private var getCategoryKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subclass)
        prepare()
    }

    private fun prepare() {
        getLesson = intent.getStringExtra(LESSON_NAME).toString()
        getLessonKey = intent.getStringExtra(LESSON_KEY).toString()
        getCategory = intent.getStringExtra(CATEGORY_NAME).toString()
        getCategoryKey = intent.getStringExtra(CATEGORY_KEY).toString()

        setSupportActionBar(toolbar)
        supportActionBar?.title = getCategory

        val tabMenus = arrayOf(
            getString(R.string.subclass),
            getString(R.string.student_list),
            getString(R.string.review)
        )

        val pageAdapter = PagerAdapterSubclass(this)

        viewpager.adapter = pageAdapter

        TabLayoutMediator(
            tabs,
            viewpager
        ) { tab, position ->
            tab.text = tabMenus[position]
        }.attach()

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> fab_add_class.show()
                    else -> fab_add_class.hide()
                }
            }

        })

        fab_add_class.setOnClickListener {
            addNewSubClass()
        }

        getFabBroadcast()
    }

    private fun getFabBroadcast() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val getCondition = intent?.getBooleanExtra("endScroll", false)
                if (getCondition == true) {
                    fab_add_class.hide()
                } else {
                    fab_add_class.show()
                }
            }
        }

        val intentFilter = IntentFilter(FAB_BROADCAST)
        registerReceiver(receiver, intentFilter)
    }

    private fun addNewSubClass() {
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
                    .document()
                    .set(data)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Sub Kelas Berhasil Ditambahkan", Toast.LENGTH_SHORT)
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
        builder.setTitle("Tambah Kelas Baru")

        alertDialog = builder.create()
        alertDialog.show()
    }
}