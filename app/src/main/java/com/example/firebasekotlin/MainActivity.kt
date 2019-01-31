package com.example.firebasekotlin


import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    lateinit var myAutStateListener:FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar_1)
        initAuthStateListener()
        KullaniciBilgileri()

    }

    private fun KullaniciBilgileri()
    {
        tvDetaySifre.text=FirebaseAuth.getInstance().currentUser?.displayName.toString()
        tvUserId.text=FirebaseAuth.getInstance().currentUser?.uid.toString()
        tvEmail.text=FirebaseAuth.getInstance().currentUser?.email.toString()
        tvUserTelefon.text=FirebaseAuth.getInstance().currentUser?.phoneNumber

    }

    private fun initAuthStateListener() {
        myAutStateListener= object : FirebaseAuth.AuthStateListener {
            override fun onAuthStateChanged(p0: FirebaseAuth) {
                var kullanici= p0.currentUser
                if (kullanici!=null)
                {

                }
                else
                {
                    var intent=Intent(this@MainActivity,LoginActivity::class.java)
                    //geri butonuna basınca geri gelmesini önler
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    //main threaddden siliyoruz.
                    finish()
                }
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.icerik_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId)
        {
            R.id.menuCikisYap ->
            {
                cikisYap()
                return true
            }
            R.id.menuAyarlar ->
            {
                var intent=Intent(this@MainActivity,KullaniciAyarlariActivity::class.java)
                startActivity(intent)
                return true

            }
            R.id.menuSohbet ->
            {
                var intent=Intent(this@MainActivity,SohbetActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun cikisYap() {
        FirebaseAuth.getInstance().signOut()

    }

    override fun onResume() {
        super.onResume()
        var kullanici=FirebaseAuth.getInstance().currentUser
        if (kullanici==null)
        {
            var intent=Intent(this@MainActivity,LoginActivity::class.java)
            //geri butonuna basınca geri gelmesini önler
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            //main threaddden siliyoruz.
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(myAutStateListener)
    }

    override fun onStop() {
        super.onStop()
        if (myAutStateListener != null)
        {
            FirebaseAuth.getInstance().removeAuthStateListener(myAutStateListener)
        }
    }
}
