package com.example.firebasekotlin


import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_yeni_sohbet_odas_dialog.*

class YeniSohbetOdasDialogFragment : DialogFragment() {

    lateinit var etSohbetOdasiAdi: EditText
    lateinit var btnSohbetOdasiOlustur: Button
    lateinit var seekBarSeviye: SeekBar
    lateinit var tvKullaniciSeviye: TextView
    var mSeekProgress = 0
    var kullaniciSeviye = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_yeni_sohbet_odas_dialog, container, false)

        btnSohbetOdasiOlustur = view.findViewById(R.id.btnYeniSohbetOdasiOlustur)
        etSohbetOdasiAdi = view.findViewById(R.id.etYeniSohbetOdasi)
        seekBarSeviye = view.findViewById(R.id.seekBarSeviye)
        tvKullaniciSeviye = view.findViewById(R.id.tvYeniSohbetSeviye)
        tvKullaniciSeviye.setText(mSeekProgress.toString())

        
        seekBarSeviye.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            //içindeki progressBar seviye değiştiğinde burası tetiklenir.
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mSeekProgress = progress
                tvKullaniciSeviye.setText(mSeekProgress.toString())

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })

        seekBarSeviye.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                mSeekProgress = progress
                tvKullaniciSeviye.setText(mSeekProgress.toString())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })

        kullaniciSeviyeGoster()

        btnSohbetOdasiOlustur.setOnClickListener {
            if (!etSohbetOdasiAdi.text.isNullOrEmpty()) {
                if (kullaniciSeviye >= seekBarSeviye.progress) {
                    //veritabanı baglantısı
                    var ref=FirebaseDatabase.getInstance().reference
                    var sohbetOdasiID=ref.child("sohbet_odasi").push().key
                    var yenisohbetOdasi=SohbetOdasi()
                    yenisohbetOdasi.olusturan_id=FirebaseAuth.getInstance().currentUser?.uid
                    yenisohbetOdasi.seviye=mSeekProgress.toString()
                    yenisohbetOdasi.sohbetodasi_adi=etSohbetOdasiAdi.text.toString()
                    yenisohbetOdasi.sohbetodasi_id=sohbetOdasiID

                    //yeni alan database eklendi.
                    ref.child("sohbet_odasi").child(sohbetOdasiID.toString()).setValue(yenisohbetOdasi)
                    

                } else {
                                     Toast.makeText(activity, "Seviye : "+ kullaniciSeviye, Toast.LENGTH_SHORT).show()

                }
            } else {
                Toast.makeText(activity, "Lütfen Boş Alanları Doldurunuz..", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    //o an giren kullacının seviye bilgisini databaseden alacağız.
    private fun kullaniciSeviyeGoster() {
        var ref = FirebaseDatabase.getInstance().reference

        var sorgu = ref.child("kullanici").orderByKey().equalTo(FirebaseAuth.getInstance().currentUser?.uid)
        sorgu.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (tekKayit in p0.children) {
                    //databaseden gelen seviye String biz bunu inte çevirdik.
                    kullaniciSeviye = tekKayit.getValue(Kullanici::class.java)?.seviye!!.toInt()
                }
            }

        })
    }


}
