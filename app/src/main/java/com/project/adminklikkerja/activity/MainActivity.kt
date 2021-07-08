package com.project.adminklikkerja.activity

import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.project.adminklikkerja.R
import com.project.adminklikkerja.fragment.LessonFragment
import com.project.adminklikkerja.fragment.SettingFragment
import com.project.adminklikkerja.fragment.StudentFragment
import com.project.adminklikkerja.fragment.teacher.TeacherFragment
import com.project.adminklikkerja.utils.UtilsConstant.KEY_FRAGMENT
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {

    private var pageContent: Fragment? = LessonFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        pageContent?.let { supportFragmentManager.putFragment(outState, KEY_FRAGMENT, it) }
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onNavigationItemSelected(menu: MenuItem): Boolean {
        when (menu.itemId) {
            R.id.lesson_list -> pageContent = LessonFragment()
            R.id.student_list -> pageContent = StudentFragment()
            R.id.teacher_list -> pageContent = TeacherFragment()
            R.id.menu_setting -> pageContent = SettingFragment()
        }

        pageContent?.let {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, it)
                .commit()
        }
        return true
    }

    private fun init(savedInstanceState: Bundle?) {
        bottom_navigation.setOnNavigationItemSelectedListener(this)
        if (savedInstanceState == null) {
            pageContent?.let {
                supportFragmentManager.beginTransaction().replace(R.id.container, it).commit()
            }
        } else {
            pageContent = supportFragmentManager.getFragment(savedInstanceState, KEY_FRAGMENT)
            pageContent?.let {
                supportFragmentManager.beginTransaction().replace(R.id.container, it).commit()
            }
        }
    }
}