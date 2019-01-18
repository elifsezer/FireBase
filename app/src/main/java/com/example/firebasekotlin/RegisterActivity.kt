package com.example.firebasekotlin

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        btnKayit.setOnClickListener {
            if (etMail1.text.isNotEmpty() && etSifre1.text.isNotEmpty() && etSifreTekrar.text.isNotEmpty()) {
                if (etSifre1.text.toString().equals(etSifreTekrar.text.toString())) {
                    yeniUyeKayit(etMail1.text.toString(), etSifre1.text.toString())
                } else {
                    FancyToast.makeText(this, "Şifreler Eşleşmiyor", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true)
                        .show()

                }
            } else {
                FancyToast.makeText(this, "Lütfen Boş Alanları Doldurunuz", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true)
                    .show()
            }

        }
    }

    private fun yeniUyeKayit(mail: String, sifre: String) {
        progressBarGoster()
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(mail, sifre)
            .addOnCompleteListener(object : OnCompleteListener<AuthResult> {
                //yukarıdaki işlem başarılı bir şekilde yapıldıysa aşağısı tetiklencek.
                override fun onComplete(p0: Task<AuthResult>) {
                    if (p0.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Üye Kayıt Edildi."+" "+FirebaseAuth.getInstance().currentUser?.uid, Toast.LENGTH_SHORT).show()
                        onayMailiGonder()
                        FirebaseAuth.getInstance().signOut()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Üye Kayıt Edilirken Sorun Oluştu."+" "+p0.exception?.message, Toast.LENGTH_SHORT)
                            .show()
                    }

                }
            })
        progressBarGizle()

    }

    private fun onayMailiGonder() {

        //hangi kullanıcıya mail attıgımızı bilmek için.oankiüye aslında.
        var kullanici=FirebaseAuth.getInstance().currentUser
        if (kullanici!=null)
        {
            //onaylama maili ve linki atılıyor.
            kullanici.sendEmailVerification().addOnCompleteListener(object: OnCompleteListener<Void>{
                override fun onComplete(p0: Task<Void>) {
                    if (p0.isSuccessful)
                    {
                        FancyToast.makeText(this@RegisterActivity, "Mail Kutunuzu Kontrol edip. Maili onaylayınız.", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true)
                            .show()
                    }
                    else
                    {
                        FancyToast.makeText(this@RegisterActivity, "Mail gönderirken sorun oluştu.", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true)
                            .show()
                    }
                }

            })
        }
    }

    private fun progressBarGoster() {
        progressBar.visibility = View.VISIBLE
    }

    private fun progressBarGizle() {
        progressBar.visibility = View.INVISIBLE
    }
}
