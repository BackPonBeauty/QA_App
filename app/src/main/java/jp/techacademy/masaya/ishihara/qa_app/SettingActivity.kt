package jp.techacademy.masaya.ishihara.qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    private lateinit var mDataBaseReference: DatabaseReference

    override  fun onBackPressed(){
        val intent = Intent(applicationContext, MainActivity::class.java)
        //    intent.putExtra("question", mQuestion)
        startActivity(intent)
        finish()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // Preferenceから表示名を取得してEditTextに反映させる
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // ログインしていない場合は何もしない
            changeButton.text = "ログインしていません"
            logoutButton.text = "ログインしていません"
        }else {
            val sp = PreferenceManager.getDefaultSharedPreferences(this)
            val name = sp.getString(NameKEY, "")
            nameText.setText(name)
        }

        mDataBaseReference = FirebaseDatabase.getInstance().reference

        // UIの初期設定
        title = getString(R.string.settings_titile)

        changeButton.setOnClickListener{v ->
            // キーボードが出ていたら閉じる
            val im = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていない場合は何もしない
                //    Snackbar.make(v, getString(R.string.no_login_user), Snackbar.LENGTH_LONG).show()
            } else {
                // 変更した表示名をFirebaseに保存する
                val name2 = nameText.text.toString()
                val userRef = mDataBaseReference.child(UsersPATH).child(user.uid)
                val data = HashMap<String, String>()
                data["name"] = name2
                userRef.setValue(data)

                // 変更した表示名をPreferenceに保存する
                val sp2 = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                val editor = sp2.edit()
                editor.putString(NameKEY, name2)
                editor.commit()

                Snackbar.make(v, getString(R.string.change_disp_name), Snackbar.LENGTH_LONG).show()
            }
        }

        logoutButton.setOnClickListener { v ->
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
           //     logoutButton.isClickable = false
                logoutButton.text = "ログインしていません"
            }else{
                FirebaseAuth.getInstance().signOut()
                nameText.setText("")
                Snackbar.make(v, getString(R.string.logout_complete_message), Snackbar.LENGTH_LONG).show()
                logoutButton.isClickable = false
                logoutButton.text = "ログインしていません"
                changeButton.isClickable = false
                changeButton.text = "ログインしていません"
            }
        }
    }
}