package com.project.adminklikkerja.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.project.adminklikkerja.fragment.NotSubscribedFragment
import com.project.adminklikkerja.fragment.SubscribedFragment

class PagerAdapterStudent(fm: Fragment) :
    FragmentStateAdapter(fm) {

    private val pages =
        listOf(SubscribedFragment(), NotSubscribedFragment())

    override fun getItemCount(): Int {
        return pages.size
    }

    override fun createFragment(position: Int): Fragment {
        return pages[position]
    }
}