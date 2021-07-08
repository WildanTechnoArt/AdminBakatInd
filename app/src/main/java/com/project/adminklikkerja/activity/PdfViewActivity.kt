package com.project.adminklikkerja.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.project.adminklikkerja.R
import com.project.adminklikkerja.utils.UtilsConstant.PDF_URL
import com.squareup.okhttp.Callback
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import kotlinx.android.synthetic.main.activity_pdf_view.*
import java.io.ByteArrayInputStream
import java.io.IOException

class PdfViewActivity : AppCompatActivity() {

    private var getPdfFile: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_view)
        prepare()
        loadContent()
        swipe_refresh?.setOnRefreshListener {
            loadContent()
        }
    }

    private fun prepare() {
        getPdfFile = intent.getStringExtra(PDF_URL)
    }

    private fun loadContent() {
        swipe_refresh?.isRefreshing = true

        val client = OkHttpClient()
        val request = Request.Builder().url(getPdfFile).build()

        client.newCall(request).enqueue(object : Callback {
            @Throws(IOException::class)
            override fun onResponse(response: Response?) {
                runOnUiThread {
                    if (!response?.isSuccessful!!) {
                        swipe_refresh?.isRefreshing = false
                        throw IOException("Failed to download file: $response")
                    } else {
                        swipe_refresh?.isRefreshing = false
                    }

                    val stream = ByteArrayInputStream(response.body()?.bytes())
                    pdfView.fromStream(stream).load()
                }
            }

            override fun onFailure(request: Request?, e: IOException?) {
                swipe_refresh?.isRefreshing = false
                e?.printStackTrace()
            }
        })
    }
}