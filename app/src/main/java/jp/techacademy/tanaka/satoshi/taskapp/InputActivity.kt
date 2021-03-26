package jp.techacademy.tanaka.satoshi.taskapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.view.View
import io.realm.Realm
import kotlinx.android.synthetic.main.content_input.*
import java.time.Year
import java.util.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.content.getSystemService
import android.util.Log

class InputActivity : AppCompatActivity() {
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0
    private var mTask: Task? = null

    private val mOnDateClickLinstener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mYear = year
                mMonth = month
                mDay = dayOfMonth
                val dateString = mYear.toString() + "/" + String.format(
                    "%02d",
                    mMonth + 1
                ) + "/" + String.format("%02d", mDay)
                date_button.text = dateString
            }, mYear, mMonth, mDay
        )
        datePickerDialog.show()
    }
    private val mOnTimeClickListener = View.OnClickListener {
        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                mHour = hour
                mMinute = minute
                val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
                times_button.text = timeString
            }, mHour, mMinute, false
        )
        timePickerDialog.show()
    }
    private val mOnDoneClickListener = View.OnClickListener {
        addTask()
        finish()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        //アクションバーを設定する
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        //UI部品の設定
        date_button.setOnClickListener(mOnDateClickLinstener)
        times_button.setOnClickListener(mOnTimeClickListener)
        done_button.setOnClickListener(mOnDoneClickListener)

        //EXTRA_TASKからTaskのIDを取得し、IDからTaskのインスタンスを取得
        val intent = intent
        val TaskID = intent.getIntExtra(EXTRA_TASK, -1)
        val realm = Realm.getDefaultInstance()
        mTask = realm.where(Task::class.java).equalTo("id", TaskID).findFirst()
        Log.d("Debug","TaskID="+"$TaskID")
        Log.d("Debug","エクストラタスク＝"+"$EXTRA_TASK")
        realm.close()

        if (mTask == null) {
            Log.d("Debug","mTask2="+"$mTask")
            //新規作成の場合
            val calendar = Calendar.getInstance()
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
        } else {
            Log.d("Debug","mTask=Not null")
            //更新の場合
            title_edit_text.setText(mTask!!.title)
            content_edit_text.setText(mTask!!.contents)
            category.setText(mTask!!.category)
            val calendar = Calendar.getInstance()
            calendar.time = mTask!!.date
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)

            val dateString = mYear.toString() + "/" + String.format(
                "%02d",
                mMonth + 1
            ) + "/" + String.format("%02d", mDay)
            val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
            date_button.text = dateString
            times_button.text = timeString
        }
    }

    private fun addTask() {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()

        if (mTask == null) {
            Log.d("Debug","通過4")
            //新規作成の場合
            mTask = Task()
            val taskRealmResults = realm.where(Task::class.java).findAll()
            val identifier: Int =
                if (taskRealmResults.max("id") != null) {
                    Log.d("Debug","通過5")
                    taskRealmResults.max("id")!!.toInt() + 1
                } else {
                    Log.d("Debug","通過6")
                    0
                }
            mTask!!.id = identifier
        }
        val title = title_edit_text.text.toString()
        val content = content_edit_text.text.toString()
        val category = category.text.toString()

        mTask!!.title = title
        mTask!!.contents = content
        mTask!!.category = category
        val calendar = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)
        val date = calendar.time
        mTask!!.date = date
        realm.copyToRealmOrUpdate(mTask!!)
        realm.commitTransaction()
        realm.close()

        val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
        resultIntent.putExtra(EXTRA_TASK, mTask!!.id)
        val resultPendingIntent = PendingIntent.getBroadcast(
            this,
            mTask!!.id,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, resultPendingIntent)
    }


}