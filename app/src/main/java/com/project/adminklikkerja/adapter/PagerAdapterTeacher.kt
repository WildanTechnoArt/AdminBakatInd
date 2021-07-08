package com.project.adminklikkerja.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.project.adminklikkerja.fragment.teacher.TeacherApprovedFragment
import com.project.adminklikkerja.fragment.teacher.TeacherNotApprovedFragment

class PagerAdapterTeacher(fm: Fragment) :
    FragmentStateAdapter(fm) {

    private val pages =
        listOf(TeacherApprovedFragment(), TeacherNotApprovedFragment())

    override fun getItemCount(): Int {
        return pages.size
    }

    override fun createFragment(position: Int): Fragment {
        return pages[position]
    }
}