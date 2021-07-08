package com.project.adminklikkerja.fragment.teacher

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminklikkerja.R
import com.project.adminklikkerja.adapter.FirestoreTeacherApprovedAdapter
import com.project.adminklikkerja.model.TeacherModel
import kotlinx.android.synthetic.main.fragment_students.*

class TeacherApprovedFragment : Fragment() {

    private val mHandler = Handler(Looper.getMainLooper())
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_students, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepare()
        checkClass()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.search_menu, menu)
        val mSearchMenuItem = menu.findItem(R.id.menu_search)
        searchView = mSearchMenuItem.actionView as SearchView
        searchView.queryHint = "Cari Nama..."

        val searchManager = context?.getSystemService(Context.SEARCH_SERVICE) as SearchManager?
        searchView.setSearchableInfo(searchManager!!.getSearchableInfo((context as AppCompatActivity).componentName))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                mHandler.postDelayed({
                    searchData(newText)
                }, 500)
                return false
            }
        })
    }

    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
        if (searchView.query.toString().isNotEmpty()) {

            searchView.isIconified = true
            searchView.isIconified = true
        }
    }

    private fun prepare() {
        swipe_refresh?.setOnRefreshListener {
            checkClass()
        }
    }

    private fun searchData(query: String) {
        val data = FirebaseFirestore.getInstance()
            .collection("klikkerja")
            .document("teacher")
            .collection("teacherList")
            .orderBy("username").startAt(query).endAt(query + "\uf8ff")

        val options = FirestoreRecyclerOptions.Builder<TeacherModel>()
            .setQuery(data, TeacherModel::class.java)
            .setLifecycleOwner(this)
            .build()

        rv_teachers?.layoutManager = LinearLayoutManager(context)
        rv_teachers?.setHasFixedSize(true)

        val adapter = context?.let { FirestoreTeacherApprovedAdapter(options) }
        rv_teachers?.adapter = adapter
    }

    private fun requestData() {
        val query = FirebaseFirestore.getInstance()
            .collection("klikkerja")
            .document("teacher")
            .collection("teacherList")
            .orderBy("username")

        val options = FirestoreRecyclerOptions.Builder<TeacherModel>()
            .setQuery(query, TeacherModel::class.java)
            .setLifecycleOwner(this)
            .build()

        rv_teachers?.layoutManager = LinearLayoutManager(context)
        rv_teachers?.setHasFixedSize(true)

        val adapter = context?.let { FirestoreTeacherApprovedAdapter(options) }
        rv_teachers?.adapter = adapter
    }

    private fun checkClass() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("klikkerja")
            .document("teacher")
            .collection("teacherList")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot?.isEmpty == true) {
                    tv_not_teachers?.visibility = View.VISIBLE
                    rv_teachers?.visibility = View.GONE
                } else {
                    tv_not_teachers?.visibility = View.GONE
                    rv_teachers?.visibility = View.VISIBLE
                    requestData()
                }

                swipe_refresh?.isRefreshing = false
            }
    }
}