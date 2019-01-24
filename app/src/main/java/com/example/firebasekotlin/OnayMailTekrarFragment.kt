package com.example.firebasekotlin


import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.shashank.sony.fancytoastlib.FancyToast


class OnayMailTekrarFragment : DialogFragment() {

    lateinit var emailEdittext: EditText
    lateinit var sifreEditText: EditText
    //oncreate bi kere ataması yapılıyor.
    lateinit var mContext:FragmentActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater!!.inflate(R.layout.fragment_dialog, container, false)
        emailEdittext = view.findViewById(R.id.etDialogMail)
        sifreEditText = view.findViewById(R.id.etDialogSifre)
        mContext= activity!!

        //findViewById viewi butona göre bul demek.
        var btnIptal = view.findViewById<Button>(R.id.btnDialogIptal)
        btnIptal.setOnClickListener {
            //o an açılan diyalogu iptal eder.
            dialog.dismiss()
        }


        var btnGonder = view.findViewById<Button>(R.id.btnDialogGonder)
        btnGonder.setOnClickListener {
            //fragmentin içinde oldugumuz için activity kullandık. Direk this kullanmıyoruz.
            //Toast.makeText(activity, "Gonder Basıldı", Toast.LENGTH_SHORT).show()

            if (emailEdittext.text.toString().isNotEmpty() && sifreEditText.text.toString().isNotEmpty()) {
                girisYapOnayMailiniTekrarGonder(emailEdittext.text.toString(), sifreEditText.text.toString())
            } else {
                FancyToast.makeText(mContext, "Boş Alanları Doldurunuz.", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show()
            }
        }
        return view
    }


    private fun girisYapOnayMailiniTekrarGonder(email: String, sifre: String) {

        var credentil = EmailAuthProvider.getCredential(email, sifre)
        //o anlık kullanıcıyı sisteme dahil ettik.
        FirebaseAuth.getInstance().signInWithCredential(credentil)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onayMailiniTekrarGonder()
                    dialog.dismiss()
                } else {
                    FancyToast.makeText(activity, "Email veya Şifre Hatalı..", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show()
                }
            }
    }

    private fun onayMailiniTekrarGonder() {
        var kullanici=FirebaseAuth.getInstance().currentUser
        if (kullanici!=null)
        {
            //kullanıcıya onaylama maili ve linki atılıyor. void:geriye dönüşü yok.
            kullanici.sendEmailVerification().addOnCompleteListener(object: OnCompleteListener<Void> {
                override fun onComplete(p0: Task<Void>) {
                    //mail gönderimi başarılı ise mail kutusuna gitmeli diye mesaj verildi.
                    if (p0.isSuccessful)
                    {
                        FancyToast.makeText(mContext, "Mail kutunuzu kontrol edin.", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true)
                            .show()
                    }
                    else
                    {
                        FancyToast.makeText(mContext, "Mail gönderirken sorun oluştu.", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true)
                            .show()
                    }
                }
            })
        }
    }
}
