package jp.techacademy.masaya.ishihara.qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import java.util.*
import kotlin.collections.HashMap

var favorite_flag  = false
class QuestionDetailActivity : AppCompatActivity() ,DatabaseReference.CompletionListener{

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mFavoriteRef: DatabaseReference
/*
    override  fun onBackPressed(){
        val intent = Intent(applicationContext, MainActivity::class.java)
        //    intent.putExtra("question", mQuestion)
        startActivity(intent)
        finish()
    }
 */
    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

            val map = dataSnapshot.value as Map<*, *>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] as? String ?: ""
            val name = map["name"] as? String ?: ""
            val uid = map["uid"] as? String ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onRestart() {
        super.onRestart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            fabo.isClickable = true
            fabo!!.hide()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 渡ってきたQuestionのオブジェクトを保持する

        val extras = intent.extras
        mQuestion = extras!!.get("question") as Question
        Log.d("GENRE",mQuestion.genre.toString())
        title = mQuestion.title
        setContentView(R.layout.activity_question_detail)

   //     val sp = PreferenceManager.getDefaultSharedPreferences(this)
   //     val val_Genre = sp.getString(NameKEY, "")


    //    Log.d("GENRE",val_Genre.toString())
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            fabo.isClickable = false
            fabo.hide()
        } else {



            val userID = FirebaseAuth.getInstance().currentUser!!.uid
            val dataBaseReference = FirebaseDatabase.getInstance().reference
            mFavoriteRef = dataBaseReference.child(FavoritePATH).child(userID).child(mQuestion.questionUid)//.child(mGenre.toString()).child("favoriteuid")
            mFavoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val data = dataSnapshot.value as Map<*, *>?
                    val genre = data?.get("genre") ?:""

                    if(genre.toString().isNotEmpty()){
                        favorite_flag = false
                        Log.d("favorite_flag1",genre.toString())
                        fabo.setImageResource(R.drawable.ic_favorite)
                    }else{
                        favorite_flag = true
                        fabo.setImageResource(R.drawable.ic_favorite_border)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
/*
            val userID = FirebaseAuth.getInstance().currentUser!!.uid
            val dataBaseReference = FirebaseDatabase.getInstance().reference
            mFavoriteRef = dataBaseReference.child(FavoritePATH).child(userID).child(mQuestion.questionUid)//.child(mGenre.toString()).child("favoriteuid")
            Log.d("mFavoriteRef",mFavoriteRef.toString())
            mFavoriteRef!!.addChildEventListener(mEventListener)

 */

            /*
            val userID = FirebaseAuth.getInstance().currentUser!!.uid
            val dataBaseReference = FirebaseDatabase.getInstance().reference
            val favoriteRef = dataBaseReference.child(FavoritePATH).child(userID)
            favoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("favavavavavavavvvavava","mGenreRef.toString()")
                    val data = snapshot.value as Map<*, *>?
                    val title = data?.get("title") ?:""
                    favorite_flag = true
                }
                override fun onCancelled(firebaseError: DatabaseError) {}
            })

             */
            fabo.setOnClickListener {
                if(favorite_flag){
                    fabo.setImageResource(R.drawable.ic_favorite)
                    favorite_flag = false
                    val view = findViewById<View>(android.R.id.content)
                    Snackbar.make(view, getString(R.string.added_favorite_message), Snackbar.LENGTH_LONG).show()
                    //push処理
                    val userID = FirebaseAuth.getInstance().currentUser!!.uid
                    val dataBaseReference = FirebaseDatabase.getInstance().reference
                    val favoriteRef =
                        dataBaseReference.child(FavoritePATH).child(userID).child(mQuestion.questionUid)
                    val data = HashMap<String, String>()
                    data["genre"] = mQuestion.genre.toString()
                    favoriteRef.setValue(data, this)
                }else{
                    fabo.setImageResource(R.drawable.ic_favorite_border)
                    favorite_flag = true
                    val view = findViewById<View>(android.R.id.content)
                    Snackbar.make(view, getString(R.string.delete_favorite_message), Snackbar.LENGTH_LONG).show()
                    val userID = FirebaseAuth.getInstance().currentUser!!.uid
                    val dataBaseReference = FirebaseDatabase.getInstance().reference
                    val favoriteRef =
                        dataBaseReference.child(FavoritePATH).child(userID).child(mQuestion.questionUid)
                    val data = HashMap<String, String>()
               //     data["favoriteuid"] = mQuestion.questionUid
                    data["genre"] = mQuestion.genre.toString()
                    favoriteRef.removeValue()
                }






            }

        }





        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()


       // val view = findViewById<View>(android.R.id.content)
      //  Snackbar.make(view, getString(R.string.create_account_failure_message), Snackbar.LENGTH_LONG).show()

   //     if (user != null) {
   //         Log.d("favorite_flag3",favorite_flag.toString())
/*
            fabo.setOnClickListener {
                 if(favorite_flag){
                    fabo.setImageResource(R.drawable.ic_favorite)
                    favorite_flag = false
                    val view = findViewById<View>(android.R.id.content)
                    Snackbar.make(view, getString(R.string.delete_favorite_message), Snackbar.LENGTH_LONG).show()
                }else{
                    fabo.setImageResource(R.drawable.ic_favorite_border)
                    favorite_flag = true
                    val view = findViewById<View>(android.R.id.content)
                    Snackbar.make(view, getString(R.string.added_favorite_message), Snackbar.LENGTH_LONG).show()
                }


                //push処理
                val userID = FirebaseAuth.getInstance().currentUser!!.uid
                val dataBaseReference = FirebaseDatabase.getInstance().reference
                val favoriteRef =
                    dataBaseReference.child(FavoritePATH).child(userID).child(mQuestion.questionUid)
                val data = HashMap<String, String>()
             //   data["favoriteuid"] = mQuestion.questionUid
                data["genre"] = mQuestion.genre.toString()
                favoriteRef.setValue(data, this)



            }

 */
            fab.setOnClickListener {
                // ログイン済みのユーザーを取得する
                val user = FirebaseAuth.getInstance().currentUser

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    val intent = Intent(applicationContext, LoginActivity2::class.java)
                    intent.putExtra("question", mQuestion)
                    startActivity(intent)
                    finish()

                } else {
                    // Questionを渡して回答作成画面を起動する
                    // --- ここから ---
                    val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                    intent.putExtra("question", mQuestion)
                    startActivity(intent)

                    // --- ここまで ---
                }
            }
            val dataBaseReference = FirebaseDatabase.getInstance().reference
            mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString())
                .child(mQuestion.questionUid).child(AnswersPATH)
            mAnswerRef.addChildEventListener(mEventListener)
  //      }


    }
    override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
        if (databaseError == null) {

        }
    }
}