package com.example.goalsetter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date

class GoalNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    private val db = FirebaseFirestore.getInstance()

    override fun doWork(): Result {
        // Fetching goals from Firestore
        db.collection("goals")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val goals = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Goal::class.java)?.apply { id = document.id }
                }
                // Find the goal with the nearest due date
                val closestGoal = goals.filter { it.duedate.isNotEmpty() }
                    .map { goal ->
                        goal to SimpleDateFormat("dd/MM/yyyy").parse(goal.duedate)
                    }
                    .filter { it.second.after(Date()) }
                    .minByOrNull { it.second }

                closestGoal?.let {
                    val goal = it.first
                    val pendingTasks = goal.taskStates.count { state -> !state }

                    // Send notification
                    sendNotification(goal.title, pendingTasks)
                }
            }

        return Result.success()
    }

    private fun sendNotification(title: String, pendingTasks: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "GoalSetterChannel"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Goal Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Reminder: $title")
            .setContentText("You have $pendingTasks pending tasks.")
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .build()

        notificationManager.notify(1, notification)
    }
}
