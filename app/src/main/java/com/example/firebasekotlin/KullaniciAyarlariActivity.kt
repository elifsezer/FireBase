package com.example.firebasekotlin

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Toast
import com.example.firebasekotlin.dialogs.ProfilResmiFragment
import com.example.firebasekotlin.model.Kullanici
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

//fragmentten activiye veri yollamak. fragmentte interface oluşturup buraya ekledk

class KullaniciAyarlariActivity : AppCompatActivity(), ProfilResmiFragment.onProfilResimListener {

    var izinlerVerildi = false
    var galeridenGelenURI:Uri? =null
    var kameradanGelenBitmap:Bitmap?=null

    //hesap ayarlarında fotoyu ilgil alana koymak için yaptık.
    override fun getResimYolu(resimPath: Uri?) {

        galeridenGelenURI=resimPath
        Picasso.get().load(galeridenGelenURI).resize(100,100).into(imgKullaniciResmi)
    }

    override fun getResimBitmap(bitmap: Bitmap) {
        kameradanGelenBitmap=bitmap
        imgKullaniciResmi.setImageBitmap(bitmap)
    }



    var kullanici = FirebaseAuth.getInstance().currentUser!!
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
                //kullanıcı adı ile telefon noyu zorunlu tutmak için yapıldı.
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
                                FancyToast.makeText(
                                    this@KullaniciAyarlariActivity,
                                    "Değişiklik Yapılmıştır.",
                                    FancyToast.LENGTH_SHORT,
                                    FancyToast.SUCCESS,
                                    true
                                ).show()
                            }
                        }
                }
            } else {
                FancyToast.makeText(
                    this@KullaniciAyarlariActivity,
                    "Boş Alanları Doldurunuz.",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,
                    true
                ).show()
            }
            //telefon bilgisini zorunlu tutmadık.
            if (tvDetayNumara.text.toString().isNotEmpty()) {
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
                            FancyToast.makeText(
                                this@KullaniciAyarlariActivity,
                                "Hatalı Şifre Girişi",
                                FancyToast.LENGTH_SHORT,
                                FancyToast.WARNING,
                                true
                            ).show()
                            guncellelayout.visibility = View.INVISIBLE
                        }
                    }
            } else {
                FancyToast.makeText(
                    this@KullaniciAyarlariActivity,
                    "Güncellemeler İçin Şifrenizi Yazmalısınız..",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.WARNING,
                    true
                ).show()
            }
        }

        imgKullaniciResmi.setOnClickListener {
            if (izinlerVerildi) {
                var dialog = ProfilResmiFragment()
                dialog.show(supportFragmentManager, "fotosec")
            } else {
                izinleriIste()
            }


        }
    }

    private fun izinleriIste() {
        var izinler = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA)

        //kullanıcının bütün izinleri verip vermediğini kontrol ediyoruz.
        if(ContextCompat.checkSelfPermission(this,izinler[0])==PackageManager.PERMISSION_GRANTED &&
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
        if (requestCode==150)
        {
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED &&
                grantResults[1]==PackageManager.PERMISSION_GRANTED &&
                grantResults[2]==PackageManager.PERMISSION_GRANTED )
            {
                var dialog=ProfilResmiFragment()
                dialog.show(supportFragmentManager,"fotosec")
            }
            else
            {
                Toast.makeText(this@KullaniciAyarlariActivity,"Tüm izinleri vermelisiniz..",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun kullaniciBilgileriniOku() {
        tvDetayName.setText(kullanici!!.displayName.toString())
        tvDetayEmail.setText(kullanici!!.email.toString())
        var referans = FirebaseDatabase.getInstance().reference
        var kullanici = FirebaseAuth.getInstance().currentUser
        //query 1
        var sorgu = referans.child("kullanici")
            //useridleri sıralıyoruz.
            .orderByKey()
            //filtreleme
            .equalTo(kullanici?.uid)
        //sonuclari burada ögreniyoruz..
        sorgu.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                //gelen verileri forla geziyoruz.
                for (singleSnapShot in p0.children) {
                    //okunan sınıfı kullanıcı tipine göre yaptık
                    var okunanKullanici = singleSnapShot.getValue(Kullanici::class.java)
                    tvDetayName.setText(okunanKullanici?.isim)
                    tvDetayNumara.setText(okunanKullanici?.telefon)
                }
            }
        })

        var sorgu2 = referans.child("kullanici")
            //useridleri sıralıyoruz.
            .orderByChild("kullanici_id")
            //filtreleme
            .equalTo(kullanici?.uid)
        sorgu2.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                //gelen verileri forla geziyoruz.
                for (singleSnapShot in p0.children) {
                    //okunan sınıfı kullanıcı tipine göre yaptık
                    var okunanKullanici = singleSnapShot.getValue(Kullanici::class.java)
                    tvDetayName.setText(okunanKullanici?.isim)
                    tvDetayNumara.setText(okunanKullanici?.telefon)
                }
            }
        })
        var sorgu3 = referans.child("kullanici")
            //useridleri sıralıyoruz.
            .orderByValue()
            //filtreleme
            .equalTo(kullanici?.uid)
        sorgu3.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                //gelen verileri forla geziyoruz.
                for (singleSnapShot in p0.children) {
                    //okunan sınıfı kullanıcı tipine göre yaptık
                    var okunanKullanici = singleSnapShot.getValue(Kullanici::class.java)
                    tvDetayName.setText(okunanKullanici?.isim)
                    tvDetayNumara.setText(okunanKullanici?.telefon)
                }
            }
        })
    }

    private fun sifreBilgisiniGuncelle() {
        if (kullanici != null) { //şifre updatei
            kullanici.updatePassword(etSifreGuncelle.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        FancyToast.makeText(
                            this@KullaniciAyarlariActivity,
                            "Şifreniz Değiştirildi Tekrar Giriş Yapınız..",
                            FancyToast.LENGTH_SHORT,
                            FancyToast.SUCCESS,
                            true
                        ).show()
                        FirebaseAuth.getInstance().signOut()
                        finish()

                    }
                }
        }
    }

    private fun mailAdresiniGuncelle() {
        if (kullanici != null) {
            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(etMailGuncelle.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //birden fazla aynı mail adresi ile kayıt edilirse uyarı ver
                        if (task.getResult().signInMethods?.size == 1) {
                            FancyToast.makeText(
                                this@KullaniciAyarlariActivity,
                                "Mail Sisteme Kayıtlı",
                                FancyToast.LENGTH_SHORT,
                                FancyToast.WARNING,
                                true
                            ).show()
                        } else {
                            //şifre updatei
                            kullanici.updateEmail(etMailGuncelle.text.toString())
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        FancyToast.makeText(
                                            this@KullaniciAyarlariActivity,
                                            "Mail Adresiniz Değiştirildi Tekrar Giriş Yapınız..",
                                            FancyToast.LENGTH_SHORT,
                                            FancyToast.SUCCESS,
                                            true
                                        ).show()
                                        FirebaseAuth.getInstance().signOut()
                                        finish()
                                    }
                                }
                        }
                    } else {
                        FancyToast.makeText(
                            this@KullaniciAyarlariActivity,
                            "Mail Adresi Güncellendi.",
                            FancyToast.LENGTH_SHORT,
                            FancyToast.SUCCESS,
                            true
                        ).show()
                    }
                }
        }
    }
}
