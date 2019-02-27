package com.example.firebasekotlin.dialogs


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.firebasekotlin.R


class ProfilResmiFragment : DialogFragment() {

    //
    lateinit var tvGaleridenSec:TextView
    lateinit var tvKameradanSec:TextView

    //fragment ve activity arasında baglantı yapıyoruz. veri alışverişni sağlayacak.
    interface onProfilResimListener
    {
        fun getResimYolu(resimPath:Uri?)
        fun getResimBitmap(bitmap: Bitmap)
    }


    lateinit var mProfilResimListener: onProfilResimListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //fragmentin alanlarına erişmek için bu şekilde atamamız gerekiyor.
        var v= inflater.inflate(R.layout.fragment_profil_resmi, container, false)

        tvGaleridenSec=v.findViewById(R.id.tvGaleridenFoto)
        tvKameradanSec=v.findViewById(R.id.tvKameradanFoto)

        tvKameradanSec.setOnClickListener {
            var intent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent,200)
        }

        tvGaleridenSec.setOnClickListener {
            var intent=Intent(Intent.ACTION_GET_CONTENT)
            //her türlü image ilgilendiğimizi belirttik.
            intent.type="image/*"
            startActivityForResult(intent,100)

        }
        return v
    }

    //yapılan seçimleri dinler. 2 farlı istek bir method var 2 farklı isteği requestcode ile ayrıyoruz.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //galeriden resim seçiliyor
        if(requestCode==100 && resultCode==Activity.RESULT_OK && data!=null)
        {
            var galeridenSecilenResimYolu=data.data
            mProfilResimListener.getResimYolu(galeridenSecilenResimYolu)
            dismiss()
        }

        //kameradan resim seçerken
        else if (requestCode==200 && resultCode==Activity.RESULT_OK && data!=null )
        {
            var kameradanCekilenResim: Bitmap
            kameradanCekilenResim=data.extras.get("data") as Bitmap
            mProfilResimListener.getResimBitmap(kameradanCekilenResim)
            dialog.dismiss()
        }

    }

    override fun onAttach(context: Context?) {
        mProfilResimListener=activity as onProfilResimListener
        super.onAttach(context)
    }


}
