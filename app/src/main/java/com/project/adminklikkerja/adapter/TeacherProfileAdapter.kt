package com.project.adminklikkerja.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.project.adminklikkerja.fragment.ClassSubscribeFragment
import com.project.adminklikkerja.fragment.teacher.TeacherProfileFragment
import com.project.adminklikkerja.fragment.teacher.ProfileClassListFragment

class TeacherProfileAdapter(fm: FragmentActivity) :
    FragmentStateAdapter(fm) {

    private val pages = listOf(TeacherProfileFragment(), ProfileClassListFragment(), ClassSubscribeFragment())

    override fun getItemCount(): Int {
        return pages.size
    }

    override fun createFragment(position: Int): Fragment {
        return pages[position]
    }
}