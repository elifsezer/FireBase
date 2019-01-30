package com.example.firebasekotlin

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shashank.sony.fancytoastlib.FancyToast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_kullanici_ayarlari.*
import org.w3c.dom.Text

class KullaniciAyarlariActivity : AppCompatActivity(),ProfilResmiFragment.onProfilResimListener {


    var izinlerVerildi=false
    var galeridengelenUrI:Uri?=null
    var kameradanGelenBitmap:Bitmap?=null


    override fun getResimYolu(resimPath: Uri?) {

        galeridengelenUrI=resimPath
        Picasso.get().load(galeridengelenUrI).resize(100,100).into(imgKullaniciResmi)
    }

    override fun getResimBitmap(bitmap: Bitmap) {
        kameradanGelenBitmap=bitmap
        imgKullaniciResmi.setImageBitmap(bitmap)
        //Picasso.get().load(bitmap).into(imgKullaniciResmi)
    }

    inner class  BackgroundResimCompress : AsyncTask<Uri,Void,ByteArray>()
    {

        override fun onPreExecute() {
            super.onPreExecute()
        }
        override fun doInBackground(vararg p0: Uri?): ByteArray {

        }

        override fun onProgressUpdate(vararg values: Void?) {
            super.onProgressUpdate(*values)
        }

        override fun onPostExecute(result: ByteArray?) {
            super.onPostExecute(result)
        }

    }


    var kullanici = FirebaseAuth.getInstance().currentUser!!
    var icClicked: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kullanici_ayarlari)


        kullaniciBilgileriniOku()

        tvDetayEmail.setText(kullanici?.email.toString())
        tvDetayName.setText(kullanici.displayName.toString())




        btnSifreGonder.setOnClickListener {
            FirebaseAuth.getInstance().sendPasswordResetEmail(kullanici.email.toString())
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        FancyToast.makeText(
                            this@KullaniciAyarlariActivity,
                            "Şifre Sıfırlama maili gönderildi.",
                            FancyToast.LENGTH_SHORT,
                            FancyToast.SUCCESS,
                            true
                        ).show()
                    } else {
                        FancyToast.makeText(
                            this@KullaniciAyarlariActivity,
                            "Hata Oluştu." + task.exception?.message,
                            FancyToast.LENGTH_SHORT,
                            FancyToast.ERROR,
                            true
                        ).show()
                    }
                }
        }
        btnDegisiklikleriKaydet.setOnClickListener {

            kullanici = FirebaseAuth.getInstance().currentUser!!
            if (tvDetayName.text.isNotEmpty()) {
                var bilgileriGuncelle = UserProfileChangeRequest.Builder()
                    .setDisplayName(tvDetayName.text.toString())
                    .build()
                kullanici.updateProfile(bilgileriGuncelle)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            //anlık olarak veritabanına kayıt ettik.
                            FirebaseDatabase.getInstance().reference
                                .child("kullanici")
                                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                                .child("isim")
                                    //kullanici tablosundan değil displayden yani aut
                                .setValue(tvDetayName.text.toString())
                            FancyToast.makeText(this@KullaniciAyarlariActivity, "Değişiklik Yapılmıştır.", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show()
                        }
                    }
            } else {
                FancyToast.makeText(
                    this@KullaniciAyarlariActivity,
                    "Kullanıcı adını giriniz.",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,
                    true
                ).show()
            }

            if (tvDetayNumara.text.toString().isNotEmpty()) {
                FirebaseDatabase.getInstance().reference
                    .child("kullanici")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child("telefon")
                    .setValue(tvDetayNumara.text.toString())
            }
        }
        btnSifreveMailGuncelle.setOnClickListener {
            if (tvDetaySifre.text.toString().isNotEmpty()) {
                //kullanıcıyı tekrardan sisteme dahil ediyoruz.
                var credentail =
                    EmailAuthProvider.getCredential(kullanici.email.toString(), tvDetaySifre.text.toString())
                kullanici.reauthenticate(credentail)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            guncellelayout.visibility = View.VISIBLE
                            btnMailGuncelle.setOnClickListener {
                                mailAdresiniGuncelle()
                            }
                            btnSifreGuncelle.setOnClickListener {
                                sifreBilgisiniGuncelle()
                            }
                        } else {
                            FancyToast.makeText(this@KullaniciAyarlariActivity, "Hatalı Şifre Girişi", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show()
                            guncellelayout.visibility = View.INVISIBLE
                        }
                    }
            } else {
                FancyToast.makeText(
                    this@KullaniciAyarlariActivity, "Güncellemeler İçin Şifrenizi Yazmalısınız..", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show()
            }
        }

        imgKullaniciResmi.setOnClickListener {

            if (izinlerVerildi)
            {
                var dialog=ProfilResmiFragment()
                dialog.show(supportFragmentManager,"fotosec")

            }
            else
            {
                izinleriIste()
            }

        }

    }

    private fun izinleriIste() {
        var izinler= arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA)

        if (ContextCompat.checkSelfPermission(this,izinler[0])==PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,izinler[1])==PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this,izinler[2])==PackageManager.PERMISSION_GRANTED)
        {
            izinlerVerildi=true
        }
        else
        {
            ActivityCompat.requestPermissions(this,izinler,150)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if(requestCode==150)
        {
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED  && grantResults[2]==PackageManager.PERMISSION_GRANTED )
            {
                var dialog=ProfilResmiFragment()
                dialog.show(supportFragmentManager,"fotosec")
            }
            else
            {
                FancyToast.makeText(this,"Tüm izinleri vermelisiniz.",FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show()
            }
        }
    }

    private fun kullaniciBilgileriniOku() {


        var referans=FirebaseDatabase.getInstance().reference

        //query 1
        var sorgu=referans.child("kullanici")
            .orderByKey()
            //uid göre sıralama yapılıyor.
            .equalTo(kullanici?.uid)
        sorgu.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            //veri değiştiğinde tetiklendiği zaman
            override fun onDataChange(p0: DataSnapshot) {
                //gelen verileri for ile gezeceğiz.
                for (singleSnapshot in p0.children)
                {
                    var okunanKullanici= singleSnapshot.getValue(Kullanici::class.java)
                    tvDetayName.setText(okunanKullanici?.isim)
                    tvDetayNumara.setText(okunanKullanici?.telefon)
                    Log.e("elif","Adı : "+okunanKullanici?.isim+" Telefon: "+okunanKullanici?.telefon)
                }
            }


        })


    }


    private fun sifreBilgisiniGuncelle() {
        if (kullanici!=null)
        {
            //şifre updatei
            kullanici.updatePassword(etSifreGuncelle.text.toString())
                .addOnCompleteListener {task->
                    if (task.isSuccessful)
                    {
                        FancyToast.makeText(this@KullaniciAyarlariActivity, "Şifreniz Değiştirildi Tekrar Giriş Yapınız.." ,FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show()
                        FirebaseAuth.getInstance().signOut()
                        finish()

                    }
                }
        }
    }

    private fun mailAdresiniGuncelle() {
        if (kullanici!=null)
        {
            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(etMailGuncelle.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    {
                        //birden fazla aynı mail adresi ile kayıt edilirse uyarı ver
                        if (task.getResult().signInMethods?.size==1)
                        {
                            FancyToast.makeText(this@KullaniciAyarlariActivity, "Mail Sisteme Kayıtlı" ,FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show()
                        }
                        else
                        {
                            //şifre updatei
                            kullanici.updateEmail(etMailGuncelle.text.toString())
                                .addOnCompleteListener {task->
                                    if (task.isSuccessful)
                                    {
                                        FancyToast.makeText(this@KullaniciAyarlariActivity, "Mail Adresiniz Değiştirildi Tekrar Giriş Yapınız.." ,FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show()
                                        FirebaseAuth.getInstance().signOut()
                                        finish()
                                    }
                                }
                        }
                    }
                    else
                    {
                        FancyToast.makeText(this@KullaniciAyarlariActivity, "Mail Adresi Güncellendi." ,FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show()
                    }
                }
        }
    }
}
