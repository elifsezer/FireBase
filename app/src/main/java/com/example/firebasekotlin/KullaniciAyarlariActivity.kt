package com.example.firebasekotlin

import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.activity_kullanici_ayarlari.*
import org.w3c.dom.Text

class KullaniciAyarlariActivity : AppCompatActivity() {
    var kullanici=FirebaseAuth.getInstance().currentUser!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kullanici_ayarlari)



        tvDetayName.setText(kullanici.displayName.toString())

        btnSifreGonder.setOnClickListener {
            FirebaseAuth.getInstance().sendPasswordResetEmail(kullanici.email.toString())
                .addOnCompleteListener { task->

                    if (task.isSuccessful)
                    {
                        FancyToast.makeText(this@KullaniciAyarlariActivity, "Şifre Sıfırlama maili gönderildi." , FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show()
                    }
                    else
                    {
                        FancyToast.makeText(this@KullaniciAyarlariActivity, "Hata Oluştu."+ task.exception?.message , FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()
                    }
                }
        }

        btnDegisiklikleriKaydet.setOnClickListener {
            if (tvDetaySifre.text.isNotEmpty() && tvDetayName.text.isNotEmpty())
            {
                if (!tvDetayName.text.toString().equals(kullanici.displayName.toString()))
                {
                    var bilgileriGuncelle=UserProfileChangeRequest.Builder()
                        .setDisplayName(tvDetayName.text.toString())
                        .build()
                    kullanici.updateProfile(bilgileriGuncelle)
                        .addOnCompleteListener {
                            task->
                            if (task.isSuccessful)
                            {
                                FancyToast.makeText(this@KullaniciAyarlariActivity, "Değişiklik Yapılmıştır." ,FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show()
                            }
                            else
                            {
                                FancyToast.makeText(this@KullaniciAyarlariActivity, "Hata Oluşmuştur." ,FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()
                            }
                        }
                }
            }
            else
            {
                FancyToast.makeText(this@KullaniciAyarlariActivity, "Boş Alanları Doldurunuz." ,FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()

            }
        }

        btnSifreveMailGuncelle.setOnClickListener {
            if (tvDetaySifre.text.toString().isNotEmpty())
            {
                //kullanıcıyı tekrardan sisteme dahil ediyoruz.
                var credentail=EmailAuthProvider.getCredential(kullanici.email.toString(),tvDetaySifre.text.toString())
                kullanici.reauthenticate(credentail)
                    .addOnCompleteListener { task->
                        if (task.isSuccessful)
                        {
                            guncellelayout.visibility= View.VISIBLE
                            btnMailGuncelle.setOnClickListener {
                                mailAdresiniGuncelle()
                            }
                            btnSifreGuncelle.setOnClickListener {
                                sifreBilgisiniGuncelle()
                            }
                        }
                        else
                        {
                            FancyToast.makeText(this@KullaniciAyarlariActivity, "Hatalı Şifre Girişi" ,FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show()
                            guncellelayout.visibility= View.INVISIBLE
                        }
                    }
            }
            else
            {
                FancyToast.makeText(this@KullaniciAyarlariActivity, "Güncellemeler İçin Şifrenizi Yazmalısınız.." ,FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show()

            }
        }

        imgSifreGoster.setOnClickListener {
                tvDetaySifre.inputType= (InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)

        }

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
}
