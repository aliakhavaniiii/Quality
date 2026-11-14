package com.qc.management

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.PersianCalendar
import org.json.JSONArray

object Reminders {
    fun schedule(c: Context) {
        val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            c, 0, Intent(c, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val t = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 8); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0)
            if (timeInMillis < System.currentTimeMillis()) add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, t.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
    }

    fun messages(c: Context): List<String> {
        val pc = PersianCalendar()
        val d = pc.get(Calendar.DAY_OF_MONTH)
        val m = pc.get(Calendar.MONTH) + 1
        val len = pc.getActualMaximum(Calendar.DAY_OF_MONTH)
        val out = mutableListOf<String>()
        if (d == len) out += "امروز آخر ماه است: نمونه‌های شاهد را بردارید"
        else if (len - d == 1) out += "فردا آخر ماه است: نمونه‌های شاهد را فراموش نکنید"
        if (d == 1 && m in listOf(3, 6, 9, 12))
            out += "امروز زمان نظافت سه‌ماهه" + (if (m == 6 || m == 12) " و شش‌ماهه" else "") + " است"
        try {
            val arr = JSONArray(c.getSharedPreferences("qc", 0).getString("loans", "[]"))
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val day = minOf(o.getInt("day"), len)
                val left = if (day >= d) day - d else len - d + o.getInt("day")
                if (left <= 2) out += "قسط ${o.getString("name")}: " + (if (left == 0) "امروز سررسید است" else "$left روز مانده")
            }
        } catch (e: Exception) { }
        return out
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        val msgs = Reminders.messages(c)
        if (msgs.isEmpty()) return
        val nm = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel("qc", "یادآورها", NotificationManager.IMPORTANCE_HIGH))
        val text = msgs.joinToString("\n")
        val n = Notification.Builder(c, "qc")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Quality Control Management")
            .setContentText(msgs.first())
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setAutoCancel(true).build()
        nm.notify(1, n)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) { Reminders.schedule(c) }
}
