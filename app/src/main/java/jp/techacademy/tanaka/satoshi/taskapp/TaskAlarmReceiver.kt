package jp.techacademy.tanaka.satoshi.taskapp

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.graphics.BitmapFactory
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import androidx.core.app.NotificationCompat
import io.realm.Realm


class TaskAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val notificationManager =
            context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //SDKver.26以上の場合、ちゃんねるを設定する
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                "default",
                "Channel name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Channel description"
            notificationManager.createNotificationChannel(channel)
        }
        //通知設定
        val builder = NotificationCompat.Builder(context, "default")
        builder.setSmallIcon(R.drawable.small_icon)
        builder.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.large_icon))
        builder.setWhen(System.currentTimeMillis())
        builder.setDefaults(Notification.DEFAULT_ALL)
        builder.setAutoCancel(true)
        //EXTRAからTASKidを取得し、IDからTASKのインスタンスを取得
        val TaskID = intent!!.getIntExtra(EXTRA_TASK, -1)
        val realm = Realm.getDefaultInstance()
        val task = realm.where(Task::class.java).equalTo("id", TaskID).findFirst()
        // TASKの情報を設定する
        builder.setTicker(task!!.title)
        builder.setContentTitle(task.title)
        builder.setContentText(task.contents)
        //通知をタップしたらアプリが起動する
        val startAppIntent = Intent(context, MainActivity::class.java)
        startAppIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        val pendingIntent = PendingIntent.getActivity(context, 0, startAppIntent, 0)
        builder.setContentIntent(pendingIntent)
        //通知を表示する
        notificationManager.notify(task!!.id, builder.build())
        realm.close()


    }
}
