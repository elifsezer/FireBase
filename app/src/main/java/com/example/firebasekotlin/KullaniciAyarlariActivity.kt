package com.example.firebasekotlin

import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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
import kotlinx.android.synthetic.main.activity_kullanici_ayarlari.*
import org.w3c.dom.Text

class KullaniciAyarlariActivity : AppCompatActivity() {

    var kullanici=FirebaseAuth.getInstance().currentUser!!
    var icClicked:Boolean=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kullanici_ayarlari)

        kullaniciBilgileriniOku()

        btnSifreGonder.setOnClickListener {
            FirebaseAuth.getInstance().sendPasswordResetEmail(kullanici!!.email.toString())
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
            if (tvDetaySifre.text!!.isNotEmpty() && tvDetayName.text.isNotEmpty()) {
                if (!tvDetayName.text.toString().equals(kullanici!!.displayName.toString())) {
                    var bilgileriGuncelle = UserProfileChangeRequest.Builder()
                        .setDisplayName(tvDetayName.text.toString())
                        .build()
                    kullanici!!.updateProfile(bilgileriGuncelle)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //database baglantısı yapıldı.
                                FirebaseDatabase.getInstance().reference
                                    .child("kullanici")
                                    .child(kullanici.uid)
                                    .child("isim")
                                    //değer ataması yapılıyor.
                                    .setValue(tvDetayName.text.toString())


                                FancyToast.makeText(this@KullaniciAyarlariActivity, "Değişiklik Yapılmıştır.", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show()
                            }
                        }
                }
            } else {
                FancyToast.makeText(this@KullaniciAyarlariActivity, "Boş Alanları Doldurunuz.", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()

            }
            if (tvDetayNumara.text.toString().isNotEmpty())
            {
                FirebaseDatabase.getInstance().reference
                    .child("kullanici")
                    .child(kullanici.uid)
                    .child("telefon")
                    .setValue(tvDetayNumara.text.toString())
            }
        }

        btnSifreveMailGuncelle.setOnClickListener {
            if (tvDetaySifre.text.toString().isNotEmpty()) {
                //kullanıcıyı tekrardan sisteme dahil ediyoruz.
                var credentail =
                    EmailAuthProvider.getCredential(kullanici!!.email.toString(), tvDetaySifre.text.toString())
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
                FancyToast.makeText(this@KullaniciAyarlariActivity, "Güncellemeler İçin Şifrenizi Yazmalısınız..", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show()

            }
        }
    }

    private fun kullaniciBilgileriniOku() {
        var referans=FirebaseDatabase.getInstance().reference
        var kullanici=FirebaseAuth.getInstance().currentUser

        tvDetayName.setText(kullanici!!.displayName.toString())
        tvDetayEmail.setText(kullanici!!.email.toString())

        //query 1
        var sorgu=referans.child("kullanici")
            //useridleri sıralıyoruz.
            .orderByKey()
                //filtreleme
            .equalTo(kullanici?.uid)
        sorgu.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
              //gelen verileri forla geziyoruz.
                for (singleSnapShot in p0.children)
                {
                    //okunan sınıfı kullanıcı tipine göre yaptık
                    var okunanKullanici=singleSnapShot.getValue(Kullanici::class.java)
                    tvDetayName.setText(okunanKullanici?.isim)
                    tvDetayNumara.setText(okunanKullanici?.telefon)
                }
            }
        })

        var sorgu2=referans.child("kullanici")
            //useridleri sıralıyoruz.
            .orderByChild("kullanici_id")
            //filtreleme
            .equalTo(kullanici?.uid)
        sorgu2.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                //gelen verileri forla geziyoruz.
                for (singleSnapShot in p0.children)
                {
                    //okunan sınıfı kullanıcı tipine göre yaptık
                    var okunanKullanici=singleSnapShot.getValue(Kullanici::class.java)
                    tvDetayName.setText(okunanKullanici?.isim)
                    tvDetayNumara.setText(okunanKullanici?.telefon)
                }
            }
        })


        var sorgu3=referans.child("kullanici")
            //useridleri sıralıyoruz.
            .orderByValue()
            //filtreleme
            .equalTo(kullanici?.uid)
        sorgu3.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                //gelen verileri forla geziyoruz.
                for (singleSnapShot in p0.children)
                {
                    //okunan sınıfı kullanıcı tipine göre yaptık
                    var okunanKullanici=singleSnapShot.getValue(Kullanici::class.java)
                    tvDetayName.setText(okunanKullanici?.isim)
                    tvDetayNumara.setText(okunanKullanici?.telefon)
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
