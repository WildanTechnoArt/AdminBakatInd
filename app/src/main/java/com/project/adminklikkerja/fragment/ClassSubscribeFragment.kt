package com.project.adminklikkerja.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminklikkerja.R
import com.project.adminklikkerja.adapter.FirestoreClassSubsAdapter
import com.project.adminklikkerja.model.SubClassResponse
import com.project.adminklikkerja.utils.UtilsConstant
import com.project.adminklikkerja.view.DeleteSubsClassListener
import kotlinx.android.synthetic.main.fragment_class_subscribe.*

class ClassSubscribeFragment : Fragment(), DeleteSubsClassListener {

    private var getUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_class_subscribe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepare(view)
        checkClass()
    }

    private fun prepare(view: View) {
        getUserId =
            (view.context as AppCompatActivity).intent?.getStringExtra(UtilsConstant.GET_PROFILE)
                .toString()
        swipe_refresh?.isEnabled = false
        swipe_refresh?.setOnRefreshListener {
            checkClass()
        }
    }

    private fun requestData() {
        val query = FirebaseFirestore.getInstance()
            .collection("subscribe")
            .document(getUserId.toString())
            .collection("class")

        val options = FirestoreRecyclerOptions.Builder<SubClassResponse>()
            .setQuery(query, SubClassResponse::class.java)
            .setLifecycleOwner(this)
            .build()

        rv_data_list?.layoutManager = LinearLayoutManager(context)
        rv_data_list?.setHasFixedSize(true)

        val adapter = context?.let { FirestoreClassSubsAdapter(this, options) }
        rv_data_list?.adapter = adapter
    }

    private fun checkClass() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("subscribe")
            .document(getUserId.toString())
            .collection("class")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot?.isEmpty == true) {
                    tv_not_data?.visibility = View.VISIBLE
                    rv_data_list?.visibility = View.GONE
                } else {
                    tv_not_data?.visibility = View.GONE
                    rv_data_list?.visibility = View.VISIBLE
                    requestData()
                }

                swipe_refresh?.isRefreshing = false
            }
    }

    override fun onDelete(classKey: String) {
        val db3 = FirebaseFirestore.getInstance()

        val builder = context?.let {
            MaterialAlertDialogBuilder(it, R.style.MaterialAlertDialog_rounded)
                .setTitle("Konfirmasi")
                .setMessage("Anda yakin ingin menghapusnya?")
                .setPositiveButton("Ya") { _, _ ->
                    swipe_refresh?.isRefreshing = true

                    db3.collection("subscribe")
                        .document(getUserId.toString())
                        .collection("class")
                        .document(classKey)
                        .delete()
                        .addOnSuccessListener {
                            swipe_refresh?.isRefreshing = false
                            Toast.makeText(context, "Berhasil Dihapus", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener { it1 ->
                            swipe_refresh?.isRefreshing = false
                            Toast.makeText(context, it1.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Tidak") { dialog, _ ->
                    dialog.dismiss()
                }
        }
        val dialog = builder?.create()
        dialog?.show()
    }
}