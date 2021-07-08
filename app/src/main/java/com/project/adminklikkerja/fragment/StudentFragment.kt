package com.project.adminklikkerja.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.project.adminklikkerja.R
import com.project.adminklikkerja.adapter.PagerAdapterStudent

class StudentFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_list, container, false)
        setHasOptionsMenu(true)

        val toolbar = view.findViewById(R.id.toolbar) as Toolbar
        val viewPager = view.findViewById(R.id.viewpager) as ViewPager2
        val tabsTeacher = view.findViewById(R.id.tabs) as TabLayout
        ((activity as AppCompatActivity).setSupportActionBar(toolbar))

        val tabMenus = arrayOf(
            getString(R.string.subscribe),
            getString(R.string.notSubscribe)
        )

        val pageAdapter = PagerAdapterStudent(this)

        viewPager.adapter = pageAdapter

        TabLayoutMediator(
            tabsTeacher,
            viewPager
        ) { tab, position ->
            tab.text = tabMenus[position]
        }.attach()

        return view
    }
}