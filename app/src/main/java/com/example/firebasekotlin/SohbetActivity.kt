package com.example.firebasekotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.firebasekotlin.dialogs.YeniSohbetOdasDialogFragment
import kotlinx.android.synthetic.main.activity_sohbet.*

class SohbetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sohbet)


        fabYeniSohbetOdasi.setOnClickListener {

            var dialog= YeniSohbetOdasDialogFragment()
            dialog.show(supportFragmentManager,"yenisohbetodasi")
        }
    }
}
