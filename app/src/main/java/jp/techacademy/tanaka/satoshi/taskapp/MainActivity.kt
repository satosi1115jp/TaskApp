package jp.techacademy.tanaka.satoshi.taskapp

import android.content.ContentUris
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import io.realm.RealmChangeListener
import io.realm.Sort
import java.util.*

const val EXTRA_TASK = "jp.techacademy.tanaka.satoshi.taskapp.TASK"

class MainActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm) {
            reloadListView()
        }

    }
    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener { view ->
            /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()*/
            val intent = Intent(this, InputActivity::class.java)
            startActivity(intent)
        }

        //Realmのせってい
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)
        //ListViewの設定
        mTaskAdapter = TaskAdapter(this)
        Search_Category_button.setOnClickListener { view ->
            if (Search_Category == null) {
                reloadListView()
            } else {
                Category_reloadListView()
            }
            /*private fun getContentsInfo() {
                val resolver = contentResolver
                Search = resolver.query(
                    ,//データの種類
                    null,//項目
                    null,//フィルタ条件
                    null,//フィルタ用パラ
                    null//ソート
                )!!
                if (Search.moveToFirst()) {
                    //indexからIDを取得、そのIDからがぞうのURIをゲット
                    val fieldIndex = Search.getColumnIndex(MediaStore.Images.Media._ID)
                    val id = Search.getLong(fieldIndex)
                    val ImageURI =
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    picture_View.setImageURI(ImageURI)

                }*/
        }
        //listViewをタップした時の処理
        listView1.setOnItemClickListener { parent, _, position, _ ->
            Log.d("Debug","通過100")
            //入力画面に移す
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
            val aaaa=intent.putExtra(EXTRA_TASK, task.id)
            Log.d("Debug","I2D="+"$aaaa")
        }
        /*Debug_button.setOnClickListener(){
            var ID=intent.putExtra("EXTRA_TASK", task.id)

            for(i in EXTRA_TASK.indices) {
                Log.d("Debug", "list[$i]=${[$i]}")
            }
        }*/
        //ListViewを長押しした時の動作
        listView1.setOnItemLongClickListener { parent, view, position, id ->
            //タスクの削除
            val task = parent.adapter.getItem(position) as Task
            //ダイアログの表示
            val builder = AlertDialog.Builder(this)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか？")

            builder.setPositiveButton("OK") { _, _ ->
                val result = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()
                result.deleteAllFromRealm()
                mRealm.commitTransaction()

                reloadListView()
            }
            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true

        }

        reloadListView()
    }

    private fun Category_reloadListView() {
        val Category1 = Search_Category.text.toString()
        //Realmデーターベースから「全てのデータを取得しCategory_Searchに記載された文字列に等しいもののみ新しい日時順になら得た結果」を取得
        val taskRealmResults =
            mRealm.where(Task::class.java).equalTo("category", Category1).findAll()
                .sort("date", Sort.DESCENDING)
        //val taskList = mutableListOf("aaa", "bbb", "ccc")
        //取得結果をTaskListとしてセットする。
        mTaskAdapter.mTaskList = mRealm.copyFromRealm(taskRealmResults)
        //TaskのLiveView用のアダプタに渡す
        listView1.adapter = mTaskAdapter
        //表示を更新するために、アダプターにデータが更新されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()
    }

    private fun reloadListView() {
        //Realmデーターベースから「全てのデータを取得し新しい日時順になら得た結果」を取得
        val taskRealmResults =
            mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)
        //val taskList = mutableListOf("aaa", "bbb", "ccc")
        //取得結果をTaskListとしてセットする。
        mTaskAdapter.mTaskList = mRealm.copyFromRealm(taskRealmResults)
        //TaskのLiveView用のアダプタに渡す
        listView1.adapter = mTaskAdapter
        //表示を更新するために、アダプターにデータが更新されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }

    /*private fun addTaskForTest() {
        val task = Task()
        task.title = "作業"
        task.contents = "プログラムを書いてPUSHする"
        task.date = Date()
        task.id = 0
        mRealm.beginTransaction()
        mRealm.copyToRealmOrUpdate(task)
        mRealm.commitTransaction()
    }*/


}
