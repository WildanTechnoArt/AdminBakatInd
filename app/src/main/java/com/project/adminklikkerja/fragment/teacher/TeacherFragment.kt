package com.project.adminklikkerja.fragment.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.project.adminklikkerja.R
import com.project.adminklikkerja.adapter.PagerAdapterTeacher

class TeacherFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_teacher, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById(R.id.toolbar) as MaterialToolbar
        val viewPager = view.findViewById(R.id.viewpager) as ViewPager2
        val tabsTeacher = view.findViewById(R.id.tabs) as TabLayout

        if(activity is AppCompatActivity){
            (activity as AppCompatActivity).setSupportActionBar(toolbar)
        }

        val tabMenus = arrayOf(
            getString(R.string.subscribe),
            getString(R.string.notSubscribe)
        )

        val pageAdapter = PagerAdapterTeacher(this)

        viewPager.adapter = pageAdapter

        TabLayoutMediator(
            tabsTeacher,
            viewPager
        ) { tab, position ->
            tab.text = tabMenus[position]
        }.attach()
    }
}