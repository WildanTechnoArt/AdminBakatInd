package com.project.adminklikkerja.adapter.teacher

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.adminklikkerja.R
import com.project.adminklikkerja.activity.LinkActivity
import com.project.adminklikkerja.activity.PdfViewActivity
import com.project.adminklikkerja.activity.VideoPlayerActivity
import com.project.adminklikkerja.model.SubjectResponse
import com.project.adminklikkerja.utils.UtilsConstant.LINK_LESSON
import com.project.adminklikkerja.utils.UtilsConstant.PDF_URL
import com.project.adminklikkerja.utils.UtilsConstant.VIDEO_URL
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.category_item.view.card_category
import kotlinx.android.synthetic.main.category_item.view.tv_category
import kotlinx.android.synthetic.main.sub_category_item.view.*

class TeacherSubjectListAdapter(
    options: FirestoreRecyclerOptions<SubjectResponse>
) :
    FirestoreRecyclerAdapter<SubjectResponse, TeacherSubjectListAdapter.ViewHolder>(options) {

    private lateinit var getContext: Context
    private var getSubCategoryName: String? = null
    private var getSubjectName: String? = null
    private var getFilePdf: String? = null
    private var getFileVideo: String? = null
    private var getFileAudio: String? = null
    private var getLink: String? = null
    private var classId: String? = null
    private var lessonId: String? = null
    private var subclassKey: String? = null
    private var subjectKey: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sub_category_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: SubjectResponse) {
        getContext = holder.itemView.context
        getSubjectName = item.name.toString()
        getSubCategoryName = item.subclassname.toString()
        getFilePdf = item.pdf.toString()
        getFileVideo = item.video.toString()
        getFileAudio = item.audio.toString()
        getLink = item.link.toString()
        classId = item.classid.toString()
        lessonId = item.lessonid.toString()
        subclassKey = item.subclassId.toString()
        subjectKey = snapshots.getSnapshot(position).id

        holder.apply {
            if (getFilePdf != "null") {
                containerView.tv_book.visibility = VISIBLE
                containerView.tv_book.isSelected = true
            } else {
                containerView.tv_book.visibility = GONE
            }

            if (getFileVideo != "null") {
                containerView.tv_video.visibility = VISIBLE
                containerView.tv_video.isSelected = true
            } else {
                containerView.tv_video.visibility = GONE
            }

            if (getFileAudio != "null") {
                containerView.tv_audio.visibility = VISIBLE
                containerView.tv_audio.isSelected = true
            } else {
                containerView.tv_audio.visibility = GONE
            }

            if (getLink != "null") {
                containerView.tv_link.visibility = VISIBLE
                containerView.tv_link.isSelected = true
            } else {
                containerView.tv_link.visibility = GONE
            }

            if ((getFilePdf != "null") || (getFileVideo != "null") || (getFileAudio != "null") || (getLink != "null")) {
                containerView.tv_media.visibility = GONE
            } else {
                containerView.tv_media.visibility = VISIBLE
            }

            containerView.tv_category.text = "${position.plus(1)}. $getSubjectName"
            containerView.btn_menu.visibility = GONE
            containerView.card_category.setOnClickListener {
                showAlertDialog(getFilePdf, getLink, getFileVideo)
            }
        }
    }

    private fun showAlertDialog(getFilePdf: String?, getLink: String?, video: String?) {
        val items: Array<CharSequence> = arrayOf("Tulisan", "Video", "Akses Link")

        val alert = MaterialAlertDialogBuilder(getContext, R.style.MaterialAlertDialog_rounded)
            .setTitle("Pilih Media")
            .setItems(items) { _, position ->
                when (position) {
                    0 -> {
                        if (getFilePdf != "null") {
                            val intent = Intent(getContext, PdfViewActivity::class.java)
                            intent.putExtra(PDF_URL, getFilePdf)
                            (getContext as AppCompatActivity).startActivity(intent)
                        } else {
                            Toast.makeText(
                                getContext,
                                getContext.getString(R.string.unable_to_access_note),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    1 -> {
                        if (video != "null") {
                            val i = Intent(getContext, VideoPlayerActivity::class.java)
                            i.putExtra(VIDEO_URL, video)
                            getContext.startActivity(i)
                        } else {
                            Toast.makeText(
                                getContext,
                                getContext.getString(R.string.unable_to_access_link),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    2 -> {
                        if (getLink != "null") {
                            val i = Intent(getContext, LinkActivity::class.java)
                            i.putExtra(LINK_LESSON, getLink)
                            getContext.startActivity(i)
                        } else {
                            Toast.makeText(
                                getContext,
                                getContext.getString(R.string.unable_to_access_link),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        alert.create()
        alert.show()
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}