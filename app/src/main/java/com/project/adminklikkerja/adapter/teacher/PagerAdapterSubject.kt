package com.project.adminklikkerja.adapter.teacher

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.project.adminklikkerja.fragment.ReviewListFragment
import com.project.adminklikkerja.fragment.teacher.StudentListFragment
import com.project.adminklikkerja.fragment.teacher.SubclassListFragment

class PagerAdapterSubject(fm: FragmentActivity) :
    FragmentStateAdapter(fm) {

    private val pages =
        listOf(SubclassListFragment(), StudentListFragment(), ReviewListFragment())

    override fun getItemCount(): Int {
        return pages.size
    }

    override fun createFragment(position: Int): Fragment {
        return pages[position]
    }
}