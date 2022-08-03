package jp.techacademy.masaya.ishihara.qa_app

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
// findViewById()を呼び出さずに該当Viewを取得するために必要となるインポート宣言
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*


class FavoriteActivity : AppCompatActivity() {    // ← 修正

    private var mGenre = 0   // ← 追加

    // --- ここから ---
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter

    private var mGenreRef: DatabaseReference? = null
    private var mFavoRef: DatabaseReference? = null
   // private lateinit var mTaskAdapter: TaskAdapter

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
//            val favpath = dataSnapshot.getValue(String::class.java)
            val map = dataSnapshot.value as Map<String, String>

            val favoriteuid = dataSnapshot.getKey().toString()
      //      Log.d("favpath",favpath.toString())
            val genre = map["genre"]?:""
            Log.d("genre",genre.toString())
            val favoriteRef = mDatabaseReference.child(ContentsPATH).child(genre).child(favoriteuid)//child(favpath.toString())
            Log.d("taaaaaaag",favoriteRef.toString())


            favoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val data = snapshot.value as Map<*, *>?
                        val title = data?.get("title") ?:""
                        val body = data?.get("body") ?: ""
                        val name = data?.get("name") ?: ""
                        val uid = data?.get("uid") ?: ""
                        val imageString = data?.get("image") .toString()?: ""
                        val bytes =
                            if (imageString.isNotEmpty()) {
                                Base64.decode(imageString, Base64.DEFAULT)
                            } else {
                                byteArrayOf()
                        }

                        //    val favoriteUid = map["FavoriteUid"] ?: ""
                        // Log.d("aaaaaaaaaaaaaaaaaaaaa",title)
                        val answerArrayList = ArrayList<Answer>()
                        val answerMap = data?.get("answers") as Map<String, String>?
                        if (answerMap != null) {
                            for (key in answerMap.keys) {
                                val temp = answerMap[key] as Map<String, String>
                                val answerBody = temp["body"] ?: ""
                                val answerName = temp["name"] ?: ""
                                val answerUid = temp["uid"] ?: ""
                                val answer = Answer(answerBody, answerName, answerUid, key)
                                answerArrayList.add(answer)
                            }
                        }


                        val question = Question(title.toString(), body.toString(), name.toString(), uid.toString(), dataSnapshot.key ?: "",mGenre, bytes, answerArrayList)
                        mQuestionArrayList.add(question)
                        mAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(firebaseError: DatabaseError) {}


            })

        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            // 変更があったQuestionを探す
            for (question in mQuestionArrayList) {
                if (dataSnapshot.key.equals(question.questionUid)) {
                    // このアプリで変更がある可能性があるのは回答（Answer)のみ
                    question.answers.clear()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            question.answers.add(answer)
                        }
                    }

                    mAdapter.notifyDataSetChanged()
                }
            }
        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }
    // --- ここまで追加する ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        mAdapter = QuestionsListAdapter(this)

        mQuestionArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

        reloadListView()

        listView.setOnItemClickListener{parent, view, position, id ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
            finish()
        }

    }
    private fun reloadListView() {
        /*
        val userID = FirebaseAuth.getInstance().currentUser!!.uid
        //    mGenreRef = mDatabaseReference.child(ContentsPATH).child(mGenre.toString())
        mGenreRef = mDatabaseReference.child(FavoritePATH).child(userID)//.child(mGenre.toString()).child("favoriteuid")
        mGenreRef!!.addChildEventListener(mEventListener)

         */

   //     val taskList = mutableListOf("aaa", "bbb", "ccc")

     //   mAdapter.mTaskList = taskList
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
        val userID = FirebaseAuth.getInstance().currentUser!!.uid
        mGenreRef = mDatabaseReference.child(FavoritePATH).child(userID)//.child(mGenre.toString()).child("favoriteuid")
        Log.d("taaaaaaaaaaaaaaaaaag",mGenreRef.toString())
        mGenreRef!!.addChildEventListener(mEventListener)
    }
    // --- ここまで追加する ---
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
  //      menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    // ～～ ここまで
}

