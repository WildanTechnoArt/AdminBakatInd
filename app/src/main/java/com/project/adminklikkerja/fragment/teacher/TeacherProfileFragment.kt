package com.project.adminklikkerja.fragment.teacher

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminklikkerja.R
import com.project.adminklikkerja.utils.UtilsConstant
import kotlinx.android.synthetic.main.fragment_teacher_profile.*

class TeacherProfileFragment : Fragment() {

    private var mUserId: String? = null
    private var mEmail: String? = null
    private var mPhone: String? = null
    private var mAddress: String? = null
    private var mExperience: String? = null
    private var mDatetime: String? = null
    private var mStatus: Boolean? = null
    private var getIsApprove: Boolean? = null
    private lateinit var mContext: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_teacher_profile , container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContext = view.context
        init()
        getRegistrantStudent()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        mUserId = (mContext as AppCompatActivity).intent?.getStringExtra(UtilsConstant.GET_PROFILE)
            .toString()
        getIsApprove =
            (mContext as AppCompatActivity).intent.getBooleanExtra(UtilsConstant.IS_APPROVE, false)
        swipe_refresh?.isEnabled = false
    }

    @SuppressLint("SetTextI18n")
    private fun showStatusButton() {
        line_five?.visibility = View.VISIBLE
        switch_status?.visibility = View.VISIBLE
        tv_status?.visibility = View.VISIBLE

        if (mStatus == true) {
            switch_status?.text = "Aktif"
            switch_status?.setTextColor(ContextCompat.getColor(mContext, R.color.colorActive))
        } else {
            switch_status?.text = "Tidak Aktif"
            switch_status?.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
        }

        switch_status.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switch_status?.text = "Aktif"
                switch_status?.setTextColor(ContextCompat.getColor(mContext, R.color.colorActive))
                changeStatusUser(true)
            } else {
                switch_status?.text = "Tidak Aktif"
                switch_status?.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                changeStatusUser(false)
            }  
        }
    }

    private fun changeStatusUser(isActive: Boolean) {
        val updateList = mutableMapOf<String, Any?>()
        updateList["active"] = isActive
        updateList["device"] = 0

        swipe_refresh?.isRefreshing = true
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(mUserId.toString())
            .update(updateList)
            .addOnSuccessListener {
                swipe_refresh?.isRefreshing = false
            }.addOnFailureListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(mContext, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("SetTextI18n")
    private fun getRegistrantStudent() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(mUserId.toString())
            .addSnapshotListener { it, error ->
                swipe_refresh?.isRefreshing = false

                if (error != null) {
                    Toast.makeText(mContext, error.localizedMessage, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                } else {
                    mExperience = it?.getString("experience")
                    mEmail = it?.getString("email").toString()
                    mPhone = it?.getString("phone").toString()
                    mAddress = it?.getString("address").toString()
                    mDatetime = it?.getString("datetime").toString()
                    mStatus = it?.getBoolean("active")

                    tv_email?.text = mEmail
                    tv_phone_number?.text = mPhone
                    tv_experience?.text = mExperience
                    tv_address?.text = mAddress
                    tv_datetime?.text = "Terdaftar Sejak: $mDatetime"

                    if (getIsApprove == true) {
                        showStatusButton()
                        switch_status?.isChecked = mStatus == true
                    }
                }
            }
    }
}