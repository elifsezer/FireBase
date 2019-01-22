package com.example.firebasekotlin

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    //içerdeki metotlarında ulaşabilmesi için tanımladı.
    //kullanıcı onaylama durumu dinler.onaylanmış mı diye bu interface tanımlıyoruz.
    lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        var drawerToggle =
            object : ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.drawer_open, R.string.drawer_close) {
                override fun onDrawerClosed(drawerView: View) {
                    super.onDrawerClosed(drawerView)
                }

                override fun onDrawerOpened(drawerView: View) {
                    super.onDrawerOpened(drawerView)
                }
            }

        //menünün yandan açılması için özelliği aktifledik.
        drawerToggle.isDrawerIndicatorEnabled = true
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        navigation_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_cut -> {
                    var intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                    startActivity(intent)
                }

                R.id.action_copy -> {
                    var intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                }
            }
            drawer_layout.closeDrawer(GravityCompat.START)
            true
        }

        initMyAuthStateListener()
        tvKayitol.setOnClickListener {
            var intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        tvOnayMailiniTekrarGonder.setOnClickListener {
            var dialogGoster=OnayMailTekrarFragment()
            dialogGoster.show(supportFragmentManager,"gosterdialog")
        }
        //anonim classın içinde olduğumuz için direk this diyemiyoruz. O yüzdedn this@LoginActivity kullandık.
        btnGirisYap.setOnClickListener {
            if (etMail1.text.isNotEmpty() && etSifre1.text.isNotEmpty()) {
                progressBarGoster()
                //kullanıcıyı sisteme dahil ettik.
                FirebaseAuth.getInstance().signInWithEmailAndPassword(etMail1.text.toString(), etSifre1.text.toString())
                    .addOnCompleteListener(object : OnCompleteListener<AuthResult> {
                        override fun onComplete(p0: Task<AuthResult>) {
                            //gelen kullanıcının başarılı olması. kullanıcı mail ve sifresiyle sisteme giris yaptı.
                            if (p0.isSuccessful) {
                                progressBarGizle()
                               // FancyToast.makeText(this@LoginActivity, "Başarılı Giriş." + FirebaseAuth.getInstance().currentUser?.email, FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show()
                                if (!p0.result.user.isEmailVerified)
                                {
                                    FirebaseAuth.getInstance().signOut()
                                }

                            } else {
                                progressBarGizle()
                                //yazılan hatanın acıklamasını kullanıcıya gösterildi. p0 parametresiyle
                                FancyToast.makeText(this@LoginActivity, "Hatalı Giriş." + p0.exception?.message, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()
                            }
                        }
                    })
            } else {
                FancyToast.makeText(this@LoginActivity, "Lütfen Boş Alanları Doldurunuz.", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()

            }
        }
    }

    private fun Context.toast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun progressBarGoster() {
        progressBar2.visibility = View.VISIBLE
    }

    private fun progressBarGizle() {
        progressBar2.visibility = View.INVISIBLE
    }

    //interface ilk işlem yapılacağı yer
    //kullanıcı giriş veya çıkış yaptıgında tetiklenen method: (onAuthStateChanged)
    private fun initMyAuthStateListener() {
        mAuthStateListener = object : FirebaseAuth.AuthStateListener {
            //kullacının sisteme giriş ve çıkışlarında tetiklenen bir method.
            override fun onAuthStateChanged(p0: FirebaseAuth) {
                //currentuser ise kullanıcı giriş yapmış kullanıcının içi dolu.
                var kullanici = p0.currentUser

                if (kullanici != null) {
                    //mail adresi onaylanmış ve giriş yapılmış ise mainactivitye yönlendirme.

                    if (kullanici.isEmailVerified) {
                        FancyToast.makeText(this@LoginActivity, "Mail onaylanmış giriş yapabilir", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show()

                        var intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        FancyToast.makeText(this@LoginActivity, "Mail adresinizi onaylayınız.", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show()
                        //başarısız girişte sistem atmak için
                    }
                }
            }
        }
    }

    //ilk tetiklenen onStart interface ilk atamasını burada yapacağız
    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener)
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener)
    }


}
