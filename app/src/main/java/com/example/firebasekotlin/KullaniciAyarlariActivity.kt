package com.example.firebasekotlin

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.shashank.sony.fancytoastlib.FancyToast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_kullanici_ayarlari.*
import kotlinx.android.synthetic.main.activity_register.*
import org.w3c.dom.Text
import java.io.ByteArrayOutputStream
import java.lang.Exception

class KullaniciAyarlariActivity : AppCompatActivity(),ProfilResmiFragment.onProfilResimListener {


    var izinlerVerildi=false
    var galeridengelenUrI:Uri?=null
    var kameradanGelenBitmap:Bitmap?=null
    val MEGABYTE=100000.toDouble()


    override fun getResimYolu(resimPath: Uri?) {

        galeridengelenUrI=resimPath
        Picasso.get().load(galeridengelenUrI).resize(100,100).into(imgKullaniciResmi)
    }

    override fun getResimBitmap(bitmap: Bitmap) {
        kameradanGelenBitmap=bitmap
        imgKullaniciResmi.setImageBitmap(bitmap)
        //Picasso.get().load(bitmap).into(imgKullaniciResmi)
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

            if(galeridengelenUrI!= null)
            {
                fotografCompressed(galeridengelenUrI!!)
            }else if(kameradanGelenBitmap!=null)
            {
                fotografCompressed(kameradanGelenBitmap!!)
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

    inner class  BackgroundResimCompress : AsyncTask<Uri,Double,ByteArray>
    {
        var myBitmap:Bitmap?=null
        constructor() {}
        constructor(bm:Bitmap) {
            //kameradan resim çekilmişse
            if (bm != null)
            {
                myBitmap=bm
            }

        }
        override fun onPreExecute() {
            super.onPreExecute()
        }
        override fun doInBackground(vararg params: Uri?): ByteArray {

            //galeriden resim seçilmiş
            if (myBitmap== null)
            {
                myBitmap=MediaStore.Images.Media.getBitmap(this@KullaniciAyarlariActivity.contentResolver,params[0])
            }
            //bütün fotoları 1 ve 0 dönüştürcek
            var resimBytes:ByteArray?=null
            for (i in 1..5)
            {
                resimBytes=convertBitmaptoByte(myBitmap,100/i)
                publishProgress(resimBytes!!.size.toDouble())
            }
            return resimBytes!!
        }

        override fun onProgressUpdate(vararg values: Double?) {
            super.onProgressUpdate(*values)
            Toast.makeText(this@KullaniciAyarlariActivity,"Şuanki byte: "+values[0]!!/MEGABYTE,Toast.LENGTH_SHORT).show()
        }

        private fun convertBitmaptoByte(myBitmap: Bitmap?, i: Int): ByteArray? {
            var stream=ByteArrayOutputStream()
            myBitmap?.compress(Bitmap.CompressFormat.JPEG,i,stream)
            return stream.toByteArray()
        }

        override fun onPostExecute(result: ByteArray?) {
            super.onPostExecute(result)
            uploadResimtoFirebase(result)
        }

    }

    private fun uploadResimtoFirebase(result: ByteArray?) {

       progressGoster()
        var storageReferans=FirebaseStorage.getInstance().getReference()
        //her kullancının klasörü farklı olacak. referabs noktası oluşturuldu.
        var resimEklenecekYer=storageReferans.child("images/users" + FirebaseAuth.getInstance().currentUser?.uid + "/profile_resim")

        var uploadGorevi=resimEklenecekYer.putBytes(result!!)
        uploadGorevi.addOnSuccessListener (object: OnSuccessListener<UploadTask.TaskSnapshot>{
            override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                var firebaseURL=p0?.storage?.downloadUrl
                Toast.makeText(this@KullaniciAyarlariActivity,"Resmin yolu : "+firebaseURL.toString(),Toast.LENGTH_SHORT).show()
                FirebaseDatabase.getInstance().reference
                    .child("kullanici")
                    .child(kullanici?.uid)
                    .child("profil_resmi")
                    .setValue(firebaseURL.toString())
                progressGizle()
            }
        }).addOnFailureListener(object : OnFailureListener {
            override fun onFailure(p0: Exception) {
                Toast.makeText(this@KullaniciAyarlariActivity,"Resim Yüklenirken bir hata oluştu.",Toast.LENGTH_SHORT).show()
            }
        })


    }

    private fun fotografCompressed(galeridengelenUrI: Uri) {

        var compressed=BackgroundResimCompress()
        compressed.execute(galeridengelenUrI)
    }
    private fun fotografCompressed(kameradangleneUrI: Bitmap) {
        var compressed=BackgroundResimCompress(kameradangleneUrI)
        var uri:Uri?=null
        compressed.execute(uri)
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
                    Picasso.get().load(okunanKullanici?.profil_resmi).resize(100,100).into(imgKullaniciResmi)

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

    fun progressGoster()
    {
        progressBar3.visibility=View.VISIBLE
    }

    fun progressGizle()
    {
        progressBar3.visibility=View.INVISIBLE
    }
}
