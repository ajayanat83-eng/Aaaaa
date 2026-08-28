package com.example.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.model.Order
import com.example.model.OrderStatus

/**
 * Local Notification System for TJW Cafe.
 * Alerts customers with heads-up notifications when kitchen updates
 * order status to 'Ready for Pickup' or 'Served / Delivered'.
 */
object LocalNotificationService {

    private const val TAG = "TJW_Notifications"
    const val CHANNEL_ID_ORDER_STATUS = "tjw_order_status_channel"
    const val CHANNEL_NAME = "TJW Cafe Order Updates"
    const val CHANNEL_DESCRIPTION = "Real-time alerts for when your order is ready or served"

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                CHANNEL_ID_ORDER_STATUS,
                CHANNEL_NAME,
                importance
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                setShowBadge(true)
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel '$CHANNEL_ID_ORDER_STATUS' created.")
        }
    }

    /**
     * Checks if the app has permission to post notifications (Android 13+).
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Dispatches a local alert notification when order is READY or SERVED.
     */
    fun showOrderStatusNotification(
        context: Context,
        order: Order,
        newStatus: OrderStatus
    ) {
        try {
            initNotificationChannel(context)

            if (!hasNotificationPermission(context)) {
                Log.w(TAG, "Notification permission not granted, skipping notification.")
                return
            }

            val (title, message, emoji) = when (newStatus) {
                OrderStatus.READY -> {
                    Triple(
                        "🧇 Order Ready for Pickup! (${order.humanOrderNumber})",
                        "Your hot & fresh order is freshly prepared at The Janakpur Waffle counter! Please collect it.",
                        "🧇"
                    )
                }
                OrderStatus.DELIVERED -> {
                    Triple(
                        "✨ Order Served! (${order.humanOrderNumber})",
                        "Your delicious meal has been served to your table / delivered. Enjoy your pure veg dining experience!",
                        "🍽️"
                    )
                }
                OrderStatus.PREPARING -> {
                    Triple(
                        "👨‍🍳 Order Being Prepared (${order.humanOrderNumber})",
                        "Our kitchen chefs have started preparing your waffles & cafe specials.",
                        "🔥"
                    )
                }
                else -> return
            }

            // Intent to reopen app and navigate to Order Tracking
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("target_order_id", order.orderId)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                order.orderId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID_ORDER_STATUS)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            val notificationManager = NotificationManagerCompat.from(context)
            val notificationId = order.orderId.hashCode()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    notificationManager.notify(notificationId, builder.build())
                    Log.d(TAG, "Notification posted successfully for order ${order.humanOrderNumber} -> $newStatus")
                }
            } else {
                notificationManager.notify(notificationId, builder.build())
                Log.d(TAG, "Notification posted successfully for order ${order.humanOrderNumber} -> $newStatus")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error posting order notification: ${e.message}")
        }
    }
}
