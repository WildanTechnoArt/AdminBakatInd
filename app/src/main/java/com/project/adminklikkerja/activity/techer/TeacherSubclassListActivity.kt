package com.project.adminklikkerja.activity.techer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.project.adminklikkerja.R
import com.project.adminklikkerja.adapter.teacher.PagerAdapterSubject
import com.project.adminklikkerja.utils.UtilsConstant
import kotlinx.android.synthetic.main.activity_teacher_subject_list.*
import kotlinx.android.synthetic.main.toolbar_layout.toolbar

class TeacherSubclassListActivity : AppCompatActivity() {

    private var getClassName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_subject_list)
        prepare()
    }

    private fun prepare() {
        getClassName = intent.getStringExtra(UtilsConstant.CATEGORY_NAME).toString()

        setSupportActionBar(toolbar)
        supportActionBar?.title = getClassName

        val tabMenus = arrayOf(
            getString(R.string.subclass_list),
            getString(R.string.student_list),
            getString(R.string.review)
        )

        val pageAdapter = PagerAdapterSubject(this)

        viewpager.adapter = pageAdapter

        TabLayoutMediator(
            tabs,
            viewpager
        ) { tab, position ->
            tab.text = tabMenus[position]
        }.attach()
    }
}