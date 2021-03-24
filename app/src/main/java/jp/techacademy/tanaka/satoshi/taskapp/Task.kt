package jp.techacademy.tanaka.satoshi.taskapp

import android.util.Log
import java.io.Serializable
import java.util.Date
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class Task : RealmObject(), Serializable {
    var title: String = ""         //題名
    var contents: String = ""      //内容
    var date: Date = Date()        //日時
    var category:String=""         //カテゴリー
    //idをプライマリーキーとして設定
    @PrimaryKey
    var id: Int = 0

}
