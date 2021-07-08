package com.project.adminklikkerja.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.adminklikkerja.R
import com.project.adminklikkerja.activity.LinkActivity
import com.project.adminklikkerja.activity.PdfViewActivity
import com.project.adminklikkerja.activity.VideoPlayerActivity
import com.project.adminklikkerja.model.ClassResponse
import com.project.adminklikkerja.utils.UtilsConstant
import com.project.adminklikkerja.utils.UtilsConstant.LINK_LESSON
import com.project.adminklikkerja.utils.UtilsConstant.PDF_URL
import com.project.adminklikkerja.view.SubCategoryListener
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.category_item.view.card_category
import kotlinx.android.synthetic.main.category_item.view.tv_category
import kotlinx.android.synthetic.main.sub_category_item.view.*
import kotlinx.android.synthetic.main.sub_category_item.view.btn_menu

class SubCategoryListAdapter(
    options: FirestoreRecyclerOptions<ClassResponse>,
    private val listener: SubCategoryListener
) :
    FirestoreRecyclerAdapter<ClassResponse, SubCategoryListAdapter.ViewHolder>(options) {

    private lateinit var getContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sub_category_item, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: ClassResponse) {
        getContext = holder.itemView.context
        val getSubCategoryName = item.name.toString()
        val getFilePdf = item.pdf.toString()
        val getFileVideo = item.video.toString()
        val getFileAudio = item.audio.toString()
        val getLink = item.link.toString()
        val listSubKey = snapshots.getSnapshot(position).id

        holder.apply {
            if (getFilePdf != "null") {
                containerView.tv_book.visibility = View.VISIBLE
                containerView.tv_book.isSelected = true
            } else {
                containerView.tv_book.visibility = View.GONE
            }

            if (getFileVideo != "null") {
                containerView.tv_video.visibility = View.VISIBLE
                containerView.tv_video.isSelected = true
            } else {
                containerView.tv_video.visibility = View.GONE
            }

            if (getFileAudio != "null") {
                containerView.tv_audio.visibility = View.VISIBLE
                containerView.tv_audio.isSelected = true
            } else {
                containerView.tv_audio.visibility = View.GONE
            }

            if (getLink != "null") {
                containerView.tv_link.visibility = View.VISIBLE
                containerView.tv_link.isSelected = true
            } else {
                containerView.tv_link.visibility = View.GONE
            }

            if ((getFilePdf != "null") || (getFileVideo != "null") || (getFileAudio != "null") || (getLink != "null")) {
                containerView.tv_media.visibility = View.GONE
            } else {
                containerView.tv_media.visibility = View.VISIBLE
            }

            containerView.tv_category.text = "${position.plus(1)}. $getSubCategoryName"
            containerView.card_category.setOnClickListener {
                showLessonOption(getFilePdf, listSubKey, getLink, getFileVideo)
            }
            containerView.btn_menu.setOnClickListener {
                val popupMenu = PopupMenu(it.context, it)
                popupMenu.setOnMenuItemClickListener(object :
                    android.widget.PopupMenu.OnMenuItemClickListener,
                    PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(p0: MenuItem?): Boolean {
                        when (p0?.itemId) {
                            R.id.menu_edit -> {
                                listener.onEdit(listSubKey, getSubCategoryName)
                            }
                            R.id.menu_delete -> {
                                listener.onDelete(listSubKey)
                            }
                        }
                        return true
                    }
                })
                popupMenu.inflate(R.menu.item_list)
                popupMenu.show()
            }
        }
    }

    private fun showLessonOption(getFilePdf: String, listSubKey: String, getLink: String , getVideo: String) {
        val items: Array<CharSequence> = arrayOf("Lihat", "Upload")

        val alert = MaterialAlertDialogBuilder(getContext, R.style.MaterialAlertDialog_rounded)
            .setTitle("Pilih Opsi Menu")
            .setItems(items) { _, position ->
                when (position) {
                    0 -> {
                        showViewDialog(getFilePdf, getLink, getVideo)
                    }
                    1 -> {
                        showUploadDialog(listSubKey)
                    }
                }
            }
        alert.create()
        alert.show()
    }

    private fun showUploadDialog(listSubKey: String) {
        val items: Array<CharSequence> = arrayOf(
            "Upload Pdf",
            "Upload Video",
            "Upload Link"
        )

        val alert = MaterialAlertDialogBuilder(getContext, R.style.MaterialAlertDialog_rounded)
            .setTitle("Pilih Jenis File")
            .setItems(items) { _, position ->
                when (position) {
                    0 -> {
                        listener.onUploadPdf(listSubKey)
                    }
                    1 -> {
                        listener.uploadVideo(listSubKey)
                    }
                    2 -> {
                        listener.uploadLink(listSubKey)
                    }
                }
            }
        alert.create()
        alert.show()
    }

    private fun showViewDialog(getFilePdf: String, link: String, video: String) {
        val items: Array<CharSequence> = arrayOf("Tulisan", "Video", "Link")

        val alert = MaterialAlertDialogBuilder(getContext, R.style.MaterialAlertDialog_rounded)
            .setTitle("Pilih Media Pembelajaran")
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
                            i.putExtra(UtilsConstant.VIDEO_URL, video)
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
                        if (link != "null") {
                            val i = Intent(getContext, LinkActivity::class.java)
                            i.putExtra(LINK_LESSON, link)
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