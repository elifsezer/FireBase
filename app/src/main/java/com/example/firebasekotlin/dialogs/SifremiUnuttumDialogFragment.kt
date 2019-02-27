package com.example.firebasekotlin.dialogs
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.example.firebasekotlin.R
import com.google.firebase.auth.FirebaseAuth
import com.shashank.sony.fancytoastlib.FancyToast

class SifremiUnuttumDialogFragment : DialogFragment() {

    lateinit var emailEditText:EditText
    lateinit var mContext:FragmentActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view= inflater.inflate(R.layout.fragment_sifremi_unuttum_dialog, container, false)
        mContext=activity!!
        emailEditText=view.findViewById(R.id.etSifreyiTekrarGonder)

        var btnIptal=view.findViewById<Button>(R.id.btnSifreyiUnuttumIptal)

        btnIptal.setOnClickListener {
            dialog.dismiss()
        }
        var btnGonder=view.findViewById<Button>(R.id.btnSifreyiUnuttumGonder)
        btnGonder.setOnClickListener {

            FirebaseAuth.getInstance().sendPasswordResetEmail(emailEditText.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    {
                        FancyToast.makeText(mContext, "Şifre Sıfırlama maili gönderildi." , FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show()
                        dialog.dismiss()
                    }
                    else
                    {
                        FancyToast.makeText(mContext, "Hata Oluştu."+ task.exception?.message , FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()
                        dialog.dismiss()
                    }
                }
        }
        return view
    }
}
