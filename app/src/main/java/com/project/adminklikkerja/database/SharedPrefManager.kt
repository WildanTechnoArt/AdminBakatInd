package com.project.adminklikkerja.database

import android.content.Context

class SharedPrefManager private constructor(context: Context) {

    init {
        mContext = context
    }

    companion object {

        //Key untuk menyimapn data pengajar sementara
        private const val TEACHER_PROFILE = "teacher_profile"

        //Key untuk mengambil Value pada SharedPreference
        private const val TEACHER_UID = "teacher_uid"

        private lateinit var mContext: Context

        private var mInstance: SharedPrefManager? = null

        @Synchronized
        fun getInstance(context: Context?): SharedPrefManager {
            if (mInstance == null)
                mInstance = context?.let { SharedPrefManager(it) }
            return mInstance!!
        }
    }

    val getTeacherUID: String?
        get() {
            val preferences = mContext.getSharedPreferences(TEACHER_PROFILE, Context.MODE_PRIVATE)
            return preferences.getString(TEACHER_UID, null)
        }

    fun deleteTeacher(): Boolean {
        val preferences = mContext.getSharedPreferences(TEACHER_PROFILE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.clear()
        return editor.commit()
    }

    fun saveTeacherUID(userId: String): Boolean {
        val preferences = mContext.getSharedPreferences(TEACHER_PROFILE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(TEACHER_UID, userId)
        editor.apply()
        return true
    }
}