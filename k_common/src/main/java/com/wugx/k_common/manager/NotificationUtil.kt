package com.wugx.k_utils.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.wugx.k_common.R
import com.wugx.k_common.util.utilcode.util.AppUtils
import com.wugx.k_common.util.utilcode.util.Utils

/**
 *通知栏
 *
 *@author Wugx
 *@date   2018/12/24
 */
object NotificationUtil {

    fun createNotificationManager(): NotificationManager {
        return Utils.getApp().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun buildNotification(channelId: String, iconResId: Int, title: String,
                          message: String, clazz: Class<*>?): NotificationCompat.Builder {
        var pendingIntent:PendingIntent?=null
        clazz?.let {
            val intent = Intent(Utils.getApp(), clazz)
            pendingIntent = PendingIntent.getActivity(Utils.getApp(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        //自定义view
//        val remoteViews = RemoteViews(AppUtils.getAppPackageName(), R.layout.layout_notification_default)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, "channelName", NotificationManager.IMPORTANCE_HIGH)
//            notificationChannel.enableLights(false)//闪光灯
//            notificationChannel.lightColor = Color.RED//闪关灯的灯光颜色
//            notificationChannel.lockscreenVisibility = VISIBILITY_SECRET //锁屏显示通知
//            notificationChannel.canShowBadge() //桌面launcher的消息角标
//            notificationChannel.enableVibration(true) //是否允许震动
//            notificationChannel.setBypassDnd(true)//设置可绕过  请勿打扰模式
//            notificationChannel.canBypassDnd()
//            notificationChannel.vibrationPattern = longArrayOf(100, 100, 200)//设置震动模式
//            notificationChannel.shouldShowLights() //是否会有灯光

            createNotificationManager().createNotificationChannel(notificationChannel)
            return NotificationCompat.Builder(Utils.getApp(), channelId)
                    .setChannelId(channelId)
                    .setLargeIcon(BitmapFactory.decodeResource(Utils.getApp().resources,
                            iconResId))
                    .setSmallIcon(iconResId)
                    .setContentTitle(title)
                    .setContentText(message)
//                    .setCustomContentView(remoteViews)
//                    .setCustomBigContentView(remoteViews)
                    .setContentIntent(pendingIntent)
        } else {
            return NotificationCompat.Builder(Utils.getApp(), channelId)
                    .setChannelId(channelId)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setWhen(System.currentTimeMillis())
                    .setLargeIcon(BitmapFactory.decodeResource(Utils.getApp().resources,
                            iconResId))
                    .setSmallIcon(iconResId)
//                    .setCustomContentView(remoteViews)
//                    .setCustomBigContentView(remoteViews)
                    .setContentIntent(pendingIntent)
                    .setContentIntent(pendingIntent)
        }
    }

}