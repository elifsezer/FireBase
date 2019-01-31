package com.example.firebasekotlin


import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView

class YeniSohbetOdasDialogFragment : DialogFragment() {

    lateinit var etSohbetOdasiAdi: EditText
    lateinit var btnSohbetOdasiOlustur: Button
    lateinit var seekBarSeviye: SeekBar
    lateinit var tvKullaniciSeviye: TextView
    var mSeekProgress=0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_yeni_sohbet_odas_dialog, container, false)

        btnSohbetOdasiOlustur = view.findViewById(R.id.btnYeniSohbetOdasiOlustur)
        etSohbetOdasiAdi=view.findViewById(R.id.etYeniSohbetOdasi)
        seekBarSeviye=view.findViewById(R.id.seekBarSeviye)
        tvKullaniciSeviye=view.findViewById(R.id.tvYeniSohbetSeviye)

        seekBarSeviye.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            //içindeki progressBar seviye değiştiğinde burası tetiklenir.
            override fun onProgressChanged(seekBar : SeekBar?, progress: Int, fromUser: Boolean) {
                mSeekProgress=progress
                tvKullaniciSeviye.setText(mSeekProgress.toString())

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })

        return view
    }


}
